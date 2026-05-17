# Chapter 5 — Retention and Disposition Management

---

## 5.1 Why Retention Is Harder Than It Looks

Most engineers, when asked to implement retention, think: set a deletion date,
run a job that deletes old records. This is incorrect for regulated healthcare
content and would constitute a compliance violation in multiple scenarios.

Retention in LifecycleCM addresses five problems that the naive approach ignores.

**The trigger is not document creation.** A Medical Claim retained for 10 years
under CMS Medicare requirements is retained for 10 years from the date the claim
was closed, not the date it was received. If a claim is received in January and
not closed until November, using the received date understates the retention
obligation by 10 months. Premature destruction of a legally required record is a
federal compliance violation.

**Multiple policies can apply simultaneously.** A document subject to both HIPAA
(6 years) and CMS Medicare (10 years) must be retained for the longer period.
The system must evaluate all applicable policies and enforce the most restrictive
one. Choosing the least restrictive would result in the destruction of federally
protected records.

**Legal holds override everything.** If a document is under an active litigation
hold or regulatory inquiry hold, it cannot be disposed of regardless of its
retention schedule. A document 12 years past its nominal retention date is still
preserved if an active legal hold names it.

**Disposition requires human approval.** Flagging a document as eligible for
disposition is not the same as disposing of it. Disposition is a deliberate act
that requires Compliance Officer review and approval. The system never destroys
content autonomously.

**Metadata survives disposition.** When content is destroyed, the document record
is not deleted. The metadata, attribute values, lifecycle history, and audit trail
are preserved permanently. The organization can always prove that a document
existed, what it contained by attribute, how it was processed, and when and under
which policy it was destroyed.

---

## 5.2 Retention Policy Model

Each retention policy defines five elements.

**Policy Code** — a unique identifier referenced in logs, reports, and the
retention schedule browser. Example: `CMS_MEDICARE_10YR`.

**Regulatory Basis** — the specific statutory or regulatory citation that mandates
this retention period. This is not a label for display — it is the legal
justification that would be cited in a compliance audit response. Example:
`CMS Medicare Conditions of Participation 42 CFR §482.24`.

**Trigger Event** — the business event that starts the retention clock. This is
an attribute of the document, not the document's creation date. The trigger event
maps to a specific attribute key on the Document Class. The Retention Engine
reads the value of that attribute to compute the retention start date.

**Retention Years** — the number of years from the trigger event date that the
document must be retained before it is eligible for disposition.

**Disposition Action** — the action to take when eligibility is reached, after
Compliance Officer approval:
- `DELETE` — binary content is destroyed. Metadata is permanently preserved.
- `ARCHIVE_THEN_DELETE` — content is migrated to the ARCHIVE storage class in
  MinIO before eventual deletion. Used for records that may need retrieval during
  a transition period.
- `LEGAL_HOLD_CHECK` — even without an explicit hold, a manual compliance review
  is required before destruction. Used for document classes with complex
  disposition considerations.

---

## 5.3 Retention Policies — Healthcare Configuration

| Policy Code | Regulatory Basis | Trigger Event | Trigger Attribute | Years | Action | Document Class |
|---|---|---|---|---|---|---|
| CMS_MEDICARE_10YR | 42 CFR §482.24 | CLAIM_CLOSE_DATE | `service_date_to` | 10 | ARCHIVE_THEN_DELETE | MEDICAL_CLAIM |
| HIPAA_PHI_6YR | 45 CFR §164.530(j) | AUTHORIZATION_DECISION_DATE | `auth_decision_date` | 6 | DELETE | PRIOR_AUTH_REQUEST |
| HIPAA_EOB_6YR | 45 CFR §164.530(j) | DOCUMENT_CREATION | `received_date` | 6 | DELETE | EXPLANATION_OF_BENEFITS |
| ERISA_6YR | ERISA §107 | MEMBER_TERMINATION_DATE | `termination_date` | 6 | DELETE | MEMBER_ENROLLMENT_FORM |
| CMS_APPEAL_6YR | 42 CFR §422.562 | RESOLUTION_DATE | `resolution_date` | 6 | DELETE | APPEAL_AND_GRIEVANCE |
| SOX_7YR | Sarbanes-Oxley §802 | CONTRACT_TERMINATION_DATE | `termination_date` | 7 | ARCHIVE_THEN_DELETE | PROVIDER_CONTRACT |

---

## 5.4 Retention Schedule

When a document transitions to ARCHIVED, the Retention Engine computes its
retention schedule immediately and writes a `retention_schedule` record. This
record is the document's computed timeline.

The schedule contains:

| Field | Description |
|---|---|
| document_id | The governed document |
| policy_id | The governing retention policy |
| trigger_date | The date value read from the trigger attribute |
| eligible_for_disposition_date | `trigger_date + retention_years` |
| disposition_status | PENDING_TRIGGER → SCHEDULED → ELIGIBLE / ON_LEGAL_HOLD → DISPOSED |
| flagged_at | When the Retention Engine set status to ELIGIBLE |
| disposition_executed_at | When the Compliance Officer approved and executed disposition |
| disposition_executed_by_id | Which user executed the disposition |

If the trigger attribute has no value at the time of archival — for example, a
Medical Claim archived before `service_date_to` was populated — the schedule is
written with `disposition_status = PENDING_TRIGGER` and a warning is logged. The
Retention Engine will re-attempt schedule computation on each subsequent nightly
run until the trigger value is available.

---

## 5.5 Retention Engine Operation

The Retention Engine is a scheduled background job running nightly at a
configurable time (default: 02:00 local time, configurable via
`RETENTION_SCAN_CRON` environment variable). It executes two scans per run.

**Scan 1 — New Archive Schedules.**
Identifies documents in ARCHIVED state without a computed retention schedule
(newly archived since last run or previously failed due to missing trigger value).
Attempts trigger attribute resolution. On success, writes the retention schedule
record with `disposition_status = SCHEDULED`.

**Scan 2 — Eligibility Evaluation.**
For all documents with `disposition_status = SCHEDULED` and
`eligible_for_disposition_date <= today`:

1. Check for active legal holds on this document.
2. If a hold exists: set `disposition_status = ON_LEGAL_HOLD`. Log the hold
   reference number. No further action.
3. If no hold exists: set `disposition_status = ELIGIBLE`, set `flagged_at` to
   current timestamp. Fire `RetentionEligibilityFlaggedEvent`.

**Multi-Policy Conflict Resolution.**
If a document is subject to more than one applicable retention policy (future
extensibility), the engine evaluates all applicable policies and selects the one
producing the latest `eligible_for_disposition_date`. Most restrictive wins.
The selected policy is recorded in the schedule along with a note listing all
evaluated policies and their computed dates.

**Job Execution Log.**
Every Retention Engine run produces a structured log entry recording: run start
time, documents scanned in Scan 1, schedules created, documents scanned in Scan 2,
documents flagged eligible, documents blocked by legal hold, errors encountered,
and run duration.

---

## 5.6 Multi-Policy Conflict Resolution — Example

A MEDICAL_CLAIM document could theoretically be subject to both
`CMS_MEDICARE_10YR` (10 years from service date) and a state-level policy
requiring 15 years for pediatric records.

```
Policy A: CMS_MEDICARE_10YR
  trigger_date:                  2015-03-01
  eligible_for_disposition_date: 2025-03-01

Policy B: STATE_PEDIATRIC_15YR
  trigger_date:                  2015-03-01
  eligible_for_disposition_date: 2030-03-01

Conflict resolution: MOST_RESTRICTIVE
Selected policy:     STATE_PEDIATRIC_15YR
Governing date:      2030-03-01

Log entry:
  RETENTION_CONFLICT_RESOLVED doc:DOC-2015-0000118
  policies_evaluated=[CMS_MEDICARE_10YR:2025-03-01, STATE_PEDIATRIC_15YR:2030-03-01]
  selected=STATE_PEDIATRIC_15YR reason=MOST_RESTRICTIVE
```

---

## 5.7 Legal Holds

A legal hold is a formal instruction to suspend normal retention and disposition
for a defined set of documents. Legal holds are placed by Compliance Officers
and recorded with a hold reference identifier, a reason, a hold type, and the
placing user and timestamp.

**Hold Types:**
- `LITIGATION` — placed in response to active or anticipated litigation
- `REGULATORY_INQUIRY` — placed in response to a regulatory investigation or audit
- `INTERNAL_AUDIT` — placed for internal compliance review purposes

**Effect on Documents:**
When a legal hold is placed naming a document, that document's retention schedule
status is set to `ON_LEGAL_HOLD`. Transition to `ELIGIBLE_FOR_DISPOSITION` is
blocked at the state machine guard level, not just at the application level.
If the document is already in `ELIGIBLE_FOR_DISPOSITION` when the hold is placed,
it transitions back to `ON_LEGAL_HOLD`.

**Effect on Release:**
When a legal hold is released, the Retention Engine re-evaluates all documents
that were blocked by that hold on its next nightly run. Documents that are still
within their retention period return to `SCHEDULED` status. Documents that have
passed their eligible date are immediately flagged `ELIGIBLE`.

**Hold Reference Format:**
```
LH-{YYYY}-{HOLD_TYPE_CODE}-{SEQUENCE}
Example: LH-2025-LIT-0041
```

---

## 5.8 Disposition Execution

When the Retention Engine flags a document as ELIGIBLE, a disposition task is
created and appears in the Compliance Officer's queue in the Administration
Console.

The Compliance Officer reviews the document's metadata, its retention schedule,
its policy basis, and its audit trail. They may approve disposition or place the
document on hold if circumstances warrant.

On approval, `DispositionService` executes the following sequence:

1. Confirm the approving user holds `can_dispose` permission.
2. Confirm no legal hold was placed between flagging and approval.
3. Execute the policy's disposition action:
   - If `DELETE`: call Resource Manager to remove the MinIO object.
   - If `ARCHIVE_THEN_DELETE`: call Resource Manager to set storage class to
     ARCHIVE, then schedule deletion for a configurable grace period.
4. Set `document.content_reference_id` to null.
5. Transition document lifecycle state to `DISPOSED`.
6. Update `retention_schedule.disposition_status` to `DISPOSED`,
   `disposition_executed_at` to now, `disposition_executed_by_id` to approver.
7. Write `lifecycle_transition` record (ELIGIBLE → DISPOSED).
8. Log structured audit entry: disposition approved, policy, approver, timestamp.

The document record is never deleted. Only the binary content object is removed.

---

*Previous: [Chapter 4 — Content Lifecycle Management](04-lifecycle.md)*
*Next: [Chapter 6 — Access Control and HIPAA Compliance](06-access-control.md)*
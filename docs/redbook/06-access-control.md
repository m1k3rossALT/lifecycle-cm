# Chapter 6 — Access Control and HIPAA Compliance

---

## 6.1 The HIPAA Access Control Requirement

HIPAA's Security Rule (45 CFR §164.312) mandates that covered entities implement
technical policies and procedures allowing only authorized persons to access
electronic protected health information (ePHI). This is not a recommendation.
It is a legal requirement carrying civil and criminal penalties for violations.

LifecycleCM enforces HIPAA access control at three levels, each independently
enforced. Bypassing one does not allow access.

**Authentication** — every request carries a JWT Bearer token. Unauthenticated
requests are rejected by the security filter before reaching any service method.
No document data is returned without a valid, unexpired token.

**Authorization** — every document access is checked against the requesting user's
role permissions for the target Document Class. A user without `can_read`
permission for a class receives a 403 response and never sees that class's
documents in search results.

**Scope filtering** — within a permitted Document Class, the user's visible
documents are filtered by their `scope_type`. Role-based scope is not a display
preference — it is a query-level filter applied before results are assembled.
There is no way for a scoped user to retrieve documents outside their scope by
crafting a different query.

---

## 6.2 Role Definitions

LifecycleCM defines seven roles. Each maps to a real job function in a healthcare
payer organization. Role assignments carry effective dates — roles can be granted
for a defined period and expire automatically.

---

### CLAIMS_PROCESSOR

Processes incoming medical claims and prior authorization requests. Works from an
assigned task queue. Cannot access documents outside their assigned queue.

**Scope:** OWN_QUEUE — sees only documents assigned to their queue.

**Permitted operations:** Read, create, download, transition (RECEIVED→INDEXED,
INDEXED→UNDER_REVIEW, UNDER_REVIEW→PENDED, PENDED→UNDER_REVIEW).

---

### MEDICAL_DIRECTOR

Performs clinical review of prior authorization requests and clinically complex
claims. Sees all clinical documents regardless of queue assignment.

**Scope:** ALL within PRIOR_AUTH_REQUEST and MEDICAL_CLAIM classes.

**Permitted operations:** Read, download, transition (UNDER_REVIEW→APPROVED,
UNDER_REVIEW→DENIED).

---

### MEMBER_SERVICES_REPRESENTATIVE

Assists members with enrollment, EOB queries, and appeal intake. Sees documents
for members they are assigned to assist.

**Scope:** MEMBER_SCOPED — sees documents where `member_id` matches their
assigned member roster.

**Permitted operations:** Read, create (MEMBER_ENROLLMENT_FORM,
APPEAL_AND_GRIEVANCE), download, transition (RECEIVED→INDEXED on enrollment
and appeal classes).

---

### PROVIDER_RELATIONS

Manages provider contracts and resolves provider-facing processing issues. Sees
documents associated with providers in their portfolio.

**Scope:** PROVIDER_SCOPED — sees documents where `provider_npi` matches their
assigned provider portfolio.

**Permitted operations:** Read, create (PROVIDER_CONTRACT), download, transition
(contract workflow steps).

---

### COMPLIANCE_OFFICER

Full read access across all Document Classes. The only role permitted to place
and release legal holds, approve dispositions, and view PHI access logs. Not a
processing role — should not be used for document ingestion or adjudication work.

**Scope:** ALL.

**Permitted operations:** Read all classes, download all, transition to
ARCHIVED (manual), place/release legal hold, approve disposition (DISPOSED),
view phi_access_log.

---

### AUDITOR

Read-only access across all Document Classes for audit and compliance reporting.
Cannot modify documents, create documents, transition states, or place holds.

**Scope:** ALL.

**Permitted operations:** Read all classes, view lifecycle_transition log,
view phi_access_log. No write operations.

---

### SYSTEM_ADMIN

Full system access including all configuration surfaces. Manages users, roles,
Document Class definitions, retention policies, and workflow definitions. Not a
document processing role — should not be used for operational document work.

**Scope:** ALL.

**Permitted operations:** All operations including admin-only endpoints.

---

## 6.3 Role Permission Matrix

The following matrix shows the permission set per role across Document Classes.
R = Read, C = Create, D = Download, T = Transition, H = Place/Release Hold,
X = Dispose.

| Role | MEDICAL_CLAIM | PRIOR_AUTH | EOB | MEMBER_ENROLL | APPEAL | PROVIDER_CONTRACT |
|---|---|---|---|---|---|---|
| CLAIMS_PROCESSOR | R,C,D,T (own queue) | R,C,D,T (own queue) | — | — | — | — |
| MEDICAL_DIRECTOR | R,D,T | R,D,T | — | — | — | — |
| MEMBER_SERVICES | R,D (member-scoped) | R (member-scoped) | R,D | R,C,D,T | R,C,D,T | — |
| PROVIDER_RELATIONS | R,D (provider-scoped) | R (provider-scoped) | — | — | — | R,C,D,T |
| COMPLIANCE_OFFICER | R,D,T,H,X | R,D,T,H,X | R,D,H,X | R,D,T,H,X | R,D,T,H,X | R,D,T,H,X |
| AUDITOR | R | R | R | R | R | R |
| SYSTEM_ADMIN | All | All | All | All | All | All |

---

## 6.4 Scope Types

Scope type determines the query-level filter applied to document searches. It is
a property of the role assignment, not the user account.

**ALL** — No additional filter beyond Document Class permission. Used by
COMPLIANCE_OFFICER, AUDITOR, MEDICAL_DIRECTOR (within their classes),
SYSTEM_ADMIN.

**OWN_QUEUE** — Documents are filtered to those with `assigned_queue` matching
the user's assigned queue identifiers. Used by CLAIMS_PROCESSOR.

**MEMBER_SCOPED** — Documents are filtered to those where `member_id` is in the
user's assigned member roster. Used by MEMBER_SERVICES_REPRESENTATIVE.

**PROVIDER_SCOPED** — Documents are filtered to those where `provider_npi` is in
the user's assigned provider portfolio. Used by PROVIDER_RELATIONS.

Scope filtering is applied at the query level, not at the result filtering level.
A scoped user's query never retrieves out-of-scope documents and then discards
them. Out-of-scope documents are excluded from the SQL query itself.

---

## 6.5 PHI Access Logging

HIPAA requires that covered entities maintain a record of every access to ePHI.
LifecycleCM implements this through an automatic PHI access interceptor that fires
on any response containing data from a PHI-bearing Document Class.

The interceptor does not require explicit calls from application code for standard
operations. It inspects the response context, identifies PHI-bearing documents
in the response, and writes `phi_access_log` records automatically.

Each log record contains:

| Field | Description |
|---|---|
| document_id | The PHI-bearing document that was accessed |
| accessed_by_id | The authenticated user's ID |
| access_type | VIEW_METADATA, DOWNLOAD_CONTENT, or SEARCH_RESULT |
| access_reason | Role-based justification (e.g. "CLAIMS_PROCESSOR — queue processing") |
| accessed_at | Precise timestamp |
| client_ip | IP address of the requesting client |

**The PHI access log is immutable.** No application code path can update or delete
a log record. The repository interface for `phi_access_log` exposes only insert
and read operations.

---

## 6.6 Transition Permission Guard — Detail

Every state transition attempt passes through five sequential checks before
executing. This is documented in Chapter 4 from the lifecycle perspective; the
access control implementation is described here.

The guard is implemented as a Spring Statemachine guard bean wired to all
human-triggered transitions. It receives the state machine context, which carries
the authenticated user's details in its extended state.

**Check 1** — Calls `StateMachine.canTransit(fromState, event)`. Framework-level.
No custom code involved.

**Check 2** — Calls `AccessControlService.canTransition(userId, documentClassCode)`.
Evaluates `role_permission.can_transition` for all active roles of the user.

**Check 3** — Calls `AccessControlService.isTransitionPermitted(roleCode, fromState, toState)`.
Evaluates the transition-level role restriction table. This is a static table
loaded from the `role_permitted_transition` configuration at startup.

**Check 4** — If `toState == DISPOSED`: calls
`AccessControlService.canDispose(userId, documentClassCode)`. Evaluates
`role_permission.can_dispose`.

**Check 5** — Calls `LegalHoldService.hasActiveLegalHold(documentId)`. If true
and `toState` is `ELIGIBLE_FOR_DISPOSITION` or `DISPOSED`, guard returns false
with reason `ACTIVE_LEGAL_HOLD`.

On guard failure, the reason code is set in the state machine context. The
controller reads the reason code and returns a 403 response with a body containing
the specific reason. Denied transitions are written to the `lifecycle_transition`
log with a denial note — they are not silently discarded.

---

## 6.7 Authentication — JWT Token Structure

Tokens are signed with HMAC-SHA256 using a server-side secret injected via
environment variable. Tokens carry the following claims:

```json
{
  "sub": "U-0041",
  "username": "j.chen",
  "roles": ["CLAIMS_PROCESSOR"],
  "scope_type": "OWN_QUEUE",
  "assigned_queue": "QUEUE-NE-01",
  "region_code": "NORTHEAST",
  "iat": 1716000000,
  "exp": 1716086400
}
```

Token expiry is 24 hours. There is no refresh token mechanism in this
implementation — re-authentication is required after expiry.

The JWT secret is never logged. Token claims are never logged in their raw form.
Log lines reference user ID only.

---

*Previous: [Chapter 5 — Retention and Disposition](05-retention.md)*
*Next: [Chapter 7 — Workflow Engine](07-workflow.md)*
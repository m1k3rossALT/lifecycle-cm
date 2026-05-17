# Chapter 4 — Content Lifecycle Management

---

## 4.1 What Lifecycle Management Means

A document in LifecycleCM is never just a stored object. It is a governed object
at a defined position in a defined process. That position is its lifecycle state.
The rules governing how it moves between positions — which transitions are legal,
who can trigger them, what happens when they fire — constitute the lifecycle
management system.

This is the capability that most clearly separates an ECM platform from a file
storage system. A file on a share has no state. It cannot be required to pass
through a review before it is accessible. It cannot be prevented from deletion
during litigation. It cannot automatically move to archive when a business process
completes.

In LifecycleCM, none of these things are optional. They are enforced at the
framework level by a formal state machine. An illegal transition is not blocked by
a conditional check — it simply does not exist as an executable path.

---

## 4.2 Lifecycle States

A document is always in exactly one lifecycle state. The following states apply
across all Document Classes, with class-specific entry points and terminal states
noted where they differ.

### RECEIVED
The document has entered the system. It has been stored in the binary repository
and a document record has been created. It has not yet been reviewed, validated,
or assigned. It exists as a raw ingestion object awaiting indexing.

*Entry point for all Document Classes ingested by human operators.*

### INDEXED
A processor has reviewed the document, confirmed its identity, verified its
attribute values, and confirmed its Document Class assignment. The document is
now a fully classified repository object. Its metadata is complete and trusted.

*Entry point for EXPLANATION_OF_BENEFITS (system-generated, enters directly at
GENERATED state which transitions to INDEXED automatically).*

### UNDER_REVIEW
The document is actively being evaluated for a business decision. For a medical
claim, this is adjudication. For a prior authorization, this is clinical review.
For an appeal, this is the formal review process. A workflow task is active at
this state.

### APPROVED
The business decision has been made affirmatively. Payment will be issued, a
service is authorized, or an appeal has been upheld. The document is closed to
further processing through the standard path.

### DENIED
The business decision has been made negatively. A claim has been rejected, an
authorization has been refused, or an appeal has been dismissed. The document
may become the subject of a subsequent appeal.

### PENDED
Processing has been paused pending additional information from the member,
provider, or an external source. The document remains in an active queue. It
is not closed but cannot proceed until the pending condition is resolved.

### ARCHIVED
The business process is complete. The document is no longer in active processing.
It must be retained per its governing retention policy. Transition to ARCHIVED
triggers automatic computation of the document's retention schedule.

### ELIGIBLE_FOR_DISPOSITION
The Retention Engine has determined the document has satisfied its retention
requirement and is eligible to be destroyed. This state is system-assigned only —
it cannot be set by a human operator. It signals to Compliance Officers that
disposition approval is pending.

### ON_LEGAL_HOLD
An active legal hold has been placed on this document. Transition to
ELIGIBLE_FOR_DISPOSITION is blocked regardless of the document's retention
schedule. This state can overlay a document at any point in its lifecycle.

### DISPOSED
The document's binary content has been destroyed per the approved retention
disposition action. The metadata record, attribute values, lifecycle history, and
audit trail are permanently preserved. This state is terminal. It cannot be
reversed.

### REMEDIATION
The document has been flagged as requiring correction — an indexing error, wrong
Document Class assignment, or invalid attribute value. It is returned to the
processing queue for reprocessing. From REMEDIATION, a corrected document
transitions back to INDEXED.

---

## 4.3 State Transition Rules

The following table defines every legal transition in the system, the role
required to trigger it, and whether it is human-initiated or system-initiated.

| From State | To State | Permitted Role(s) | Trigger Type |
|---|---|---|---|
| RECEIVED | INDEXED | CLAIMS_PROCESSOR, MEMBER_SERVICES_REP | Human |
| RECEIVED | REMEDIATION | CLAIMS_PROCESSOR | Human |
| INDEXED | UNDER_REVIEW | CLAIMS_PROCESSOR, MEDICAL_DIRECTOR | Human |
| INDEXED | REMEDIATION | CLAIMS_PROCESSOR | Human |
| UNDER_REVIEW | APPROVED | CLAIMS_PROCESSOR, MEDICAL_DIRECTOR | Human |
| UNDER_REVIEW | DENIED | CLAIMS_PROCESSOR, MEDICAL_DIRECTOR | Human |
| UNDER_REVIEW | PENDED | CLAIMS_PROCESSOR | Human |
| PENDED | UNDER_REVIEW | CLAIMS_PROCESSOR | Human |
| APPROVED | ARCHIVED | SYSTEM, COMPLIANCE_OFFICER | System / Human |
| DENIED | ARCHIVED | SYSTEM, COMPLIANCE_OFFICER | System / Human |
| REMEDIATION | INDEXED | CLAIMS_PROCESSOR | Human |
| ARCHIVED | ELIGIBLE_FOR_DISPOSITION | SYSTEM (Retention Engine only) | System |
| ELIGIBLE_FOR_DISPOSITION | DISPOSED | COMPLIANCE_OFFICER | Human |
| ANY | ON_LEGAL_HOLD | COMPLIANCE_OFFICER | Human |
| ON_LEGAL_HOLD | (prior state restored) | COMPLIANCE_OFFICER | Human |

Any transition not listed above is illegal and will be rejected by the state
machine before any business logic executes.

---

## 4.4 Transition Guard Evaluation

Before any transition executes, the following five checks must all pass. Failure
at any check produces a `403 Forbidden` response with a specific reason code. The
denied attempt is logged.

**Check 1 — State machine legality.** Is the requested from-state → to-state
transition defined in the state machine configuration for this Document Class?

**Check 2 — Class permission.** Does the requesting user's role have
`can_transition` permission for this Document Class?

**Check 3 — Transition-level role restriction.** Even within a permitted class,
does the role have permission for this specific transition? A CLAIMS_PROCESSOR
cannot transition to APPROVED on a PRIOR_AUTH — that requires MEDICAL_DIRECTOR.

**Check 4 — Disposition permission.** If the target state is DISPOSED, does the
user hold `can_dispose` permission? This is restricted to COMPLIANCE_OFFICER only.

**Check 5 — Legal hold block.** Is an active legal hold preventing this
transition? Applies when the target state is ELIGIBLE_FOR_DISPOSITION or DISPOSED.

---

## 4.5 The Transition Audit Trail

Every state transition — without exception — is recorded as a permanent,
immutable row in `lifecycle_transition`. This record is never updated and never
deleted.

Each record captures:

| Field | Description |
|---|---|
| document_id | The document that transitioned |
| from_state | The state before the transition |
| to_state | The state after the transition |
| triggered_by_id | User ID if human-triggered; null if system-triggered |
| system_triggered | Boolean flag for system-initiated transitions |
| trigger_note | Optional reason or note supplied by the operator |
| transitioned_at | Precise timestamp of the transition |
| client_ip | IP address of the requesting client (HIPAA traceability) |

When a compliance auditor or federal examiner requests the complete processing
history of a document, the `lifecycle_transition` table is the definitive answer.
Every action taken on a document, by whom, when, and from what state, is recorded
here permanently.

---

## 4.6 Spring Statemachine Implementation

The lifecycle is implemented using Spring Statemachine. This is not a custom
if/else branching structure. It is a formally configured state machine where
states, transitions, guards, and actions are declared once and enforced by the
framework.

**States** are declared as enum constants matching the lifecycle state definitions
above. The state machine holds exactly one active state per document instance.

**Transitions** are declared between specific from-state and to-state pairs with
an associated event name. Events are the mechanism by which application code
requests a transition. If no transition is declared for a given event in the
current state, the framework rejects it without invoking any guard or action.

**Guards** are Spring beans evaluated before a transition executes. The five
guard checks described in Section 4.4 are implemented as a guard condition wired
to every human-triggered transition. If any check fails, the guard returns false,
the transition is rejected, and no action is taken.

**Actions** are Spring beans invoked when a transition executes successfully. Two
actions are wired to every transition:

- `TransitionAuditAction` — writes the `lifecycle_transition` record
- `TransitionEventPublisher` — fires the `LifecycleTransitionEvent` for downstream
  listeners (workflow task creation, retention schedule computation, etc.)

Using a formal state machine framework rather than application-level conditional
logic provides a guarantee that informal code cannot: the state machine
configuration is the complete and authoritative definition of all legal document
behaviour. There is no other code path that can move a document between states.

---

## 4.7 Domain Events Fired on Transition

The `LifecycleTransitionEvent` carries the document ID, from-state, to-state,
and triggering user. Downstream listeners react independently.

| Transition | Listener | Action Taken |
|---|---|---|
| null → RECEIVED | WorkflowEventListener | Creates WorkflowTask for step 1 of the document class workflow |
| INDEXED → UNDER_REVIEW | WorkflowEventListener | Creates WorkflowTask for clinical review step |
| APPROVED / DENIED → ARCHIVED | RetentionScheduleListener | Computes and writes retention schedule |
| ANY → ON_LEGAL_HOLD | LegalHoldListener | Updates all active retention schedules for document to ON_LEGAL_HOLD status |
| ARCHIVED → ELIGIBLE | DispositionNotificationListener | Creates disposition task visible to COMPLIANCE_OFFICER |

Listeners are independent. Adding new behaviour on a transition means adding a
new listener. Existing listeners are never modified. This is the open/closed
principle applied to event-driven architecture.

---

*Previous: [Chapter 3 — Document Model](03-document-model.md)*
*Next: [Chapter 5 — Retention and Disposition Management](05-retention.md)*
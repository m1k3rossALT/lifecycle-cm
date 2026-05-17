# Chapter 7 — Workflow Engine

---

## 7.1 Design Approach

LifecycleCM implements a task-based workflow engine tied directly to document
lifecycle state transitions. It does not implement a full BPMN process engine.
This is a deliberate, scoped decision: the goal is to demonstrate that documents
drive business process, not to build a general-purpose workflow orchestrator.

The governing model is:

> **Document state transitions create and resolve work items.**

When a document enters a state that has a workflow step defined for it, a
`WorkflowTask` is created and placed in the role queue responsible for that step.
When a user completes the task, the document transitions to the next state, which
may create the next task in the sequence. The document's lifecycle state is the
integration point between the content repository and the business process.

This is structurally identical to how enterprise ECM platforms integrate with
adjacent workflow systems: the document state drives task creation and
resolution. The workflow does not live separately from the document — it is
expressed through the document's progression.

---

## 7.2 Workflow Model

A workflow is defined by two entities.

**WorkflowDefinition** — the named workflow assigned to a Document Class. Defines
which document class it governs and provides a display name for operations staff.
Each Document Class has at most one active workflow definition.

**WorkflowStep** — an individual step within a workflow. Each step defines:

| Field | Description |
|---|---|
| step_code | Unique identifier within the workflow |
| step_name | Human-readable name shown in the task queue |
| assigned_role | The role queue this step's tasks are assigned to |
| triggers_on_state | The lifecycle state whose entry creates a task for this step |
| resolves_to_state | The lifecycle state the document moves to when this task is completed |
| step_order | Sequence number for display and reporting purposes |
| sla_hours | Maximum hours allowed for this step before escalation |

**WorkflowTask** — a runtime instance of a workflow step for a specific document.
Tasks are created by `WorkflowEventListener` when a `LifecycleTransitionEvent`
fires and the to-state matches a step's `triggers_on_state`.

---

## 7.3 Prior Authorization Processing Workflow

The Prior Authorization Processing workflow is LifecycleCM's primary workflow
demonstration. It maps to the real operational flow used by healthcare payers to
process authorization requests from providers.

```
DOCUMENT RECEIVED (lifecycle state: RECEIVED)
│
│  WorkflowTask created:
│  "Index Authorization Request"
│  Assigned to: CLAIMS_PROCESSOR queue
│  SLA: 4 hours
│  Action: Verify member eligibility, confirm provider credentials,
│           validate request completeness, assign urgency level
│
▼ Task completed → document transitions RECEIVED → INDEXED

DOCUMENT INDEXED (lifecycle state: INDEXED)
│
│  WorkflowTask created:
│  "Route for Clinical Review"
│  Assigned to: CLAIMS_PROCESSOR queue
│  SLA: 2 hours
│  Action: Confirm clinical pathway, assign to appropriate
│           medical director queue based on service type
│
▼ Task completed → document transitions INDEXED → UNDER_REVIEW

DOCUMENT UNDER REVIEW (lifecycle state: UNDER_REVIEW)
│
│  WorkflowTask created:
│  "Clinical Review Decision"
│  Assigned to: MEDICAL_DIRECTOR queue
│  SLA: 24 hours (STANDARD urgency) / 6 hours (URGENT) / 1 hour (EMERGENT)
│  Action: Review clinical criteria, evaluate against coverage
│           guidelines, make approval decision
│
▼ Task completed → document transitions UNDER_REVIEW → APPROVED or DENIED

                    ┌──────────────┐
                    │   APPROVED   │
                    └──────┬───────┘
                           │
                           │  WorkflowTask created:
                           │  "Notification and Filing"
                           │  Assigned to: CLAIMS_PROCESSOR queue
                           │  SLA: 2 hours
                           │  Action: Send approval notification to
                           │           provider, associate with any
                           │           pending claims, file document
                           │
                           ▼ Task completed → document transitions APPROVED → ARCHIVED
                           │
                           ▼ Retention schedule computed on ARCHIVED transition
                           │
                           ▼ Document enters governed archive
```

For DENIED outcomes, a separate "Denial Notification" step is created, assigned
to CLAIMS_PROCESSOR, with a 4-hour SLA for sending the denial notice and
documenting the clinical rationale.

---

## 7.4 Task Claim Model

WorkflowTasks are not assigned to individual users. They are assigned to role
queues. Every user with the matching role sees all unclaimed tasks in their queue.

A user explicitly **claims** a task to take ownership. Claiming removes the task
from the general pool — it no longer appears as available to other queue members
and appears in the claiming user's personal work list.

A user can **release** a claimed task back to the general queue if they cannot
complete it. This mirrors real-world queue management where a processor may need
to return a task they cannot action.

**Why claim-based rather than direct assignment?** Queue-based processing with
explicit claiming is how real healthcare operations centers handle work
distribution. Direct assignment requires a routing engine that knows individual
processor capacity and availability. Queue-based claiming allows natural
load distribution and handles absence gracefully — unclaimed tasks remain
visible to the entire role queue.

---

## 7.5 Task Priority

Task priority is a computed integer value used to order the task queue display.
Higher numeric priority means higher urgency. Priority is computed from two
factors.

**SLA proximity factor:** As a task approaches its due time, priority increases.
Tasks with more than 50% of their SLA remaining have base priority. Tasks with
25-50% remaining have elevated priority. Tasks with less than 25% remaining have
high priority. Tasks past their due time have critical priority.

**Urgency level factor:** If the associated document has an `urgency_level`
attribute value of `URGENT`, the task's base priority is multiplied by 2. If
`EMERGENT`, the base priority is multiplied by 4.

Priority is recomputed by the SLA scanner job that runs every 15 minutes. Queue
displays are always ordered by current computed priority descending.

---

## 7.6 SLA Tracking and Escalation

The SLA scanner is a scheduled job running every 15 minutes. For every task in
`OPEN` or `IN_PROGRESS` status, it evaluates the `due_at` timestamp against
current time.

**Approaching SLA (< 25% remaining):** Priority is elevated to HIGH. The task
appears highlighted in the queue display.

**SLA Breached (due_at < now):** The task status is set to `ESCALATED`. An
`AuditLogger.slaBreached()` entry is written recording the document ID, step,
assigned role, due time, and breach duration. Priority is set to maximum.

**Escalated tasks** appear at the top of every queue display with a visual
indicator. They remain in the assigned role queue — escalation in LifecycleCM is
a priority and visibility change, not an automatic reassignment. Manual
reassignment by a supervisor is the expected operational response.

---

## 7.7 Task Lifecycle States

A WorkflowTask moves through the following states:

```
OPEN         → The task exists and is unclaimed in the role queue
CLAIMED      → A user has taken ownership; visible in their personal list
IN_PROGRESS  → The user has marked the task as actively being worked
COMPLETED    → The user has recorded a completion decision
CANCELLED    → The task was voided (e.g. document moved to REMEDIATION)
ESCALATED    → SLA has been breached
```

When a task is completed, `WorkflowService` validates that the completing user
is the current claimant, records the completion timestamp and notes, and triggers
the document lifecycle transition to the task's `resolves_to_state`.

The lifecycle transition fires a `LifecycleTransitionEvent`, which the
`WorkflowEventListener` receives. If the new state matches the next step's
`triggers_on_state`, the next task is created. The workflow advances one step at
a time, driven entirely by document state.

---

## 7.8 Workflow Definitions Installed at Seed

Five workflow definitions are seeded at startup.

| Workflow Code | Document Class | Steps |
|---|---|---|
| PRIOR_AUTH_PROCESSING | PRIOR_AUTHORIZATION_REQUEST | 4 steps (detailed above) |
| CLAIMS_INTAKE | MEDICAL_CLAIM | 3 steps: Index → Route → Adjudicate |
| ENROLLMENT_VERIFICATION | MEMBER_ENROLLMENT_FORM | 2 steps: Index → Verify |
| APPEAL_PROCESSING | APPEAL_AND_GRIEVANCE | 3 steps: Index → Review → Resolve |
| CONTRACT_REVIEW | PROVIDER_CONTRACT | 3 steps: Index → Legal Review → Execute |

---

*Previous: [Chapter 6 — Access Control](06-access-control.md)*
*Next: [Chapter 8 — System Administration](08-administration.md)*
# Chapter 8 — System Administration

---

## 8.1 Two Clients, Two Audiences

LifecycleCM ships two separate frontend applications. They share no code, run on
separate ports, and serve completely different purposes.

**ecm-admin** is the System Administration Console. Its audience is the system
administrator and the compliance officer responsible for system configuration.
It exposes every configuration surface in the system. An operator can configure
the entire LifecycleCM repository — document schemas, retention policies, user
accounts, role assignments, legal holds — without touching a database or
configuration file. Ordinary processing staff never log into this application.

**ecm-client** is the Operations Client. Its audience is the processing staff —
claims processors, medical directors, member services representatives, provider
relations staff. It presents only what the authenticated user's role permits.
No configuration surfaces are exposed. A user never sees an action they are not
permitted to take.

The design language of the two clients deliberately differs. The Administration
Console is dense and information-rich — it is a tool for technically capable
operators. The Operations Client is clean and task-focused — it is a tool for
operational staff whose time-to-task matters.

---

## 8.2 Administration Console — ecm-admin

Access requires the `SYSTEM_ADMIN` role. All endpoints backing the admin console
are protected by a role guard that rejects requests from any other role with a
403 response before business logic executes.

### Repository Health Dashboard

The first screen any operator checks when investigating a reported issue. Displays
live status cards for:

- ecm-library-service — status, version, response time
- ecm-resource-manager — status, version, response time
- PostgreSQL (Library DB) — connection status, active connections
- PostgreSQL (Resource DB) — connection status
- MinIO — status, bucket name, total objects, total storage used
- Retention Engine — last run timestamp, last run result, next scheduled run

Each card shows either a green UP or a red DOWN indicator. The page auto-refreshes
every 30 seconds.

### Document Model Management

**Attribute Group browser** — lists all Attribute Groups with their definitions.
Each row shows the group code, display name, number of definitions, and active
status. Selecting a group shows all attribute definitions in it with their full
configuration.

**Attribute Group editor** — allows creation of new groups and addition, editing,
and deactivation of attribute definitions. Deactivating a definition does not
remove it — documents that already have values for it retain those values. New
documents will not be required to supply deactivated attributes.

**Document Class browser** — lists all Document Classes with their PHI flag,
active status, governing retention policy, and assigned workflow. Selecting a
class shows the ordered list of Attribute Groups attached to it.

**Document Class editor** — allows creation of new Document Classes and
modification of Attribute Group assignments and ordering. A new Document Class
is immediately available for document ingestion once saved.

### User and Access Management

**User list** — all user accounts with their status (active/inactive), last
login timestamp, department, and region code. Search by username or email.

**User detail and editor** — account creation, field editing, password reset,
and account activation/deactivation. Deactivated users cannot authenticate.
Their historical audit trail is preserved.

**Role assignment panel** — for each user, shows their current role assignments
with effective dates. New assignments can be added with a start date and optional
end date. When an end date is reached, the assignment expires automatically and
the user loses that role at their next login.

**Role permission matrix** — a read-only table showing every permission bit for
every role across every Document Class. This is the reference view for access
control configuration. Permission rules are defined in seed data and are not
editable through the UI — changes require a code-level configuration update to
ensure they are captured in version control.

### Retention Management

**Policy browser** — all retention policies with their regulatory basis, trigger
event, retention years, and disposition action. Read-only. Policy definitions are
managed through seed data.

**Retention schedule browser** — searchable by document number, Document Class,
policy code, or disposition status. Shows each document's computed trigger date,
eligible date, and current status. This is the screen a compliance officer uses
to answer "when is this document eligible for disposal?"

**Legal hold management** — lists all active legal holds with their reference
number, type, reason, placing user, and date placed. A new hold can be placed
from this screen, specifying one or more document numbers to hold. An active hold
can be released by selecting it and confirming the release.

**Disposition approval queue** — documents in `ELIGIBLE_FOR_DISPOSITION` status
awaiting Compliance Officer review. Each entry shows the document number, class,
policy, trigger date, eligible date, and flagging date. The officer selects a
document, reviews its metadata and audit trail, and either approves disposition
or places it on hold.

### Workflow Configuration

**Workflow definition viewer** — lists all workflow definitions with their
associated Document Class and step count. Selecting a definition shows the full
step sequence with trigger states, resolving states, assigned roles, and SLA hours.

**Step SLA editor** — SLA hours for each workflow step can be adjusted without
a code deployment. Changes take effect for tasks created after the change.

### Audit and Compliance

**PHI access log browser** — filterable by user, document, date range, access
type, and IP address. Paginated results. This is the primary screen for responding
to a HIPAA access audit request.

**Lifecycle transition log browser** — filterable by document number, user, date
range, from-state, and to-state. Shows the complete processing history of any
document including denied transition attempts.

**System event log viewer** — shows Retention Engine run logs, SLA breach events,
startup events, and system errors. Filterable by level and date range.

---

## 8.3 Operations Client — ecm-client

Access requires any role other than SYSTEM_ADMIN. The navigation and available
actions are role-filtered at the API level — the client does not make layout
decisions based on role. Role-restricted actions simply do not appear because
the APIs backing them return 403 for the requesting user.

### Document Inbox

The default landing screen after login. Shows the authenticated user's task queue
ordered by computed priority. Each row shows:

- Document number and class
- Step name
- Member ID or claim number (key identifying attribute)
- Time remaining before SLA breach
- Priority indicator (colour-coded)
- Claim / Release button

Claimed tasks appear in a separate "My Work" section above the general queue.

### Document Ingestion

A form for submitting new documents. The user selects a Document Class from the
classes their role can create. The form dynamically generates attribute input
fields based on the class's Attribute Group configuration — including data type
validation, required field enforcement, and enum dropdowns where applicable.
The user attaches the binary content file and submits.

On successful ingestion, the assigned document number is shown and the first
workflow task appears in the queue.

### Document Search

Faceted search with the following filters:

- Document Class (multi-select, role-filtered)
- Lifecycle State (multi-select)
- Date Range (received date or custom attribute date)
- Attribute value (key-value pair, up to three simultaneously)

Results show document number, class, current state, key attributes, and last
modified date. Selecting a result opens the Document Detail view.

### Document Detail

Full document view. Shows:

- Document number, class, version, current lifecycle state
- All attribute values grouped by Attribute Group
- Binary content download button (PHI-logged on click)
- Lifecycle transition history — complete audit trail in chronological order
- Active workflow tasks for this document
- Permitted transition buttons — only transitions legal for the current state
  and the user's role are shown. Illegal transitions do not appear.

### Clinical Review (MEDICAL_DIRECTOR only)

A purpose-built screen for prior authorization clinical review. Shows the
member's details, the requested service, the provider's clinical notes (if
attached as a document attribute), and the decision recording form:

- Decision: APPROVE / DENY / REQUEST_ADDITIONAL_INFO
- Clinical rationale (required text field)
- Approval code (required if approving)

Completing the form triggers task completion and the document lifecycle transition.

---

*Previous: [Chapter 7 — Workflow Engine](07-workflow.md)*
*Next: [Chapter 9 — Integration Guide](09-integration.md)*
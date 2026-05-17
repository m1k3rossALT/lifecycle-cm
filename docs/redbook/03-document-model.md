# Chapter 3 — Document Model

---

## 3.1 Design Philosophy

The document model in LifecycleCM is metadata-first and schema-driven. Unlike
file systems or generic databases, the schema is not defined in code — it is
defined in data. An administrator creates a new Document Class, attaches
Attribute Groups to it, and the system immediately supports ingestion of that
class. No code change. No deployment. No restart.

This is the core extensibility principle of enterprise ECM. Document schemas
evolve. New document types are introduced. Attribute requirements change over
time due to regulatory updates or operational changes. All of this happens
through administration, not engineering. Systems that hardcode document schemas
in application code calcify. Systems that manage schemas as data stay operational
for decades.

---

## 3.2 Attribute Definitions

Every metadata field in LifecycleCM is defined as an `AttributeDefinition`. Each
definition specifies:

| Property | Description |
|---|---|
| `attribute_key` | Machine-readable identifier used in queries, e.g. `member_id` |
| `display_name` | Human-readable label shown in UI |
| `data_type` | One of: STRING, INTEGER, DECIMAL, DATE, DATETIME, BOOLEAN, ENUM_TYPE |
| `is_indexed` | Whether a database index exists on this attribute for fast query |
| `is_required` | Whether a value must be supplied on ingestion |
| `is_phi` | Whether this field contains Protected Health Information |
| `enum_allowed_values` | Array of permitted values when data_type = ENUM_TYPE |
| `validation_regex` | Optional regex applied to STRING values on ingestion |

---

## 3.3 Attribute Groups

An Attribute Group is a named, reusable collection of attribute definitions. The
group exists independently of any Document Class. Multiple Document Classes
reference the same Attribute Group.

**Why reusability matters at scale:** The `member_id` attribute appears on Medical
Claims, Prior Authorization Requests, Explanations of Benefits, Member Enrollment
Forms, and Appeals and Grievances. Defining it once in `MEMBER_ATTRIBUTES` and
attaching it to each class means validation rules, PHI flags, and display names
are managed in one place. When the format of `member_id` changes, one definition
update covers all five document classes.

### MEMBER_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| member_id | Member ID | STRING | Yes | Yes | Yes |
| member_name | Member Name | STRING | Yes | Yes | Yes |
| date_of_birth | Date of Birth | DATE | No | Yes | No |
| plan_id | Plan ID | STRING | Yes | No | Yes |
| group_number | Group Number | STRING | Yes | No | No |

### PROVIDER_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| provider_npi | Provider NPI | STRING | Yes | No | Yes |
| provider_name | Provider Name | STRING | Yes | No | Yes |
| provider_tin | Provider TIN | STRING | No | No | No |
| network_status | Network Status | ENUM_TYPE | Yes | No | Yes |

`network_status` allowed values: `IN_NETWORK`, `OUT_OF_NETWORK`, `PENDING_CREDENTIALING`

### CLAIM_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| claim_number | Claim Number | STRING | Yes | No | Yes |
| service_date_from | Service Date From | DATE | Yes | No | Yes |
| service_date_to | Service Date To | DATE | Yes | No | Yes |
| claim_type | Claim Type | ENUM_TYPE | Yes | No | Yes |
| total_billed_amount | Total Billed Amount | DECIMAL | No | No | No |
| diagnosis_code_primary | Primary Diagnosis Code | STRING | Yes | No | No |

`claim_type` allowed values: `MEDICAL`, `DENTAL`, `VISION`, `PHARMACY`, `BEHAVIORAL_HEALTH`

### AUTHORIZATION_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| auth_number | Authorization Number | STRING | Yes | No | Yes |
| requested_service_code | Requested Service Code | STRING | Yes | No | Yes |
| requested_units | Requested Units | INTEGER | No | No | No |
| urgency_level | Urgency Level | ENUM_TYPE | Yes | No | Yes |
| requesting_provider_npi | Requesting Provider NPI | STRING | Yes | No | Yes |

`urgency_level` allowed values: `STANDARD`, `URGENT`, `EMERGENT`

### PROCESSING_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| received_date | Received Date | DATE | Yes | No | Yes |
| received_channel | Received Channel | ENUM_TYPE | Yes | No | Yes |
| assigned_queue | Assigned Queue | STRING | Yes | No | No |
| processing_region | Processing Region | STRING | Yes | No | No |
| indexer_user_id | Indexed By | STRING | No | No | No |

`received_channel` allowed values: `PORTAL`, `EDI`, `FAX`, `MAIL`, `PHONE`

### CONTRACT_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| contract_number | Contract Number | STRING | Yes | No | Yes |
| effective_date | Effective Date | DATE | Yes | No | Yes |
| termination_date | Termination Date | DATE | Yes | No | No |
| contract_type | Contract Type | ENUM_TYPE | Yes | No | Yes |
| contracting_entity | Contracting Entity | STRING | Yes | No | Yes |

`contract_type` allowed values: `FEE_FOR_SERVICE`, `CAPITATION`, `BUNDLED_PAYMENT`, `VALUE_BASED`

### ENROLLMENT_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| enrollment_effective_date | Enrollment Effective Date | DATE | Yes | No | Yes |
| coverage_type | Coverage Type | ENUM_TYPE | Yes | No | Yes |
| employer_group_id | Employer Group ID | STRING | Yes | No | No |
| enrollment_source | Enrollment Source | ENUM_TYPE | Yes | No | Yes |

`coverage_type` allowed values: `INDIVIDUAL`, `FAMILY`, `EMPLOYEE_ONLY`, `EMPLOYEE_PLUS_SPOUSE`

### APPEAL_ATTRIBUTES

| Attribute Key | Display Name | Type | Indexed | PHI | Required |
|---|---|---|---|---|---|
| appeal_number | Appeal Number | STRING | Yes | No | Yes |
| original_claim_number | Original Claim Number | STRING | Yes | No | No |
| appeal_type | Appeal Type | ENUM_TYPE | Yes | No | Yes |
| appeal_reason_code | Appeal Reason Code | STRING | Yes | No | Yes |
| resolution_date | Resolution Date | DATE | No | No | No |

`appeal_type` allowed values: `FIRST_LEVEL`, `SECOND_LEVEL`, `EXTERNAL_REVIEW`, `EXPEDITED`

---

## 3.4 Document Classes

Each Document Class defines a specific type of healthcare document. It specifies
which Attribute Groups apply, whether the class is PHI-bearing, which retention
policy governs it, and which workflow (if any) is triggered on ingestion.

---

### MEDICAL_CLAIM

**Description:** A formal request by a provider or member for reimbursement of
healthcare services rendered.

**PHI-Bearing:** Yes — all access is HIPAA-audited.

**Attribute Groups:**
1. MEMBER_ATTRIBUTES
2. PROVIDER_ATTRIBUTES
3. CLAIM_ATTRIBUTES
4. PROCESSING_ATTRIBUTES

**Governing Retention Policy:** CMS_MEDICARE_10YR

**Workflow on Ingestion:** CLAIMS_INTAKE

---

### PRIOR_AUTHORIZATION_REQUEST

**Description:** A request submitted by a provider seeking pre-approval for a
specific service, medication, or procedure before it is delivered.

**PHI-Bearing:** Yes

**Attribute Groups:**
1. MEMBER_ATTRIBUTES
2. PROVIDER_ATTRIBUTES
3. AUTHORIZATION_ATTRIBUTES
4. PROCESSING_ATTRIBUTES

**Governing Retention Policy:** HIPAA_PHI_6YR

**Workflow on Ingestion:** PRIOR_AUTH_PROCESSING

---

### EXPLANATION_OF_BENEFITS

**Description:** A summary statement issued to members detailing how a claim was
processed, what amounts were billed, and what was paid by the plan.

**PHI-Bearing:** Yes

**Attribute Groups:**
1. MEMBER_ATTRIBUTES
2. CLAIM_ATTRIBUTES

**Governing Retention Policy:** HIPAA_EOB_6YR

**Workflow on Ingestion:** None (system-generated document, enters at GENERATED state)

---

### MEMBER_ENROLLMENT_FORM

**Description:** The application or change form submitted when a member enrolls
in or modifies their health plan coverage.

**PHI-Bearing:** Yes

**Attribute Groups:**
1. MEMBER_ATTRIBUTES
2. ENROLLMENT_ATTRIBUTES
3. PROCESSING_ATTRIBUTES

**Governing Retention Policy:** ERISA_6YR

**Workflow on Ingestion:** ENROLLMENT_VERIFICATION

---

### APPEAL_AND_GRIEVANCE

**Description:** A formal challenge filed by a member or provider contesting a
claim decision, coverage denial, or quality of care issue.

**PHI-Bearing:** Yes

**Attribute Groups:**
1. MEMBER_ATTRIBUTES
2. APPEAL_ATTRIBUTES
3. PROCESSING_ATTRIBUTES

**Governing Retention Policy:** CMS_APPEAL_6YR

**Workflow on Ingestion:** APPEAL_PROCESSING

---

### PROVIDER_CONTRACT

**Description:** A contractual agreement between the organization and a healthcare
provider or provider group governing payment rates, network participation, and
service terms.

**PHI-Bearing:** No

**Attribute Groups:**
1. PROVIDER_ATTRIBUTES
2. CONTRACT_ATTRIBUTES

**Governing Retention Policy:** SOX_7YR

**Workflow on Ingestion:** CONTRACT_REVIEW

---

## 3.5 The EAV Storage Pattern

Document attribute values are stored using the Entity-Attribute-Value pattern.
Rather than a fixed column per attribute, each attribute value is a separate row
in the `document_attribute` table, referencing its definition and holding its
value in the typed column that matches the definition's `data_type`.

```
document_attribute
  document_id              → FK to document
  attribute_definition_id  → FK to attribute_definition
  str_value                → populated when data_type = STRING or ENUM_TYPE
  int_value                → populated when data_type = INTEGER
  decimal_value            → populated when data_type = DECIMAL
  date_value               → populated when data_type = DATE
  datetime_value           → populated when data_type = DATETIME
  bool_value               → populated when data_type = BOOLEAN
```

Only one value column is populated per row. The others are null.

**Why EAV here:** Document Classes are user-configurable at runtime. It is not
possible to have a fixed column per attribute when new Document Classes and new
Attribute Definitions can be created by an administrator without a code
deployment. The EAV trade-off — more complex queries — is the correct trade-off
for a system where schema extensibility is a core functional requirement.

Queries filter on indexed `(attribute_key, str_value)` pairs. PostgreSQL partial
indexes on the value columns make this performant at operational data volumes.

---

## 3.6 Document Versioning

Every document is versioned. Version labels follow a defined scheme:

- `1.0` — initial ingestion
- `1.1` — minor revision (metadata correction, attribute value update)
- `2.0` — major revision (content replacement, e.g. corrected claim submission)

When a new version is created, the previous version record has `is_current_version`
set to false and `superseded_by_id` set to the new version's document ID. The
previous version's audit trail, lifecycle history, and attribute values are
permanently preserved and unchanged. Versioning is additive. Nothing is deleted
or overwritten.

---

## 3.7 Document Numbering

Every document is assigned a system-generated document number at creation:

```
DOC-{YYYY}-{7-digit-sequence}
Example: DOC-2025-0000441
```

This number is the primary external reference for the document. It is what an
integrating system stores when it submits a document to LifecycleCM. It is what
appears in correspondence, audit trails, and workflow tasks.

---

*Previous: [Chapter 2 — Architecture Overview](02-architecture.md)*
*Next: [Chapter 4 — Content Lifecycle Management](04-lifecycle.md)*
# Chapter 2 — System Architecture Overview

---

## 2.1 The Two-Tier Repository

LifecycleCM is composed of two independently deployed backend services. This is
not a microservices trend choice. It is a deliberate replication of the
architectural separation that has made enterprise ECM platforms durable across
decades of operational use.

**ecm-library-service** is the Library Server. It owns all metadata. It knows
what documents exist, what class they belong to, what their attributes are, what
state they are in, who can access them, and what retention schedule governs them.
It stores no binary content. It holds a reference — a `content_reference_id` —
that points to where binary content lives.

**ecm-resource-manager** is the Resource Manager. It owns all binary content. It
knows nothing about document classes, retention policies, or user roles. It
accepts content objects, stores them in MinIO with a SHA-256 checksum, and
returns them on authenticated request. It trusts the Library Service to handle
all authorization decisions before calling it.

**Why this separation matters operationally:**

The metadata store can be queried, indexed, and optimized independently of binary
storage. A search across ten million document records touches only the Library
Service database. The Resource Manager is not involved until a user explicitly
requests content download.

Binary storage can be scaled, tiered, or migrated without touching the metadata
model. If the storage backend changes from local MinIO to a distributed object
store, the Library Service is completely unaffected.

A document's metadata outlives its content. When content is disposed, the document
record, its attribute values, its lifecycle history, and its complete audit trail
are retained permanently. The system can always prove a document existed, what it
contained by attribute, how it was processed, and when and why it was destroyed.

---

## 2.2 Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          LifecycleCM System                             │
│                                                                         │
│  ┌──────────────────┐          ┌──────────────────┐                     │
│  │   ecm-admin      │          │   ecm-client     │                     │
│  │  Admin Console   │          │  Operations UI   │                     │
│  │  (React / :3000) │          │  (React / :3001) │                     │
│  └────────┬─────────┘          └────────┬─────────┘                     │
│           │  REST / HTTPS / JWT         │  REST / HTTPS / JWT           │
│           └──────────────┬──────────────┘                               │
│                          │                                              │
│             ┌────────────▼────────────┐                                 │
│             │   ecm-library-service   │  port 8080                      │
│             │     (Spring Boot 3)     │                                 │
│             │                         │                                 │
│             │  DocumentService        │                                 │
│             │  LifecycleService       │                                 │
│             │  RetentionEngine        │                                 │
│             │  AccessControlService   │                                 │
│             │  WorkflowService        │                                 │
│             │  AuditLogger            │                                 │
│             └────────────┬────────────┘                                 │
│                          │                                              │
│              ┌───────────┴────────────┐                                 │
│              │                        │                                 │
│   ┌──────────▼──────────┐  ┌──────────▼──────────┐                      │
│   │     PostgreSQL      │  │  ecm-resource-mgr   │  port 8081           │
│   │   (Library DB)      │  │   (Spring Boot 3)   │                      │
│   │      port 5432      │  │                     │                      │
│   │                     │  │  ContentStoreService│                      │
│   │  All metadata       │  │  ChecksumVerify     │                      │
│   │  All policies       │  │  StorageTiering     │                      │
│   │  All audit logs     │  └──────────┬──────────┘                      │
│   └─────────────────────┘             │                                 │
│                                ┌───────┴────────┐                       │
│                                │                │                       │
│                      ┌─────────▼──────┐  ┌───────▼───────┐              │
│                      │  PostgreSQL    │  │     MinIO     │              │
│                      │ (Resource DB)  │  │  port 9000    │              │
│                      │  port 5433     │  │  Binary store │              │
│                      │ content_object │  │  S3-compatible│              │
│                      └────────────────┘  └───────────────┘              │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2.3 Technology Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java | 21 LTS | Backend services |
| Framework | Spring Boot | 3.3.x | Application framework |
| State Machine | Spring Statemachine | 4.x | Lifecycle enforcement |
| Security | Spring Security + JJWT | 6.x | Auth and authorization |
| ORM | Spring Data JPA + Hibernate | 6.x | Database access |
| DB Migration | Flyway | 10.x | Schema versioning and seeding |
| Validation | Jakarta Bean Validation | 3.x | Input validation |
| Mapping | MapStruct | 1.6.x | DTO to entity mapping |
| HTTP Client | Spring RestClient | — | Library → RM internal calls |
| Scheduler | Spring @Scheduled | — | Retention engine nightly job |
| Build | Maven | 3.9.x | Multi-module build |
| Binary Storage | MinIO Java SDK | 8.x | S3-compatible local storage |
| Database | PostgreSQL | 16 | Relational metadata store |
| Frontend | React + Vite | 18 / 5 | Both UI clients |
| UI Components | Shadcn/ui | Latest | Component library |
| API State | TanStack Query | 5.x | Frontend API management |
| Containers | Docker + Compose | — | Local deployment |
| API Docs | SpringDoc OpenAPI | 2.x | Swagger UI generation |

---

## 2.4 Maven Module Structure

The project is a single Maven multi-module build. All modules are built from
one parent POM invocation.

```
lifecycle-cm/
├── pom.xml                      ← Parent POM: dependency management, plugins
│
├── ecm-common/                  ← Shared library. No Spring Boot. No main class.
│   └── src/main/java/
│       └── com/ecm/common/
│           ├── dto/             ← Request/response DTOs shared across services
│           ├── enums/           ← LifecycleState, DocumentClassCode, RoleCode...
│           ├── exception/       ← EcmException hierarchy
│           └── event/           ← Domain event interfaces
│
├── ecm-library-service/         ← Library Server. Primary system of record.
│   └── src/main/java/
│       └── com/ecm/library/
│           ├── config/          ← Spring Security, Statemachine, Flyway, Beans
│           ├── domain/          ← JPA entities, repositories
│           ├── service/         ← Business logic interfaces and implementations
│           ├── api/             ← REST controllers
│           ├── client/          ← HTTP client to Resource Manager
│           ├── event/           ← Domain event listeners
│           └── scheduler/       ← Retention engine, SLA scanner
│
├── ecm-resource-manager/        ← Resource Manager. Binary content only.
│   └── src/main/java/
│       └── com/ecm/rm/
│           ├── config/          ← MinIO client bean, Flyway
│           ├── domain/          ← ContentObject entity and repository
│           ├── service/         ← ContentStoreService, StorageClassService
│           └── api/             ← Content upload, download, delete endpoints
│
├── ecm-admin/                   ← System Administration Console (React)
│   └── src/
│       ├── pages/               ← Health, DocClasses, Users, Retention, Audit
│       └── components/
│
├── ecm-client/                  ← Operations Client (React)
│   └── src/
│       ├── pages/               ← Inbox, Search, DocumentDetail, PriorAuth
│       └── components/
│
└── docker/
    ├── docker-compose.yml
    └── init/
        └── postgres-init.sql    ← Creates both databases on first start
```

---

## 2.5 Request Flow — Document Ingestion

The following describes what happens, step by step, when a claims processor
submits a new Medical Claim document through the operations client.

**Step 1.** The client sends a multipart HTTP POST to `ecm-library-service`
containing a JSON metadata block and the binary content file. The request
carries a JWT Bearer token.

**Step 2.** The auth filter validates the JWT, loads the user's roles and scope,
and populates the SecurityContext.

**Step 3.** `AccessControlService` confirms the user holds `can_create` permission
for the `MEDICAL_CLAIM` document class. If not, a 403 is returned immediately.

**Step 4.** `DocumentAttributeService` validates every supplied attribute value
against the attribute definition schema for the MEDICAL_CLAIM class: required
fields present, data types correct, enum values within allowed set.

**Step 5.** The Library Service forwards the binary content to
`ecm-resource-manager` via internal HTTP. The Resource Manager stores the file
in MinIO under the path `/{class_code}/{year}/{month}/{uuid}`, computes a
SHA-256 checksum, persists a `content_object` record, and returns the
`content_reference_id`.

**Step 6.** The Library Service creates the `document` record with
`lifecycle_state = RECEIVED`, stores all attribute values as rows in
`document_attribute`, and writes the initial transition record to
`lifecycle_transition` (null → RECEIVED, system-triggered).

**Step 7.** A `DocumentReceivedEvent` is published internally.
`WorkflowEventListener` receives this event, looks up the workflow definition for
MEDICAL_CLAIM, and creates a `workflow_task` in step 1 of the CLAIMS_INTAKE
workflow, assigned to the CLAIMS_PROCESSOR role queue.

**Step 8.** `AuditLogger` writes a structured log line recording the ingestion,
the document number assigned, the user, the correlation ID, and the duration.

**Step 9.** The Library Service returns HTTP 201 with the new document number,
assigned document class, initial lifecycle state, and the workflow task ID to the
client.

---

## 2.6 Request Flow — Document Search

**Step 1.** The user submits a search: class `MEDICAL_CLAIM`, attribute
`member_id = M-10041`, state `APPROVED`.

**Step 2.** The scope filter is applied. A `CLAIMS_PROCESSOR` with
`scope_type = OWN_QUEUE` sees only documents in queues assigned to them. A
`COMPLIANCE_OFFICER` with `scope_type = ALL` sees the full result set.

**Step 3.** The query executes against `document` joined to `document_attribute`
on the indexed attribute key and string value columns. PostgreSQL partial indexes
on `(attribute_key, str_value)` keep this performant.

**Step 4.** Matching documents are assembled into metadata summary DTOs. No binary
content is returned in search responses.

**Step 5.** The PHI interceptor inspects each result. If any document has
`is_phi_bearing = true`, the entire response is marked as a PHI_ACCESS event and
a `phi_access_log` record is written for the requesting user.

**Step 6.** Paginated results are returned to the client.

---

## 2.7 Internal Communication

The Library Service calls the Resource Manager over HTTP using Spring's
`RestClient`. All internal calls are on the Docker Compose internal network and
are not exposed externally. The Resource Manager has no public-facing
authentication — it trusts that only the Library Service can reach it on the
internal network. Content authorization decisions are made by the Library Service
before it calls the Resource Manager.

---

## 2.8 Startup Sequence

Docker Compose manages dependencies via health check conditions. The correct
startup order is enforced automatically.

```
1. postgres-library     → must be healthy
2. postgres-resource    → must be healthy
3. minio                → must be healthy
4. ecm-resource-manager → starts after postgres-resource and minio are healthy
5. ecm-library-service  → starts after postgres-library and ecm-resource-manager are healthy
6. ecm-admin            → starts after ecm-library-service is healthy
7. ecm-client           → starts after ecm-library-service is healthy
```

---

*Previous: [Chapter 1 — Introduction](01-introduction.md)*
*Next: [Chapter 3 — Document Model](03-document-model.md)*
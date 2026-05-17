# LifecycleCM

**Enterprise Content Management for Healthcare**

A governed document repository that manages the complete lifecycle of healthcare
documents — from ingestion through processing, archival, retention enforcement,
and final disposition. Built on the architectural principles of enterprise ECM
platforms: metadata-driven classification, state machine lifecycle enforcement,
HIPAA-compliant access control, policy-based retention governance, and
workflow-driven processing.

---

## What It Does

Healthcare organizations manage millions of documents annually. Medical claims,
prior authorization requests, explanations of benefits, member enrollment forms,
provider contracts, and appeals — each carrying distinct regulatory retention
obligations and access restrictions.

Generic file storage fails this environment for four reasons: folder-based
retrieval is not auditable or enforceable, there is no lifecycle state
enforcement, there is no retention policy engine, and there is no PHI access
audit trail.

LifecycleCM treats every document as a governed object, not a file.

A document has a **class** — a typed schema defining its metadata attributes. It
has a **lifecycle state** — a position in an enforced state machine that governs
what can happen to it and who can act on it. It has a **retention schedule** —
a computed disposition date derived from the regulatory policy governing its
document class. And it has an **audit trail** — a permanent, immutable record of
every action taken on it, by whom, and when.

Nothing in this system is folder-based. Everything is metadata-driven and
policy-enforced.

---

## Architecture

Two independently deployed backend services. Two purpose-built frontend clients.

```
ecm-admin (React :3000)          ecm-client (React :3001)
System Administration Console    Operations Client

         │                                │
         └────────────┬───────────────────┘
                      │ REST / JWT
          ┌───────────▼───────────┐
          │  ecm-library-service  │  :8080
          │    (Spring Boot 3)    │
          │                       │
          │  Document Model       │  Metadata-driven classification
          │  Lifecycle Engine     │  Spring Statemachine enforcement
          │  Retention Engine     │  Nightly policy evaluation
          │  Access Control       │  HIPAA-compliant RBAC + PHI audit
          │  Workflow Engine      │  Task-based process integration
          └───────────┬───────────┘
                      │ Internal HTTP
          ┌───────────▼───────────┐
          │  ecm-resource-manager │  :8081
          │    (Spring Boot 3)    │
          │                       │
          │  Content Store        │  Binary object storage
          │  Checksum Verify      │  SHA-256 integrity check
          │  Storage Tiering      │  ACTIVE / NEARLINE / ARCHIVE
          └───────────┬───────────┘
               ┌──────┴──────┐
          PostgreSQL        MinIO
          (metadata)     (binary content)
```

Full architecture documentation: [docs/redbook/02-architecture.md](docs/redbook/02-architecture.md)

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 LTS |
| Framework | Spring Boot 3.3, Spring Statemachine 4 |
| Security | Spring Security 6, JWT (JJWT) |
| Database | PostgreSQL 16 |
| DB Migration | Flyway 10 |
| Binary Storage | MinIO (S3-compatible, local Docker) |
| Build | Maven 3.9 (multi-module) |
| Frontend | React 18, Vite, Shadcn/ui, TanStack Query |
| Containers | Docker, Docker Compose |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |

---

## Key Capabilities

**Metadata-driven document classification.** Documents are instances of typed
Document Classes with structured attribute schemas. Six healthcare document types
are pre-configured: Medical Claims, Prior Authorization Requests, Explanations of
Benefits, Member Enrollment Forms, Appeals and Grievances, and Provider Contracts.

**State machine lifecycle enforcement.** Every document moves through defined
lifecycle states via legal transitions only. Illegal transitions are rejected at
the framework level. Every transition is permanently recorded.

**HIPAA-compliant access control.** Seven roles with scope-based query filtering.
Every access to PHI-bearing documents is automatically logged with accessor
identity, access type, timestamp, and IP address.

**Retention policy engine.** Six pre-configured retention policies mapped to
federal regulations (HIPAA, CMS, ERISA, SOX). Nightly scheduled engine computes
eligibility dates, enforces most-restrictive policy when multiple apply, and
respects legal hold blocks. Disposition requires Compliance Officer approval.

**Legal hold management.** Litigation, regulatory, and internal holds suspend
retention and disposition regardless of schedule. Hold placement and release are
fully audited.

**Workflow-driven processing.** Document state transitions create and resolve work
items. Five pre-configured workflows cover all document classes. Task queue with
SLA tracking, explicit claim model, and escalation on breach.

**Two-tier repository.** Library Service (metadata) and Resource Manager (binary
content) are independently deployed services, mirroring the architectural
separation of enterprise ECM platforms.

---

## Quick Start

**Prerequisites:** Docker Desktop, Git

```bash
git clone https://github.com/YOUR_USERNAME/lifecycle-cm.git
cd lifecycle-cm/docker

# Copy and configure environment file
cp .env.example .env
# Edit .env — set database passwords, JWT secret, MinIO credentials

# Start the full stack
docker compose up -d

# Wait for all services to report healthy
docker compose ps
```

Verify both backend services are up:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

| Interface | URL | Role |
|---|---|---|
| Operations Client | http://localhost:3001 | claims_processor / changeme |
| Admin Console | http://localhost:3000 | system_admin / changeme |
| Library Service API | http://localhost:8080/swagger-ui.html | Any seeded user |
| Resource Manager API | http://localhost:8081/swagger-ui.html | Internal |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |

All seed user accounts use password `changeme`. See
[Chapter 10](docs/redbook/10-deployment.md) for the full account list.

---

## Documentation

| Document | Description |
|---|---|
| [Chapter 1 — Introduction](docs/redbook/01-introduction.md) | Problem statement, ECM concept mapping, domain overview |
| [Chapter 2 — Architecture](docs/redbook/02-architecture.md) | Component diagram, request flows, technology stack |
| [Chapter 3 — Document Model](docs/redbook/03-document-model.md) | Document Classes, Attribute Groups, healthcare taxonomy |
| [Chapter 4 — Lifecycle Management](docs/redbook/04-lifecycle.md) | State machine, transition rules, audit trail |
| [Chapter 5 — Retention and Disposition](docs/redbook/05-retention.md) | Policy model, retention engine, legal holds, disposition |
| [Chapter 6 — Access Control](docs/redbook/06-access-control.md) | Roles, HIPAA PHI logging, scope filtering, JWT |
| [Chapter 7 — Workflow Engine](docs/redbook/07-workflow.md) | Prior auth workflow, task queue, SLA tracking |
| [Chapter 8 — System Administration](docs/redbook/08-administration.md) | Admin Console and Operations Client reference |
| [Chapter 9 — Integration Guide](docs/redbook/09-integration.md) | REST API reference, request/response examples |
| [Chapter 10 — Deployment](docs/redbook/10-deployment.md) | Startup sequence, configuration, health monitoring |

---

## Project Structure

```
lifecycle-cm/
├── ecm-common/              Shared enums, DTOs, exceptions, domain events
├── ecm-library-service/     Library Server — metadata, lifecycle, retention, workflow
├── ecm-resource-manager/    Resource Manager — binary content storage
├── ecm-admin/               System Administration Console (React)
├── ecm-client/              Operations Client (React)
├── docker/                  Docker Compose, init scripts, environment template
└── docs/
    └── redbook/             10-chapter technical reference documentation
```

---

## License

MIT — see [LICENSE](LICENSE)
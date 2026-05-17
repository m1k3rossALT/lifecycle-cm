# Chapter 10 — Deployment and Operations Reference

---

## 10.1 Prerequisites

The following must be installed on the host machine before LifecycleCM can be
started.

| Prerequisite | Minimum Version | Purpose |
|---|---|---|
| Docker Desktop | 4.x | Container runtime |
| Docker Compose | 2.x (included with Docker Desktop) | Multi-container orchestration |
| Git | 2.x | Repository clone |
| Java 21 JDK | 21 LTS | Required for local development only |
| Maven | 3.9.x | Required for local development only |
| Node.js | 20 LTS | Required for local frontend development only |

For running the full stack via Docker Compose only, Java, Maven, and Node.js are
not required on the host.

---

## 10.2 Component Inventory

| Component | Type | Port | Description |
|---|---|---|---|
| ecm-library-service | Spring Boot application | 8080 | Primary Library Server |
| ecm-resource-manager | Spring Boot application | 8081 | Binary content Resource Manager |
| postgres-library | PostgreSQL 16 container | 5432 | Library Service database |
| postgres-resource | PostgreSQL 16 container | 5433 | Resource Manager database |
| minio | MinIO container | 9000 (API), 9001 (console) | Binary object storage |
| ecm-admin | React application | 3000 | System Administration Console |
| ecm-client | React application | 3001 | Operations Client |

---

## 10.3 Environment Configuration

All environment-specific values are supplied as environment variables. No
configuration is hardcoded in application code. A `.env` file in the `docker/`
directory is read by Docker Compose at startup.

**Library Service variables:**

| Variable | Description | Example |
|---|---|---|
| `LIBRARY_DB_URL` | JDBC URL for Library database | `jdbc:postgresql://postgres-library:5432/ecm_library_db` |
| `LIBRARY_DB_USERNAME` | Database username | `ecm_user` |
| `LIBRARY_DB_PASSWORD` | Database password | (set in .env) |
| `JWT_SECRET` | HMAC-SHA256 signing secret (min 32 chars) | (set in .env) |
| `JWT_EXPIRY_HOURS` | Token validity in hours | `24` |
| `RM_BASE_URL` | Resource Manager internal URL | `http://ecm-resource-manager:8081` |
| `RETENTION_SCAN_CRON` | Cron expression for nightly retention scan | `0 0 2 * * *` |
| `SLA_SCAN_CRON` | Cron expression for SLA breach scanner | `0 */15 * * * *` |

**Resource Manager variables:**

| Variable | Description | Example |
|---|---|---|
| `RESOURCE_DB_URL` | JDBC URL for Resource database | `jdbc:postgresql://postgres-resource:5433/ecm_resource_db` |
| `RESOURCE_DB_USERNAME` | Database username | `ecm_rm_user` |
| `RESOURCE_DB_PASSWORD` | Database password | (set in .env) |
| `MINIO_ENDPOINT` | MinIO API endpoint | `http://minio:9000` |
| `MINIO_ACCESS_KEY` | MinIO access key | (set in .env) |
| `MINIO_SECRET_KEY` | MinIO secret key | (set in .env) |
| `MINIO_BUCKET_NAME` | Bucket for content objects | `ecm-content` |

**Never commit the `.env` file to version control.** A `.env.example` file with
placeholder values is committed instead. The `.gitignore` excludes `.env`.

---

## 10.4 Startup Sequence

Docker Compose enforces the startup sequence using `depends_on` with health
check conditions. Each service waits for its dependencies to report healthy
before starting.

```
1. postgres-library    Waits for: nothing. Health check: pg_isready
2. postgres-resource   Waits for: nothing. Health check: pg_isready
3. minio               Waits for: nothing. Health check: HTTP GET /minio/health/live
4. ecm-resource-mgr    Waits for: postgres-resource (healthy), minio (healthy)
5. ecm-library-service Waits for: postgres-library (healthy), ecm-resource-mgr (healthy)
6. ecm-admin           Waits for: ecm-library-service (healthy)
7. ecm-client          Waits for: ecm-library-service (healthy)
```

On first startup, Flyway migrations run automatically in both backend services
before they begin accepting requests. Seed data is applied as part of the
migration sequence.

---

## 10.5 First-Time Startup

```bash
git clone https://github.com/YOUR_USERNAME/lifecycle-cm.git
cd lifecycle-cm/docker

# Copy the example environment file and set your secrets
cp .env.example .env
# Edit .env and set: LIBRARY_DB_PASSWORD, RESOURCE_DB_PASSWORD,
#                    JWT_SECRET, MINIO_ACCESS_KEY, MINIO_SECRET_KEY

# Start the full stack
docker compose up -d

# Monitor startup (wait for all services to show healthy)
docker compose ps

# Verify both backend services are up
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

Expected health response for a fully started Library Service:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "resourceManager": { "status": "UP" },
    "retentionEngine": {
      "status": "UP",
      "lastRunAt": "startup",
      "nextRunAt": "2025-05-18T02:00:00Z"
    }
  },
  "version": "1.0.0"
}
```

---

## 10.6 Default Seed Accounts

The following user accounts are created by the seed migration. **Change all
passwords before any non-local deployment.**

| Username | Role | Scope | Default Password |
|---|---|---|---|
| system_admin | SYSTEM_ADMIN | ALL | changeme |
| compliance_officer | COMPLIANCE_OFFICER | ALL | changeme |
| medical_director | MEDICAL_DIRECTOR | ALL | changeme |
| claims_processor | CLAIMS_PROCESSOR | OWN_QUEUE | changeme |
| member_services | MEMBER_SERVICES_REP | MEMBER_SCOPED | changeme |
| provider_relations | PROVIDER_RELATIONS | PROVIDER_SCOPED | changeme |
| auditor | AUDITOR | ALL | changeme |

---

## 10.7 Health Monitoring

Every backend service exposes Spring Actuator health and info endpoints.

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Overall health including DB and RM connectivity |
| `GET /actuator/info` | Service version, build time, active Spring profiles |
| `GET /actuator/metrics` | Micrometer metrics (request counts, latencies, custom ECM metrics) |

Custom metrics exposed by the Library Service:

| Metric Name | Description |
|---|---|
| `ecm.documents.ingested.total` | Total documents ingested since startup |
| `ecm.transitions.total` | Total lifecycle transitions executed |
| `ecm.transitions.denied.total` | Total denied transition attempts |
| `ecm.retention.flagged.total` | Total documents flagged eligible by Retention Engine |
| `ecm.workflow.tasks.open` | Current count of open workflow tasks |
| `ecm.workflow.tasks.escalated` | Current count of escalated (SLA breached) tasks |

---

## 10.8 Structured Log Format

All log output follows the structured format defined in the AuditLogger. Every
line is machine-parseable and human-readable.

```
[TIMESTAMP] [LEVEL] [SERVICE] [CORRELATION-ID] [USER-ID] [OPERATION] [ENTITY-TYPE] [ENTITY-ID] [MESSAGE] [DURATION-MS?]
```

**Examples:**

```
2025-05-16T09:11:00.441Z INFO  library-service a3f9c812 user:U-0041   INGEST     doc:MEDICAL_CLAIM    doc:DOC-2025-0000441  Document ingested                          duration=34ms
2025-05-16T09:11:00.501Z INFO  library-service a3f9c812 user:U-0041   TRANSITION doc:MEDICAL_CLAIM    doc:DOC-2025-0000441  State RECEIVED→INDEXED                     duration=12ms
2025-05-16T09:11:00.502Z INFO  library-service a3f9c812 user:U-0041   PHI_ACCESS doc:MEDICAL_CLAIM    doc:DOC-2025-0000441  Content downloaded role=CLAIMS_PROCESSOR
2025-05-17T02:00:04.002Z WARN  retention-engine SYSTEM  scheduler     RETENTION  doc:MEDICAL_CLAIM    doc:DOC-2024-0000018  Eligible for disposition policy=CMS_MEDICARE_10YR trigger=2015-03-01
2025-05-16T09:11:01.009Z ERROR library-service  b7d1e293 user:U-0019  TRANSITION doc:PROVIDER_CONTRACT doc:DOC-2025-0000089  Denied ARCHIVED→DISPOSED reason=ACTIVE_LEGAL_HOLD hold=LH-2025-LIT-0041
```

The correlation ID on every line is the request-scoped trace ID. All log lines
from a single API request share the same correlation ID, enabling full request
tracing by filtering on that value.

---

## 10.9 Common Operations

**View logs for a specific document:**
```bash
docker compose logs ecm-library-service | grep "DOC-2025-0000441"
```

**View all denied transitions:**
```bash
docker compose logs ecm-library-service | grep "TRANSITION_DENIED"
```

**View the last Retention Engine run:**
```bash
docker compose logs ecm-library-service | grep "retention-engine"
```

**Connect to the Library database for direct inspection:**
```bash
docker compose exec postgres-library psql -U ecm_user -d ecm_library_db
```

**Force a Retention Engine run immediately (without waiting for schedule):**

The Retention Engine exposes an admin-only trigger endpoint:
```bash
curl -X POST http://localhost:8080/api/v1/admin/retention/run-now \
  -H "Authorization: Bearer <system_admin_token>"
```

**Reset the full stack to clean state:**
```bash
docker compose down -v
docker compose up -d
```
Note: This destroys all data including MinIO content and both databases.
Flyway migrations and seed data are re-applied automatically on restart.

---

## 10.10 Rollback Procedure

If a deployment introduces a defect requiring rollback:

1. `docker compose down` — stop all containers.
2. Check out the previous Git tag: `git checkout v{previous-version}`.
3. Rebuild images: `docker compose build ecm-library-service ecm-resource-manager`.
4. Start the stack: `docker compose up -d`.

Flyway handles schema rollback considerations automatically — all migrations are
forward-only. If a migration in the defective version created schema changes that
must be reverted, a compensating migration is applied in the next forward version.
Schema is never rolled back by removing migration files.

---

*Previous: [Chapter 9 — Integration Guide](09-integration.md)*
*Return to: [README](../../README.md)*
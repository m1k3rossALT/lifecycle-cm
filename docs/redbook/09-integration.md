# Chapter 9 — Integration Guide

---

## 9.1 Integration Philosophy

LifecycleCM exposes a REST API over HTTP. Every operation available in the two
frontend clients is available through the API. There is no privileged back-channel
for integrating systems — they authenticate with a service account JWT and call
the same endpoints the UI clients use.

This is deliberate. An ECM platform that exposes different surfaces to different
callers creates maintenance and security complexity. One API, one auth mechanism,
one access control model — regardless of whether the caller is a browser, a
mobile app, or an upstream claims processing system.

All request and response bodies are JSON. Binary content is transferred as
multipart form data on ingestion and as an octet-stream on download.

Full interactive API documentation is available at:
- Library Service: `http://localhost:8080/swagger-ui.html`
- Resource Manager: `http://localhost:8081/swagger-ui.html`

External systems integrate with the Library Service only. The Resource Manager
API is internal.

---

## 9.2 Authentication

All API requests must carry a valid JWT Bearer token in the Authorization header.

```
Authorization: Bearer <token>
```

**Obtaining a token:**

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "svc_claims_system",
  "password": "..."
}
```

Response:

```json
{
  "token": "eyJhbGci...",
  "expiresAt": "2025-05-18T09:00:00Z",
  "userId": "U-0099",
  "roles": ["CLAIMS_PROCESSOR"]
}
```

Service accounts are standard user accounts with a system-purpose role assignment.
They are created and managed in the Administration Console. Token expiry is 24
hours. Integrating systems should implement token refresh before expiry by
re-authenticating.

---

## 9.3 Document Ingestion

Submit a new document to LifecycleCM. The document is assigned to the specified
Document Class, attribute values are validated, content is stored, and the
initial workflow task is created.

**Request:**

```
POST /api/v1/documents
Authorization: Bearer <token>
Content-Type: multipart/form-data

metadata: {
  "documentClassCode": "MEDICAL_CLAIM",
  "attributes": {
    "member_id": "M-10041",
    "provider_npi": "1234567890",
    "claim_number": "CLM-2025-00881",
    "service_date_from": "2025-05-01",
    "service_date_to": "2025-05-01",
    "claim_type": "MEDICAL",
    "received_date": "2025-05-15",
    "received_channel": "EDI"
  }
}
content: <binary file>
```

**Successful response (201 Created):**

```json
{
  "documentNumber": "DOC-2025-0000441",
  "documentClassCode": "MEDICAL_CLAIM",
  "lifecycleState": "RECEIVED",
  "createdAt": "2025-05-15T14:22:10Z",
  "workflowTaskId": "WFT-0000881",
  "contentReferenceId": "a3f9c812-..."
}
```

**Validation error response (400 Bad Request):**

```json
{
  "error": "ATTRIBUTE_VALIDATION_FAILED",
  "message": "Required attribute 'claim_number' is missing",
  "invalidAttributes": ["claim_number"]
}
```

---

## 9.4 Document Search

Query documents by class and attribute values. Results are filtered by the
authenticated user's role and scope automatically.

**Request:**

```
POST /api/v1/documents/search
Authorization: Bearer <token>
Content-Type: application/json

{
  "documentClassCode": "MEDICAL_CLAIM",
  "filters": [
    { "attributeKey": "member_id", "value": "M-10041" },
    { "attributeKey": "claim_type", "value": "MEDICAL" }
  ],
  "lifecycleStates": ["APPROVED", "ARCHIVED"],
  "page": 0,
  "pageSize": 20,
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

**Response (200 OK):**

```json
{
  "totalResults": 3,
  "page": 0,
  "pageSize": 20,
  "documents": [
    {
      "documentNumber": "DOC-2025-0000441",
      "documentClassCode": "MEDICAL_CLAIM",
      "lifecycleState": "APPROVED",
      "createdAt": "2025-05-15T14:22:10Z",
      "keyAttributes": {
        "member_id": "M-10041",
        "claim_number": "CLM-2025-00881",
        "claim_type": "MEDICAL"
      }
    }
  ]
}
```

Note: PHI access is logged for all results when the Document Class is
PHI-bearing, even when only metadata is returned.

---

## 9.5 Document Retrieval

Retrieve the full metadata record for a specific document by its document number.

**Request:**

```
GET /api/v1/documents/DOC-2025-0000441
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "documentNumber": "DOC-2025-0000441",
  "documentClassCode": "MEDICAL_CLAIM",
  "lifecycleState": "APPROVED",
  "version": "1.0",
  "isCurrentVersion": true,
  "containsPhi": true,
  "legalHoldActive": false,
  "createdAt": "2025-05-15T14:22:10Z",
  "createdBy": "U-0041",
  "lastModifiedAt": "2025-05-16T09:11:00Z",
  "attributes": {
    "member_id": "M-10041",
    "provider_npi": "1234567890",
    "claim_number": "CLM-2025-00881",
    "service_date_from": "2025-05-01",
    "service_date_to": "2025-05-01",
    "claim_type": "MEDICAL",
    "received_date": "2025-05-15",
    "received_channel": "EDI"
  }
}
```

---

## 9.6 Content Download

Download the binary content of a document. PHI access is logged automatically
for PHI-bearing documents.

**Request:**

```
GET /api/v1/documents/DOC-2025-0000441/content
Authorization: Bearer <token>
```

**Response (200 OK):**

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="DOC-2025-0000441.pdf"
Content-Length: 204800

<binary stream>
```

**Error — document disposed (410 Gone):**

```json
{
  "error": "CONTENT_DISPOSED",
  "message": "Content for DOC-2025-0000441 has been disposed per retention policy CMS_MEDICARE_10YR on 2025-03-01. Metadata record is permanently preserved.",
  "disposedAt": "2025-03-01T02:15:44Z"
}
```

---

## 9.7 Lifecycle Transition

Trigger a lifecycle state transition on a document programmatically.

**Request:**

```
POST /api/v1/documents/DOC-2025-0000441/transition
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetState": "APPROVED",
  "note": "Claim adjudicated. Payment authorized. Auth: AUTH-2025-009921."
}
```

**Successful response (200 OK):**

```json
{
  "documentNumber": "DOC-2025-0000441",
  "previousState": "UNDER_REVIEW",
  "newState": "APPROVED",
  "transitionedAt": "2025-05-16T09:11:00Z",
  "triggeredBy": "U-0041"
}
```

**Error — transition denied (403 Forbidden):**

```json
{
  "error": "TRANSITION_DENIED",
  "fromState": "RECEIVED",
  "toState": "APPROVED",
  "reason": "ILLEGAL_TRANSITION",
  "message": "Transition RECEIVED → APPROVED is not a defined transition for document class MEDICAL_CLAIM."
}
```

**Error — legal hold (403 Forbidden):**

```json
{
  "error": "TRANSITION_DENIED",
  "fromState": "ARCHIVED",
  "toState": "ELIGIBLE_FOR_DISPOSITION",
  "reason": "ACTIVE_LEGAL_HOLD",
  "holdReference": "LH-2025-LIT-0041",
  "message": "Document is subject to active legal hold LH-2025-LIT-0041. Disposition is blocked."
}
```

---

## 9.8 Lifecycle Transition History

Retrieve the complete transition audit trail for a document.

**Request:**

```
GET /api/v1/documents/DOC-2025-0000441/transitions
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "documentNumber": "DOC-2025-0000441",
  "transitions": [
    {
      "fromState": null,
      "toState": "RECEIVED",
      "triggeredBy": null,
      "systemTriggered": true,
      "note": "Initial ingestion",
      "transitionedAt": "2025-05-15T14:22:10Z"
    },
    {
      "fromState": "RECEIVED",
      "toState": "INDEXED",
      "triggeredBy": "U-0041",
      "systemTriggered": false,
      "note": "Member verified. Provider credentialed. Claim complete.",
      "transitionedAt": "2025-05-15T16:04:22Z"
    },
    {
      "fromState": "INDEXED",
      "toState": "UNDER_REVIEW",
      "triggeredBy": "U-0041",
      "systemTriggered": false,
      "note": null,
      "transitionedAt": "2025-05-15T16:05:01Z"
    },
    {
      "fromState": "UNDER_REVIEW",
      "toState": "APPROVED",
      "triggeredBy": "U-0019",
      "systemTriggered": false,
      "note": "Claim adjudicated. Payment authorized.",
      "transitionedAt": "2025-05-16T09:11:00Z"
    }
  ]
}
```

---

## 9.9 Retention Schedule Query

Retrieve the computed retention schedule for a document.

**Request:**

```
GET /api/v1/documents/DOC-2025-0000441/retention
Authorization: Bearer <token>
```

**Response (200 OK):**

```json
{
  "documentNumber": "DOC-2025-0000441",
  "policyCode": "CMS_MEDICARE_10YR",
  "regulatoryBasis": "CMS Medicare Conditions of Participation 42 CFR §482.24",
  "triggerEvent": "CLAIM_CLOSE_DATE",
  "triggerDate": "2025-05-16",
  "eligibleForDispositionDate": "2035-05-16",
  "dispositionStatus": "SCHEDULED",
  "legalHoldActive": false
}
```

---

## 9.10 Error Response Format

All error responses follow a consistent structure.

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable explanation.",
  "correlationId": "a3f9c812-4d1e-4b9c-8f2a-1c3e5d7f9a0b",
  "timestamp": "2025-05-16T09:11:00Z"
}
```

The `correlationId` matches the correlation ID in the server-side log for this
request. When reporting an issue, provide the correlationId to enable fast log
lookup.

**Standard error codes:**

| Code | HTTP Status | Meaning |
|---|---|---|
| UNAUTHENTICATED | 401 | Missing or invalid JWT |
| ACCESS_DENIED | 403 | Role does not permit this operation |
| TRANSITION_DENIED | 403 | Transition is illegal or role-restricted |
| DOCUMENT_NOT_FOUND | 404 | No document exists for the given number |
| CONTENT_DISPOSED | 410 | Content has been disposed; metadata only remains |
| ATTRIBUTE_VALIDATION_FAILED | 400 | One or more attribute values failed validation |
| DOCUMENT_CLASS_NOT_FOUND | 400 | Unknown document class code |
| ILLEGAL_TRANSITION | 400 | Transition is not defined for this class |
| ACTIVE_LEGAL_HOLD | 403 | Disposition blocked by legal hold |
| INTERNAL_ERROR | 500 | Unexpected server error — check logs with correlationId |

---

*Previous: [Chapter 8 — System Administration](08-administration.md)*
*Next: [Chapter 10 — Deployment and Operations](10-deployment.md)*
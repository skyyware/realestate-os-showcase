# Architecture

RealEstate OS is a production-oriented vertical slice for digital WEG
management. It is intentionally small, but it is not a throwaway demo. The
architecture is built around stable product objects, server-side commands,
traceable state changes, and a frontend that renders decisions instead of
inventing product truth in the browser.

## System Shape

- One Spring Boot API.
- One Angular frontend.
- PostgreSQL/Aurora-compatible relational schema managed by Flyway.
- App-native identity for local and stage usage.
- Keycloak/OIDC boundary for production identity integration.
- Transactional mail through environment-based SMTP configuration.
- File storage behind a `DocumentStorage` boundary.
- Stage deployment through Apache, systemd, PostgreSQL, TLS, and restricted
  environment files.
- AWS managed-service blueprint for the production target.

## Backend Modules

The backend is a modular monolith. Module boundaries follow product objects, not
technical layers.

| Module | Responsibility |
| --- | --- |
| `identity` | Registration, password setup, password reset, sessions, external identity mapping |
| `property` | WEG profile, properties, units, MEA, roles, memberships, invitations |
| `finance` | Bookings, house-money assessments, reserves, balances, receivables |
| `planning` | Annual budget and reserve planning foundation |
| `document` | Document metadata, visibility, object links, upload/download storage |
| `meeting` | Owner meetings, invitations, agenda, quorum and majority context |
| `task` | Operational tasks, due dates, status, owner, source context |
| `communication` | Prepared messages, channel, status, recipient, follow-up tasks |
| `audit` | Technical audit trail for write-side commands |
| `activity` | Product-facing activity stream |
| `workspace` | Product-oriented read model and command API for the frontend |
| `security`, `mail`, `config`, `common` | Cross-cutting infrastructure |

Domain modules expose behavior through services and DTOs. The API does not
serve JPA entities directly.

## Workspace Read Model

The frontend consumes a product-oriented workspace response. It contains the
current WEG, portfolio metrics, readiness, units, members, finance data,
documents, meetings, decisions, tasks, messages, permissions, activity, and
technical audit entries.

The key idea is consistency: after each command, the frontend reloads the
workspace from the backend. This keeps the UI thin and prevents browser-only
product state from drifting away from persisted truth.

## Write Model

Write operations are command-oriented:

1. Validate authentication.
2. Resolve the active user and current WEG context.
3. Check role-based permission.
4. Validate product invariants.
5. Persist the change.
6. Write activity for product visibility.
7. Write audit for technical traceability.
8. Return the updated workspace view where appropriate.

This pattern makes product behavior reviewable and gives future operators a
clear chain of evidence.

## Identity

Local and stage environments use app-native registration with one-time tokens,
BCrypt password hashing, and short-lived HMAC JWT sessions. This keeps the
product directly testable.

The production boundary is OIDC-ready:

- Token validation is centralized in `security`.
- Product services receive only the authenticated principal.
- `docker-compose --profile identity` starts Keycloak for integration work.
- `REALESTATE_IDENTITY_MODE=keycloak` enables OIDC resource-server behavior.
- Keycloak roles map to product roles such as `OWNER_ADMIN`,
  `PROPERTY_MANAGER`, and `BOARD_MEMBER`.
- External subjects are stored on the user record for traceability.

## Data Model

Flyway owns the schema. PostgreSQL is used locally; migrations are written to
stay Aurora PostgreSQL-compatible. CI uses H2 in PostgreSQL mode so pull
requests can validate without external infrastructure.

Product-relevant data is stored server-side. Browser-only state is allowed only
for local UI preferences.

## Documents

Documents are evidence, not just files. Each document can carry:

- document type
- status
- visibility
- source
- description
- target object type and ID
- original filename
- storage key
- content type
- size
- SHA-256 hash
- upload timestamp

The storage boundary can use local or stage disk today and can move to
S3-compatible object storage later without changing the domain model.

## Finance

Finance is modeled as an explainable chain:

`bank/manual event -> booking type -> category -> distribution key -> unit ->
owner share -> document evidence -> activity/audit`.

The current slice supports bookings, due dates, payment dates, counterparties,
document references, house-money assessments, unit balances, reserves, and open
receivables. The next product depth is annual closing, owner statements,
plausibility checks, and exportable reports.

## Meetings And Decisions

Decisions are linked to meetings where appropriate. The model captures agenda
context, decision text, voting result, status, due date, responsible role, cost
impact, and document evidence.

The product value is not the decision record alone. The value is the ability to
turn a decision into tasks, documents, communication, finance impact, and a
traceable implementation status.

## Communication And Tasks

Communication is treated as workflow evidence. A message can point to a product
context and can create a follow-up task atomically. Tasks carry owner,
responsibility, origin, due date, reminder date, status, completion timestamp,
activity, and audit.

This prevents important owner communication from becoming an unstructured chat
history.

## Frontend

The Angular frontend is intentionally straightforward:

- `AppComponent` orchestrates session, workspace state, forms, API commands,
  and view switching.
- `auth/AuthShellComponent` renders registration, login, password reset, and
  password setup.
- `layout/SidebarComponent` renders navigation and logout.
- Product data is read from the backend; forms submit typed commands.

The frontend should continue to be split only when it reduces real complexity.
Do not create abstractions before the workflow needs them.

## Product Design

The UI is designed as a consumer-facing owner workspace, not an ERP clone. The
layout should show the next important action, explain money clearly, expose
status and responsibility, and stay usable on mobile.

Design rules:

- Use clear task language.
- Keep empty states actionable.
- Make status, due dates, and responsibility visible without extra clicks.
- Keep finance numbers explainable.
- Avoid decorative UI that does not help the user decide or act.
- Verify desktop and mobile screenshots after meaningful UI changes.

## Operations

Stage is deliberately pragmatic:

- Angular bundle served by Apache.
- `/api` reverse proxy to Spring Boot.
- Spring Boot JAR managed by systemd.
- PostgreSQL on the host or managed-compatible target.
- Secrets in restricted environment files.
- TLS through Let's Encrypt.

The AWS blueprint in `infra/aws` models the managed-service target: Aurora,
S3, SES, CloudWatch, App Runner, and identity integration points.

## Architecture Decision Records

- [ADR 0001: Modular Monolith](adr/0001-modular-monolith.md)

## Verification

Primary checks:

```bash
npm run ci
npm run qa:local
```

Stage health:

```bash
curl -fsS https://realestate.stage.dev/actuator/health
```

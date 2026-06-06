# RealEstate OS

RealEstate OS is a production-oriented product-engineering showcase for digital
WEG management in Germany. It is built to demonstrate how a small team can turn
a modern Java/Spring/Angular/PostgreSQL stack into a usable, maintainable
workspace for property owners, advisory boards, and self-managed owner
communities.

The product value is simple: owners should understand what is open, what costs
money, what needs a decision, and who is allowed to act. The codebase makes that
visible through real onboarding, role-aware commands, finance flows, document
evidence, meeting decisions, communication, tasks, audit logs, and a responsive
consumer-grade UI.

## Live Targets

- Stage: https://realestate.stage.dev
- Local web target: https://realestate.localhost
- Repository: use the configured Git remote for this checkout.

## Product Scope

- Empty workspace after registration: no hidden demo data.
- WEG setup with properties, units, MEA, owner roles, invitations, and readiness.
- Finance workspace with bookings, house-money assessments, reserves, open
  receivables, unit balances, and evidence links.
- Planning foundation for annual budgets, reserves, and year-based operating
  views.
- Documents with upload, metadata, visibility, object links, storage key,
  content type, file size, SHA-256 hash, and download.
- Meetings and decisions with agenda context, voting result, implementation
  status, due dates, responsibility, and document evidence.
- Communication workflow with recipient, channel, status, context, and optional
  follow-up task.
- Task lifecycle from open to review to done, with edit, delete, due date,
  owner, source context, and audit trail.
- Roles, permissions, access settings, activity stream, and technical audit log.
- Password setup and password reset through email-ready transactional flows.

## Technology Stack

- Java 21 target, Spring Boot 3.5, Spring Security, JPA, Flyway, Mail, Actuator
- Angular 21, standalone components, typed reactive forms, functional HTTP
  interceptor
- PostgreSQL 16 locally, Aurora PostgreSQL-compatible schema
- Keycloak/OIDC-ready identity boundary through `docker-compose --profile identity`
- Apache/systemd stage deployment with a clear upgrade path to AWS managed
  services
- Terraform blueprint for Aurora, S3, SES, CloudWatch, App Runner, and Keycloak
  integration points

## Local Setup

```bash
createdb realestate 2>/dev/null || true
cd backend && ./mvnw spring-boot:run
cd ../frontend && npm run start
```

Frontend: `http://localhost:4200`  
API health: `http://localhost:8098/actuator/health`

The local Apache vhost can serve the built frontend at
`https://realestate.localhost` and proxy `/api` to the backend.

## CI

```bash
npm install
npm run ci
```

`npm run ci` runs backend tests and builds the Angular frontend.

## QA Smoke

```bash
npm run qa:local
```

The local QA smoke registers a fresh user, sets a password, starts an empty
workspace, creates real WEG data, exercises finance, document, decision,
communication, task, settings, audit, search, download, and viewport flows, and
writes screenshots to `output/qa`.

## Stage Registration Flow

1. Open https://realestate.stage.dev.
2. Register with name, email, and organization.
3. Open the password setup link from the email.
4. Set a password and use the dashboard.

SMTP settings are supplied only through environment variables on the server.
Secrets are not stored in Git.

## Documentation Map

- [Architecture](docs/architecture.md)
- [WEG market research](docs/research/weg-market-2026.md)
- [Customer problem map](docs/product/weg-customer-problem-map.md)
- [Product brief](docs/product/weg-product-brief.md)
- [Design and case-study workflow](docs/design/figma-case-study-workflow.md)
- [Slice 1: WEG onboarding and roles](docs/case-study/slice-1-weg-onboarding.md)
- [Slice 2: finance foundation](docs/case-study/slice-2-finance-foundation.md)
- [Slice 3: document evidence chain](docs/case-study/slice-3-document-evidence-chain.md)
- [Slice 4: meeting and decision workflow](docs/case-study/slice-4-meeting-decision-workflow.md)
- [Slice 5: communication, tasks, and deadlines](docs/case-study/slice-5-communication-task-deadline-workflow.md)
- [Slice 6: roles, rights, audit, and operations](docs/case-study/slice-6-roles-rights-audit-ops.md)
- [ADR 0001: modular monolith](docs/adr/0001-modular-monolith.md)
- [Test strategy](docs/test-strategy.md)
- [Handover runbook](docs/handover.md)
- [AWS blueprint](infra/aws/README.md)

## Quality Bar

The repository should be easy to review, easy to run, and honest about what is
implemented. Every product claim in the docs should point to code, tests, a
verified flow, or a documented market assumption.

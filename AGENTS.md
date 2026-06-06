# AGENTS.md

This file is the binding working instruction for coding agents in RealEstate OS.

## Mission

Build RealEstate OS as an internally maintainable product-engineering codebase:
Java 21+, Spring Boot, Angular, PostgreSQL/Aurora-compatible schema,
Keycloak/OIDC boundaries, AWS managed-service readiness, modular design, and no
unnecessary abstraction.

## Before Every Change

- Read `README.md`, `docs/architecture.md`, and the affected files.
- Check `git status --short --branch`.
- Search with `rg` or `rg --files`.
- Protect local changes made by someone else.
- Never write secrets, SMTP credentials, tokens, private PDFs, or personal
  documents into the repository.

## Architecture Rules

- Domain modules must not depend on `workspace`, `security`, `mail`, browser
  code, PDF rendering, CI, network, or filesystem delivery details.
- Write-side workspace commands must enforce role-based access.
- Persistent domain objects need a Flyway migration, repository, service
  command, activity entry, audit log, and focused tests.
- Frontend forms must call real API commands for product data. Do not keep
  product-relevant state only in the browser.
- New WEG functionality must be represented in the local QA smoke flow.
- Documentation must be in English, concise, source-based, and useful to both
  humans and agents.

## Standard Checks

```bash
npm run ci
npm run qa:local
```

For deployment:

```bash
curl -fsS https://realestate.stage.dev/actuator/health
```

## Do Not

- Do not add demo seeds to productive workspaces.
- Do not commit secrets.
- Do not remove tests just to make the build green.
- Do not introduce framework-heavy architecture when a modular monolith is
  enough.
- Do not claim success without actual verification.

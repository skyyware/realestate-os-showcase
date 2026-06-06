# Test Strategy

Last updated: 2026-06-06

RealEstate OS should be safe to review, change, and deploy. Tests focus on
product behavior, module boundaries, real workflows, and responsive usability.

## Test Pyramid

- Unit-like tests: email policy, transactional mail failure handling, external
  identity boundary.
- Integration tests: Spring context, Flyway migrations, JPA repositories,
  workspace service flow.
- Architecture tests: module boundaries with ArchUnit.
- Frontend build: Angular template and TypeScript checks.
- Browser smoke: registration, password setup, empty workspace, and core
  product flows.
- Security smoke: public auth endpoints remain reachable without session;
  workspace APIs remain protected.

## Critical Product Paths

The local QA smoke should cover:

- registration without pre-filled data
- activation link and password setup
- password reset from login
- property creation
- unit and owner structure
- finance booking and open amount
- annual planning foundation
- document upload, save, download, and content comparison
- meeting preparation
- decision creation and implementation
- task create, edit, review, complete, and delete
- communication preparation and follow-up task
- user role update, access status, and deactivation
- role and workspace settings
- workspace switching
- global search
- desktop and mobile screenshots without visible layout breaks

## Definition Of Done

- `npm run ci` passes.
- `npm run qa:local` passes when UI or API flows changed.
- New persistent data has Flyway migration and focused service coverage.
- New modules do not violate ArchUnit rules.
- Stage deployment verifies health and browser smoke behavior.
- Known residual risks are reported explicitly.

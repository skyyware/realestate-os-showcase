# Slice 1: WEG Onboarding And Roles

Last updated: 2026-06-06

## Purpose

This slice closes the most important gap between a visual demo and a real WEG
product. A WEG is not just a property. It consists of units, ownership shares,
owners, roles, invitations, permissions, and readiness signals. Finance,
decisions, documents, and communication are not reliable without this
foundation.

## Figma

- Case-study board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artifact: `RealEstate OS Slice 1 WEG Onboarding And Roles`

The flow covers registration, empty workspace, WEG profile, units, MEA review,
roles, invitations, and transition into finance.

## Implementation

Backend:

- Flyway V5 extends `property_asset` with fiscal year, reserve target, expected
  MEA total, and management mode.
- `owner_unit` stores owner email, usage type, MEA, and voting weight.
- `community_member` models WEG roles and invitation status.
- Service-level permissions distinguish admin, manager, self-manager, advisory
  board, owner, and external expert.
- Password activation links invited memberships automatically.
- The workspace response exposes `members` and `readiness`.

Frontend:

- WEG setup captures fiscal year, reserve target, MEA target, and management
  mode.
- Unit setup captures owner email, MEA, voting weight, and usage.
- Readiness cards show MEA status, roles, and finance readiness.
- Roles can be invited from the unit view.
- The onboarding checklist makes MEA and role setup explicit.

## Acceptance

- New accounts start empty.
- A WEG can be created with useful domain data.
- The first unit creates a traceable owner role.
- Role invitations are visible in activity and audit.
- The system can identify whether units, MEA, and roles are ready for finance.

## Verification

```bash
npm run ci
npm run qa:local
```

The local QA smoke covers registration, password setup, WEG creation, units,
roles, readiness, finance, documents, decisions, tasks, communication, workspace
switching, search, and mobile dashboard.

Screenshot outputs:

- `output/qa/realestate-units-desktop.png`
- `output/qa/realestate-dashboard-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Product Value

The slice gives the product a real operating base. Every later module can now
answer: which WEG, which unit, which owner, which role, and which permission?

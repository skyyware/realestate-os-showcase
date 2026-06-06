# Slice 6: Roles, Rights, Audit, And Operations

Last updated: 2026-06-06

## Purpose

A production-oriented WEG app must build trust: who may do what, which action
was triggered by whom, and can the system be reviewed and operated? This slice
makes permissions and audit visible while improving operational readiness.

## Figma

- Case-study board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artifact: `RealEstate OS Slice 6 Roles Rights Audit Operations`

The artifact covers role model, server-side permission checks, audit trail,
product visibility, and operations readiness.

## Implementation

Backend:

- Password reset is implemented as a dedicated auth flow and explicitly allowed
  through security configuration.
- Flyway V10 extends `audit_log` with `property_id` and a WEG/time index.
- Audit entries store actor, role, WEG, action, target type, target ID, summary,
  and timestamp.
- Workspace commands write audit with WEG context.
- Tasks can be updated and deleted; status changes, edits, and deletion write
  audit.
- Community members can be updated, deactivated, and assigned roles. The primary
  admin is protected from deactivation.
- Workspace response exposes `access` with current role, edit/admin rights, and
  allowed command groups.
- Workspace response exposes recent technical audit entries.
- Tests cover access read model and audit entries in the workspace flow.

Frontend:

- Known users land on login first; password reset is available from the login
  card.
- Settings show current role, edit level, and allowed command groups.
- Settings manage user roles, access status, and workspace notifications.
- Tasks can be edited, deleted, and moved by status from the list.
- Activity view also shows technical audit evidence.
- `/set-password` is a registered route for direct setup links.

## Acceptance

- Every write-side workspace action creates audit with WEG context.
- Users can see current role and allowed work areas.
- Admins can manage roles and access safely.
- Tasks can be edited, moved, completed, and deleted without workarounds.
- Returning users can reset passwords.
- Audit entries are visible in the product and searchable.
- Direct setup links do not create router errors.
- CI, local browser QA, and stage smoke are part of release practice.

## Verification

```bash
npm run backend:test
npm run frontend:build
npm run qa:local
npm run ci
```

Screenshot outputs:

- `output/qa/realestate-audit-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Product Value

The product becomes reviewable. It can explain who acted, under which role, in
which WEG context, and what changed.

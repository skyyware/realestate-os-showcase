# Slice 2: Finance Foundation, House Money, And Balances

Last updated: 2026-06-06

## Purpose

This slice turns WEG structure into an explainable finance workspace. Owner
communities need more than account balance and reserve totals. They need
receivables, due dates, receipts, distribution keys, unit balances, and a clear
view of what each owner owes.

## Figma

- Case-study board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artifact: `RealEstate OS Slice 2 Finance Foundation`

The flow follows the chain from units and MEA to house-money assessment,
booking, evidence, open receivable, unit balance, and dashboard priority.

## Implementation

Backend:

- Flyway V6 extends `finance_event` with event type, distribution key, unit
  reference, due date, payment date, counterparty, receipt number, and document
  reference.
- `house_money_assessment` stores house money and reserve share per unit and
  fiscal year.
- `WorkspaceService` calculates unit balances from assessments and owner
  payments.
- Expenses and reimbursements are normalized as negative values server-side;
  owner payments stay positive.
- Open receivables are calculated from all finance events in the selected WEG.

Frontend:

- Finance uses tabs for booking capture and booking history.
- Booking form captures event type, category, amount, booking date, due date,
  distribution key, unit, counterparty, receipt number, document reference, and
  status.
- House-money assessment can be created per unit and year.
- Unit balance cards show yearly target, paid amount, and open amount.
- After booking, the view switches to the history with the new context visible.

## Acceptance

- A WEG with units can create house-money assessments per unit.
- Yearly target amount is composed from house money and reserve share.
- Positive expense input is stored as a negative cost/receivable event.
- Open receivables appear in metrics, command center, and booking history.
- Finance events carry enough context for later document search and owner
  communication.

## Verification

```bash
npm run backend:test
npm run frontend:build
npm run qa:local
npm run ci
```

The local QA smoke covers assessment creation, unit balance, server-side amount
normalization, annual plan foundation, documents, decisions, tasks,
communication, search, workspace switching, and mobile dashboard.

Screenshot outputs:

- `output/qa/realestate-finances-desktop.png`
- `output/qa/realestate-dashboard-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Product Value

Finance becomes explainable. A number is no longer a static card; it becomes a
path from booking to owner impact.

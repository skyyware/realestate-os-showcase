# Product Brief: RealEstate OS For WEG Management

Last updated: 2026-06-06

## Product Mission

RealEstate OS should become a usable operating workspace for German WEG
self-management and assisted management. The goal is not to show many screens.
The goal is that a small or mid-sized WEG can start real administration work
with no demo data, clear roles, traceable money, reliable decisions, documents,
communication, and tasks.

## Non-Negotiable Qualities

- **Understandable:** Domain complexity is explained, not hidden.
- **Traceable:** Every relevant action, number, document, and decision has a
  source and history.
- **Guided:** Users always see the next useful step.
- **Consumer-ready:** The UI is calm, mobile, readable, and forgiving.
- **Maintainable:** Modules, tests, documentation, and architecture can be
  owned by a small product team.
- **Honest:** The product supports legal and financial workflows; it does not
  pretend to replace professional advice.

## Target Product Capabilities

### 1. WEG And Roles

Capabilities:

- Create WEG with address, fiscal year, reserve target, management mode, and
  expected MEA total.
- Create units with owner contact, usage, MEA, and voting weight.
- Invite owners, board members, managers, and external experts.
- Enforce role-aware write commands.

Acceptance:

- New accounts start with an empty workspace.
- Invites and role changes are audited.
- Users can see their own role and allowed command groups.

### 2. Finance Workspace

Capabilities:

- Connect bookings, receipts, categories, distribution keys, units, due dates,
  payment dates, counterparties, and document references.
- Track house-money assessment, reserve share, paid amount, and open balance per
  unit.
- Surface open receivables in metrics and next-step recommendations.

Acceptance:

- No finance card shows a number without a path to explanation.
- A booking can be saved with context and evidence.
- Open receivables can be inspected per unit.

### 3. Annual Budget And Closing

Capabilities:

- Year-based annual budget from actuals, categories, reserve planning, and
  scenarios.
- Annual statement with plausibility checks, missing evidence, owner balance,
  additional payment or credit, and asset report.
- Decision templates for budget and annual statement.

Acceptance:

- Users can start from actual values and adjust the plan.
- The system highlights missing evidence and unusual deviations.
- Exports are reviewable per community and per unit.

### 4. Meetings And Decisions

Capabilities:

- Owner meeting with in-person, hybrid, or virtual mode.
- Agenda, invitation date, response deadline, quorum, majority requirement,
  voting rights, voting result, minutes, and decision register.
- Decision status, implementation due date, responsible role, cost impact, and
  follow-up tasks.

Acceptance:

- An agenda item can become a decision and then tasks.
- Decisions are readable on mobile and exportable.
- Virtual meeting context can capture the required basis decision.

### 5. Document Evidence Chain

Capabilities:

- Upload documents with type, status, visibility, source, year, object link,
  file metadata, and version-ready structure.
- Link documents to bookings, decisions, meetings, tasks, and contracts.
- Search by type, year, object, unit, person, and text metadata.

Acceptance:

- Every document has ownership, visibility, audit, and product context.
- Users can download uploaded files.
- Meeting or advisory export packages can be assembled later from the same
  evidence model.

### 6. Communication As Cases

Capabilities:

- Owner request, damage report, announcement, and internal note as structured
  cases.
- Status, recipient, channel, responsible role, due date, answer history, and
  optional follow-up task.
- Email notifications deep-link into the correct case.

Acceptance:

- No important request disappears as free-form chat.
- Owners see status and responsibility.
- Urgent topics can be escalated.

### 7. Maintenance And Contracts

Capabilities:

- Contracts, deadlines, maintenance events, offers, measures, orders, and
  acceptance.
- Renovation initiatives with funding need, decision requirement, and progress.

Acceptance:

- A measure shows reason, offers, decision, cost, funding, responsible party,
  and next action.
- Contract deadlines create tasks before risk appears.

## Design Direction

The product should feel calm and trustworthy:

- first screen is a working dashboard, not a landing page
- large visual priority only for truly important decisions
- dense but readable detail views for repeated work
- mobile support for reading, answering, approving, uploading, and finding
  documents
- no decorative overload
- numbers, status, and next actions before long explanatory copy

## Engineering Slice Order

1. Deepen WEG onboarding with units, roles, and empty product state.
2. Expand finance into evidence, categories, distribution keys, house money, and
   owner balances.
3. Build meeting and decision workflow end to end.
4. Expand documents into real file storage and object linking.
5. Turn communication into case and task workflow.
6. Deliver annual budget, annual statement, and asset report as a guided yearly
   cycle.
7. Add maintenance, offers, contracts, and renovation initiatives.

## Definition Of Ready

- Problem is backed by research, interview notes, or a documented assumption.
- User journey and UI state are captured in Figma/FigJam or an equivalent
  design artifact.
- Data model, permissions, audit, and export impact are known.
- Backend, frontend, and QA smoke coverage are named.
- Privacy and operational implications are understood.

## Definition Of Done

- `npm run ci` passes.
- Affected core flow is covered by local QA smoke or focused tests.
- Desktop, tablet, and mobile were visually checked when UI changed.
- Design artifact and implementation screenshot were compared.
- Documentation is updated in English.

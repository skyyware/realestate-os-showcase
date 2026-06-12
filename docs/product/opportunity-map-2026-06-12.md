# RealEstate OS Opportunity Map

Last updated: 2026-06-12

This document refines the product direction for RealEstate OS after reviewing
the current WEG self-management market, the existing product slice, and the
largest remaining gaps between a credible showcase and a product that could
become commercially useful.

## Product Thesis

RealEstate OS should become a guided operating system for small and mid-sized
German owner communities. The product should not compete by showing more
administration screens. It should compete by helping non-expert owners answer
three questions faster and with more confidence:

1. What needs attention now?
2. What does it cost me and the community?
3. Which decision, document, task, or person proves the next step?

The strongest product direction is therefore:

> Turn WEG administration from scattered documents, e-mails, meetings, and
> spreadsheets into a connected annual operating cycle.

## Current Baseline

RealEstate OS already has a useful vertical slice:

- Registration, password setup, and password reset.
- Empty workspace after signup.
- WEG, properties, units, MEA, roles, memberships, and permissions.
- Finance bookings, house money, reserves, open receivables, and unit balances.
- Documents with metadata, upload, download, visibility, and object links.
- Meetings, decisions, implementation status, and evidence.
- Communication with follow-up tasks.
- Task lifecycle with edit, delete, status transitions, activity, and audit.
- Settings, role management, activity stream, and technical audit log.
- Local QA smoke across desktop and mobile.

This is enough to prove product-engineering ability. It is not yet enough to
prove a sellable product, because the core yearly WEG cycle is still fragmented.

## Market Signals

| Signal | Product meaning |
| --- | --- |
| WEG software buyers look for one place for documents, decisions, tasks, and important figures. | The dashboard must become a true work cockpit, not a static metric surface. |
| Assisted self-management is positioned as software plus process guidance and human support. | The product must know when to guide, when to warn, and when to hand off to an expert. |
| Annual budget, annual statement, and asset report are legally central artifacts under WEG Section 28. | Finance depth is the biggest product gap and should be tackled first. |
| Owner communities need bank account and payment visibility in addition to accounting records. | Bank import, reconciliation, and payment status are product multipliers. |
| Decisions are valuable only when they become implementation work. | Decision, task, offer, document, cost, and owner communication must form one case. |
| Low-friction usability matters because owners are not professional administrators. | Every module needs a personal owner view, mobile completion, and plain language. |

## Biggest Opportunities

### 1. Guided Annual Operating Cycle

This is the highest-leverage opportunity. WEG work repeats every year and
touches almost every existing module:

- opening balance
- annual budget
- house-money schedule
- monthly bookings
- receipt completeness
- reserve movement
- open receivables
- owner balances
- annual statement
- asset report
- meeting agenda
- decision package
- owner communication

Why it matters:

- It maps directly to the legal and practical core of WEG administration.
- It turns current finance objects into a coherent workflow.
- It gives the product a measurable promise: fewer surprises at year end.
- It creates a natural sales demo: start with actuals, find gaps, create a
  decision-ready annual package.

### 2. Owner-Centric Financial Explainability

RealEstate OS currently explains finance at community and unit level, but it
does not yet give every owner a personal financial narrative.

The product should answer:

- What do I owe this year?
- What have I paid?
- What is still open?
- Which receipts explain my share?
- Which decision caused this cost?
- What changes if the annual budget changes?

This should become the consumer-facing differentiator. A WEG product wins when
skeptical owners can understand their own position without calling the board.

### 3. Decision-To-Implementation Cases

Decisions should become structured cases:

- agenda item
- decision text
- voting result
- document evidence
- responsible person or role
- offers
- implementation tasks
- cost impact
- owner update
- final proof

The current decision and task modules are a good base. The missing product layer
is a single case view that shows whether a decision is still only a protocol
entry or already real implemented work.

### 4. Bank And Invoice Intake

Manual finance entry is useful for the showcase, but sellable software needs
intake:

- bank transaction import
- invoice upload
- matching between invoice, transaction, booking, and document
- exceptions queue
- reconciliation status
- audit trail for manual corrections

This should not be built as a heavy accounting system first. Start with a narrow
bank-import and invoice-matching workflow that improves trust in existing
finance numbers.

### 5. Readiness And Risk Guidance

Self-managed WEGs need guardrails. The product should detect whether the
community is operationally ready:

- missing units or MEA total
- missing roles
- incomplete house-money schedule
- missing reserve target
- overdue tasks
- no annual budget
- missing decision evidence
- unresolved owner requests

The current onboarding checklist should evolve into a readiness model that
explains risk and recommends the next safe step.

### 6. Expert And Support Handoff

Assisted self-management is not pure SaaS. A useful product should make expert
support efficient:

- case export package
- relevant documents
- decision history
- finance evidence
- timeline
- open questions
- permission-limited expert access

This should be designed as a boundary. The product prepares context; the human
advisor gives advice or validation.

### 7. AI With Source Discipline

AI can become useful later, but it should not be the first differentiator. In
this domain, blind automation is risky. Good use cases are:

- summarize a case from linked evidence
- propose missing fields before a decision package is complete
- classify an uploaded invoice
- draft owner communication from approved facts
- explain a financial position with source links

Every AI output must show sources, uncertainty, and required human approval.

## What To Tackle First

Start with the guided annual operating cycle.

Reasoning:

- It is the clearest market need.
- It connects the most existing modules.
- It turns the product from a workspace into a repeatable operating system.
- It creates better demo value than adding another isolated module.
- It reduces the largest delta between current RealEstate OS and a product an
  owner community would pay for.

## Next Slice: Annual Cycle MVP

### User Job

As a self-managing owner or advisory board member, I want to prepare the next
annual budget and understand the previous year well enough to make a clean
meeting decision.

### Scope

Backend:

- Extend `annual_plan` into an annual cycle aggregate or add a dedicated
  annual-cycle model.
- Store fiscal year status, opening balance, budget categories, planned
  reserves, actual totals, evidence completeness, owner balances, and review
  state.
- Calculate owner impact from existing units, MEA, house-money assessments, and
  finance events.
- Record activity and audit for every status change.
- Expose a cycle read model in the workspace response.

Frontend:

- Add an annual-cycle view with three tabs:
  - Plan: budget, reserve contribution, house-money schedule.
  - Review: actuals, missing evidence, deviations, open receivables.
  - Package: decision-ready summary, owner impact, document checklist.
- Keep the dashboard focused on the next missing annual-cycle step.
- Add owner-level cards: expected, paid, open, adjustment or credit.
- Make missing evidence actionable by linking to document upload.

QA:

- Local smoke creates a fiscal year cycle.
- It creates house-money assessments and finance events.
- It marks a document as evidence.
- It verifies owner impact and missing evidence.
- It generates or previews a decision package.
- It checks desktop and mobile rendering.

### First Acceptance Criteria

- A WEG can start an annual cycle for a fiscal year.
- The cycle shows whether the annual budget, house-money schedule, bookings,
  evidence, owner balances, and meeting package are complete.
- Owner impact is visible per unit.
- Missing evidence points to a concrete upload action.
- The cycle can move through draft, review, decision-ready, and decided states.
- Every state transition creates activity and audit.
- The UI never claims legal certainty; it presents workflow readiness.

## Sequencing After The Annual Cycle

1. Annual Cycle MVP.
2. Owner financial view and owner statement preview.
3. Decision-to-implementation case view.
4. Bank transaction import and invoice matching.
5. Readiness scoring and guided risk warnings.
6. Expert handoff package.
7. Source-linked assistance for classification, summaries, and message drafts.

## Design Principles For The Next Iteration

- Default to a guided workspace over blank forms.
- Show owner impact beside community impact.
- Prefer checklists with evidence links over long explanations.
- Use calm status labels: draft, needs review, decision-ready, decided,
  implemented.
- Put the next action in one obvious place.
- Keep mobile owner participation first-class.
- Use technical audit for operators, but product activity for owners.

## Engineering Principles For The Next Iteration

- Keep the modular monolith.
- Add persistence through Flyway before UI state.
- Keep calculations server-side and covered by focused tests.
- Do not store finance truth only in the browser.
- Every write command enforces role-based access.
- Every workflow state change writes activity and audit.
- Keep exports and AI behind clear boundaries.

## Sources

- https://www.dotega.de/magazin/
- https://www.dotega.de/magazin/wirtschaftsplan-weg/
- https://www.dotega.de/magazin/weg-selbstverwaltung-eigenregie/
- https://www.wohnen-im-eigentum.de/dotega-die-software-fuer-die-weg-selbstverwaltung
- https://www.wohnen-im-eigentum.de/wohnungseigentum/hausgeld/jahresabrechnung
- https://www.gesetze-im-internet.de/woeigg/__28.html


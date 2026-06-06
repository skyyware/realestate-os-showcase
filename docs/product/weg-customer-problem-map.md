# WEG Customer Problem Map

Last updated: 2026-06-06

This map turns market research into product jobs, pain points, responses, and
acceptance criteria. It is the reference for backlog decisions, design reviews,
and QA coverage.

## Target Roles

| Role | Job to be done | Success looks like |
| --- | --- | --- |
| Self-managing owner | Run the WEG legally, transparently, and with less manual work. | Required workflows have checklists, templates, deadlines, permissions, and audit. |
| Advisory board | Review management work and inform owners. | Money, receipts, decisions, tasks, and risks are easy to inspect. |
| Individual owner | Understand what is happening and what it costs. | Personal share, documents, votes, and open decisions are clear. |
| Landlord owner | Use statements and evidence for tenants and taxes. | Owner statements, receipts, and exports are available. |
| New owner | Understand an existing WEG quickly. | Unit, house money, reserves, decisions, documents, and current issues are visible. |
| External expert | Help on a case without reconstructing context. | Shared case files, documents, decisions, and status reduce back-and-forth. |

## Problem Clusters

| Priority | Problem | Customer pain | Product response |
| --- | --- | --- | --- |
| P0 | Master data and roles are unclear | Nobody knows who owns what, who may act, or who is responsible. | Tenant-ready WEG structure with properties, units, MEA, roles, invitations, and permissions. |
| P0 | Money is not explainable | House money, reserves, arrears, and receipts feel like a black box. | Finance chain from booking to receipt to category to distribution key to owner share. |
| P0 | Annual budget and statement create yearly stress | Excel, deadlines, error fear, and meeting conflict. | Guided annual cycle with actuals, plan suggestions, plausibility, owner impact, and decision templates. |
| P0 | Decisions are not managed cleanly | Dispute risk, missing history, unclear implementation. | Meeting and decision workflow with agenda, vote, minutes, decision register, tasks, and evidence. |
| P0 | Documents are scattered | Receipts, contracts, minutes, insurance, and declarations must be hunted down. | Document evidence chain with upload, type, version, visibility, search, and object links. |
| P0 | Communication has no consequence | Emails and chats do not create responsibility. | Case-based communication with status, SLA, owners, internal notes, and owner-facing updates. |
| P1 | Maintenance is reactive | Damage, offers, cost, and funding decisions drift apart. | Maintenance planning with measures, offers, decisions, funding, and progress. |
| P1 | Owners have different digital comfort levels | Some work mobile-first; others need simple email and PDFs. | Accessible UI, clear language, email bridge, PDF exports, and mobile task completion. |
| P1 | External help is hard to involve | Legal, construction, and tax questions need context. | Expert access per case, export packages, and advisory notes. |
| P2 | AI without trust is risky | Users do not want a black box for law or money. | Assistance only with sources, uncertainty, explanation, and human approval. |

## Product Thesis

1. A WEG app earns trust when it explains money and decisions better than the
   current manual process.
2. Self-management becomes usable when responsibility is visible and work is
   broken into small safe steps.
3. The highest value comes from connected workflows: decision creates task,
   task creates order, order creates invoice, invoice explains house money.
4. Consumer-grade design is not cosmetic in this domain. Many users are not
   administration professionals.

## Acceptance Rules For New Features

- Every write action has role enforcement, activity, and audit.
- Every finance number can be traced to its source.
- Every legally relevant decision can produce an exportable artifact.
- Every workflow has empty state, validation, error state, success state,
  mobile view, and automated coverage.
- Product copy must not imply legal certainty where the software only provides
  workflow assistance.
- Every meaningful UI decision should be captured in Figma/FigJam and checked
  against implementation screenshots.

## Product Metrics

| Metric | Why it matters |
| --- | --- |
| Time to first WEG | Shows how quickly a new user can start real work. |
| Linked booking ratio | Shows whether finance transparency is actually created. |
| Decision implementation rate | Shows whether decisions become action. |
| Average response time per case | Measures service reliability instead of chat volume. |
| Open required steps | Shows whether a WEG is operationally on track. |
| Mobile task completion | Shows whether owners can participate without a desktop. |

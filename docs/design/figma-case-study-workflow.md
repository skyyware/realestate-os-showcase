# Design And Case-Study Workflow

Last updated: 2026-06-06

RealEstate OS should document product thinking as clearly as it documents code.
Every important product decision should be connected to research, design,
implementation, and verification.

## Design Artifacts

| Artifact | Purpose |
| --- | --- |
| Research map | Make market roles, pain points, and product responses visible. |
| Information architecture | Clarify modules, navigation, and product object relationships. |
| User flows | Test critical jobs before implementation: create WEG, book finance event, prepare decision, find document. |
| Wireframes | Validate content order, density, and mobile usage before visual polish. |
| High-fidelity screens | Capture layout, typography, colors, components, empty states, and responsive behavior. |
| Implementation review | Compare live screenshots against design intent and document deviations. |

## Design Rules

- Start every view from a real user job.
- Make every key number explainable.
- Name primary actions by product intent.
- Empty states should move users toward setup or resolution.
- Mobile views are first-class, not follow-up work.
- Use status colors sparingly: green for healthy/done, amber for review, red for
  risk, blue for information.
- Text must wrap cleanly on small viewports.
- Avoid decorative UI that does not help a user decide, understand, or act.

## Review Protocol

| Review area | Question |
| --- | --- |
| Task clarity | Can an owner understand the next step without instructions? |
| Domain completeness | Are the required WEG, finance, document, decision, or role fields present? |
| Permission logic | Does each role see only what it may see and do? |
| Empty/error/success states | Are non-happy paths designed, not left to browser defaults? |
| Responsive layout | Does the view work on mobile, tablet, laptop, and desktop? |
| Accessibility | Are contrast, focus, labels, and keyboard behavior acceptable? |
| Implementability | Does the design fit the Angular/Spring architecture without awkward state hacks? |

## Figma Evidence

| Date | Artifact | Link | Status |
| --- | --- | --- | --- |
| 2026-06-04 | WEG Market Problem Map | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8?utm_source=chatgpt&utm_content=edit_in_figjam&oai_id=&request_id=d10a8ab9-bc1f-4f99-828b-94eab9cee443) | Created |
| 2026-06-04 | Slice 1 WEG Onboarding And Roles | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Created and implemented locally |
| 2026-06-04 | Slice 2 Finance Foundation | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Created, tested locally, deployable |
| 2026-06-04 | Slice 3 Documents And Evidence Chain | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Created, tested locally, deployable |
| 2026-06-04 | Slice 4 Meeting Decision Workflow | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Created, tested locally, deployable |
| 2026-06-04 | Slice 5 Communication Task Deadline Workflow | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Created, tested locally, deployable |
| 2026-06-04 | Slice 6 Roles Rights Audit Operations | [FigJam](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Created, tested locally, deployable |

## Case-Study Narrative

The case study should tell this story:

1. Market problem: scarcity, cost pressure, transparency gaps, and owner trust.
2. Customer understanding: roles, jobs, pain points, and core legal workflows.
3. Product thesis: guided self-management instead of generic property lists.
4. Architecture: modular Java/Spring/Angular/PostgreSQL product codebase.
5. Design process: research map, flows, screens, and screenshot reviews.
6. Implementation: registration, empty workspace, role-based commands, audit,
   finance, decisions, documents, communication, and tasks.
7. Verification: CI, local QA, viewport checks, stage health.
8. Outcome: shareable live app, shareable code, and a clear roadmap.

# Slice 4: Meeting And Decision Workflow

Last updated: 2026-06-06

## Purpose

WEG decisions are reliable only when invitation, agenda, decision text, voting,
minutes, and implementation stay connected. This slice turns loose decisions
into a traceable workflow from meeting planning to evidence and follow-up.

## Figma

- Case-study board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artifact: `RealEstate OS Slice 4 Meeting Decision Workflow`

The artifact covers the user job from invitation to decision, minutes, and
implementation.

## Implementation

Backend:

- Flyway V8 extends `owner_meeting` with invitation date, response deadline,
  quorum, and majority requirement.
- `community_decision` stores optional meeting reference, agenda item,
  implementation due date, responsible role, and cost impact.
- `WorkspaceService` validates that decisions can link only to meetings in the
  same WEG.
- Meeting and decision DTOs expose the new workflow data.
- Backend tests cover WEG creation, meeting planning, decision linking, and
  implementation review.

Frontend:

- Meetings can be planned with invitation date, response deadline, majority
  requirement, status, and agenda.
- Decisions can link to a meeting and agenda item.
- Due date, responsible role, and cost impact are visible in the decision
  register.
- Documents can be attached as minutes evidence.
- Search and lists expose context without extra navigation.

## Acceptance

- An empty workspace contains no demo decisions.
- A meeting can be planned for the selected WEG.
- A decision can link to that meeting.
- Decision details show agenda item, meeting, due date, responsibility, cost
  impact, and voting result.
- Status changes write activity and audit.
- A document can prove the decision as minutes evidence.

## Verification

```bash
npm run backend:test
npm run frontend:build
npm run qa:local
npm run ci
```

The local QA smoke creates a meeting, links a decision, marks it implemented,
and uploads minutes as decision evidence.

Screenshot outputs:

- `output/qa/realestate-decisions-desktop.png`
- `output/qa/realestate-documents-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Product Value

Decisions stop being static text. They become accountable work with context,
evidence, due dates, and implementation status.

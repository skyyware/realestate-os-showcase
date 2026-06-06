# Slice 5: Communication, Tasks, And Deadlines

Last updated: 2026-06-06

## Purpose

WEG administration needs reliable follow-up. Owners must be informed, boards and
managers need clear responsibility, and deadlines must not disappear in email
threads. This slice connects communication with tasks, reminders, and source
context.

## Figma

- Case-study board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artifact: `RealEstate OS Slice 5 Communication Task Deadline Workflow`

The flow covers trigger, message, follow-up task, deadline control, and
evidence.

## Implementation

Backend:

- Flyway V9 extends `work_task` with responsibility, source context, target
  object, reminder date, and completion timestamp.
- `community_message` stores channel, status, source context, target object,
  send readiness, optional follow-up task link, and sent timestamp.
- Shared `WorkContextType` models manual, finance, document, decision, and
  meeting contexts.
- `WorkspaceService` validates WEG context for tasks and messages.
- A message can atomically create a follow-up task.
- Insights prioritize overdue tasks and upcoming reminders.

Frontend:

- Task form captures responsibility, source context, due date, and reminder.
- Communication form captures recipient, channel, status, send date, source
  context, and optional follow-up task.
- Lists show context, status, deadlines, and follow-up task without extra
  navigation.
- Global search covers tasks and messages by context, owner, channel, and due
  dates.

## Acceptance

- The empty workspace stays free of demo messages.
- A message can link to a product context in the same WEG.
- A message can create a follow-up task with due date and reminder.
- Tasks show responsibility, source, due date, reminder, and status.
- Done tasks set `completed_at` and leave open metrics.
- Browser QA covers meeting, message, follow-up task, status change, and
  dashboard update.

## Verification

```bash
npm run backend:test
npm run frontend:build
npm run qa:local
npm run ci
```

Screenshot outputs:

- `output/qa/realestate-communication-desktop.png`
- `output/qa/realestate-dashboard-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Product Value

Communication becomes accountable work. Each message can carry context, owner,
state, and a next action.

# Slice 3: Document Evidence Chain

Last updated: 2026-06-06

## Purpose

WEG management often fails because documents lack context. Which invoice belongs
to which booking? Which minutes support which decision? Which document may an
owner, board member, or manager see? This slice turns documents into linked
evidence instead of loose files.

## Figma

- Case-study board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artifact: `RealEstate OS Slice 3 Documents And Evidence Chain`

The flow covers upload, classification, object linking, review, and reuse in
finance, decision, and meeting contexts.

## Implementation

Backend:

- Flyway V7 extends `property_document` with status, visibility, source,
  description, link type, and target object ID.
- Flyway V11 adds storage key, content type, file size, SHA-256 checksum, and
  upload timestamp.
- `DocumentStatus`, `DocumentVisibility`, and `DocumentLinkType` model document
  state explicitly.
- Documents can link to finance events, decisions, or meetings in the same WEG.
- `WorkspaceService` validates every link server-side against the current WEG.
- General documents may not set a target object; linked documents must set a
  valid target.
- `DocumentStorage` stores real uploads outside domain modules, limits file
  size, normalizes names, and calculates SHA-256.
- Downloads use a role-aware workspace API with content disposition and content
  type.

Frontend:

- Document form captures type, status, visibility, source, target type, target
  object, and description.
- The file picker uses the real selected filename.
- Target objects are offered from current WEG finance events, decisions, and
  meetings.
- Document lists show file, date, approval, visibility, link type, target,
  size, content type, and hash.
- Saved files can be downloaded from the document list.
- Search includes status, visibility, source, and description.

## Acceptance

- A finance receipt can be linked to a finance event.
- Meeting minutes can be linked to a decision.
- Documents cannot point to objects from another WEG.
- Visibility, status, upload metadata, and hash are product data.
- The empty workspace stays empty until users upload real documents.

## Verification

```bash
npm run backend:test
npm run frontend:build
npm run qa:local
npm run ci
```

The local QA smoke uploads two fixture files, links one to a finance event and
one to a decision, downloads both, and compares the downloaded content with the
fixtures.

Screenshot outputs:

- `output/qa/realestate-documents-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Product Value

Documents become operational evidence. Future exports, advice packages, annual
reviews, and owner questions can reuse the same source chain.

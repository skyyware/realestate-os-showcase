# Slice 4: Versammlung und Beschlussworkflow

Stand: 4. Juni 2026

## Ziel

WEG-Entscheidungen sind nur belastbar, wenn Einladung, Tagesordnung,
Beschlusswortlaut, Abstimmung, Protokoll und Umsetzung zusammenhaengen. Slice 4
macht aus losen Beschluessen einen nachvollziehbaren Workflow: Eine
Eigentuemer- oder Beiratssitzung wird geplant, Beschluesse werden einem
Tagesordnungspunkt zugeordnet, Kosten und Fristen werden sichtbar und der
Umsetzungsstatus bleibt pruefbar.

## Figma

- Case-Study-Board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artefakt: `RealEstate OS Slice 4 Meeting Decision Workflow`

Das Artefakt dokumentiert den Nutzerjob von Einladung ueber Beschlussfassung
bis Protokollnachweis und Umsetzung.

## Umsetzung

Backend:
- Flyway V8 erweitert `owner_meeting` um Einladungsdatum, Rueckmeldefrist und
  Quorum-/Mehrheitsanforderung.
- `community_decision` fuehrt optionalen Versammlungsbezug, Tagesordnungspunkt,
  Umsetzungsfrist, verantwortliche Rolle und Kostenwirkung.
- Der Workspace-Service validiert, dass ein Beschluss nur auf eine Versammlung
  derselben WEG zeigen kann.
- Meeting- und Decision-DTOs liefern die neuen Workflowdaten an das Frontend.
- Backend-Tests decken den Pfad WEG anlegen -> Versammlung planen -> Beschluss
  zuordnen -> Umsetzung pruefen ab.

Frontend:
- Versammlungen koennen mit Einladungsdatum, Rueckmeldefrist,
  Mehrheitsanforderung, Status und Tagesordnung geplant werden.
- Beschluesse koennen direkt an eine Versammlung und einen Tagesordnungspunkt
  gebunden werden.
- Umsetzungsfrist, verantwortliche Rolle und Kostenwirkung sind sichtbar in der
  Beschlusssammlung.
- Dokumente koennen weiterhin als Protokollnachweis an den Beschluss gehaengt
  werden.
- Suche und Listen zeigen den fachlichen Kontext ohne Zusatznavigation.

## Akzeptanz

- Ein leerer Workspace bleibt frei von Demodaten.
- Eine Versammlung kann geplant und in der aktuellen WEG angezeigt werden.
- Ein Beschluss kann genau dieser Versammlung zugeordnet werden.
- Der Beschluss zeigt TOP, Datum, Versammlung, Frist, Verantwortlichkeit,
  Kostenwirkung und Abstimmungsergebnis.
- Der Status kann auf umgesetzt gesetzt werden und schreibt Aktivitaet/Audit.
- Ein Protokolldokument kann den Beschluss nachweisbar belegen.

## Tests

- `npm run backend:test`
- `npm run frontend:build`
- `npm run qa:local`
- `npm run ci`

Der lokale Browser-Smoke erstellt eine Versammlung, verknuepft einen Beschluss,
markiert ihn als umgesetzt und legt anschliessend das Protokoll als
Beschlussnachweis ab.

Screenshot-Artefakte:
- `output/qa/realestate-decisions-desktop.png`
- `output/qa/realestate-documents-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Naechster Slice

Slice 5 sollte Kommunikation, Aufgaben und Fristen schliessen: Eigentuemern
muessen Mitteilungen, Verantwortlichkeiten, Rueckfragen und Wiedervorlagen aus
Finanz-, Dokument- und Beschlusskontexten klar zugestellt werden.

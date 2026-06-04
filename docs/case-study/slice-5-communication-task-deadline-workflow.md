# Slice 5: Kommunikation, Aufgaben und Fristen

Stand: 4. Juni 2026

## Ziel

WEG-Verwaltung braucht nicht nur Daten, sondern verlaessliche Nacharbeit:
Eigentuemer muessen informiert werden, Beirat oder Verwaltung brauchen klare
Verantwortlichkeiten und Fristen duerfen nicht im Postfach verschwinden. Slice 5
verbindet Kommunikation deshalb mit operativen Aufgaben, Wiedervorlagen und
fachlichem Ursprung.

## Figma

- Case-Study-Board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artefakt: `RealEstate OS Slice 5 Communication Task Deadline Workflow`

Das Artefakt zeigt den Ablauf von Ausloeser ueber Mitteilung, Folgeaufgabe,
Friststeuerung und Nachweis.

## Umsetzung

Backend:
- Flyway V9 erweitert `work_task` um Verantwortlichkeit, Ursprung,
  Zielobjekt, Wiedervorlage und Abschlusszeitpunkt.
- `community_message` fuehrt Kanal, Status, Ursprung, Zielobjekt,
  Versandbereitschaft, optionalen Folgeaufgaben-Link und Versandzeitpunkt.
- Gemeinsamer `WorkContextType` modelliert manuelle, Finanz-, Dokument-,
  Beschluss- und Versammlungskontexte.
- Der Workspace-Service validiert den WEG-Kontext fuer Aufgaben und
  Mitteilungen serverseitig.
- Eine Mitteilung kann atomar eine Folgeaufgabe erzeugen.
- Insights priorisieren ueberfaellige Aufgaben und anstehende Wiedervorlagen.

Frontend:
- Aufgabenformular fuehrt Verantwortlichkeit, Ursprung und Wiedervorlage.
- Kommunikationsformular fuehrt Empfaenger, Kanal, Status, Versanddatum,
  Ursprung und optionale Folgeaufgabe.
- Listen zeigen Kontext, Status, Fristen und Folgeaufgabe ohne Zusatzklick.
- Die globale Suche findet Aufgaben und Mitteilungen ueber Kontext,
  Verantwortlichkeit, Kanal und Fristen.

## Akzeptanz

- Ein leerer Workspace bleibt frei von Demodaten.
- Eine Mitteilung kann einem Produktkontext derselben WEG zugeordnet werden.
- Eine Mitteilung kann direkt eine Folgeaufgabe mit Fälligkeit und
  Wiedervorlage erzeugen.
- Aufgaben zeigen Verantwortliche, Fristen, Ursprung und Status.
- Erledigte Aufgaben setzen `completed_at` und verschwinden aus offenen
  Kennzahlen.
- Browser-QA prueft den Pfad Versammlung -> Mitteilung -> Folgeaufgabe ->
  Statuswechsel -> Dashboard.

## Tests

- `npm run backend:test`
- `npm run frontend:build`
- `npm run qa:local`
- `npm run ci`

Screenshot-Artefakte:
- `output/qa/realestate-communication-desktop.png`
- `output/qa/realestate-dashboard-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Naechster Slice

Slice 6 sollte Rollen, Rechte, Audit und Betrieb vertiefen: granulare
Command-Rechte, sichtbare Audit-Nachweise, robuste Fehlerzustaende und
betriebliche Readiness fuer einen externen Code-Review.

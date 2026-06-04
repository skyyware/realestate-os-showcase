# Figma- und Case-Study-Workflow

Stand: 4. Juni 2026

Dieses Projekt soll nicht nur Software liefern, sondern als Case Study fuer das
Bewerbungsdeck funktionieren. Deshalb wird jede relevante Produktentscheidung
gestaltet, dokumentiert, getestet und spaeter als nachvollziehbarer
Produktentwicklungsprozess gezeigt.

## Figma Artefakte

| Artefakt | Zweck |
| --- | --- |
| Research Map | Markt, Rollen, Pain Points und Produktantworten sichtbar machen. |
| Information Architecture | Module, Navigation und Objektbeziehungen fuer WEG-Nutzer klaeren. |
| User Flows | Kritische Aufgaben vor Implementierung pruefen: WEG anlegen, Buchung buchen, Beschluss fassen, Dokument finden. |
| Wireframes | Inhalt, Reihenfolge und mobile Nutzbarkeit testen, bevor visuelles Polish kommt. |
| High-Fidelity Screens | Designsystem, Layout, Typografie, Farbe, Komponenten und leere Zustaende festhalten. |
| Implementation Review | Live-Screenshots gegen Figma pruefen und Designabweichungen dokumentieren. |

## Design-Regeln

- Jede Ansicht startet aus einem echten Nutzerjob.
- Jede Zahl hat einen Drilldown oder eine Erklaerung.
- Jede primaere Aktion ist eindeutig und fachlich benannt.
- Leere Zustaende fuehren zu Einrichtung, nicht zu Dekoration.
- Mobile Ansichten sind keine Nachtraege.
- Statusfarben sind sparsam: Gruen fuer erledigt/gesund, Amber fuer
  Klaerungsbedarf, Rot fuer Risiko, Blau fuer Information.
- Texte muessen deutsche Umlaute korrekt anzeigen und auch auf kleinen
  Viewports sauber umbrechen.

## Testprotokoll pro Design

| Test | Frage |
| --- | --- |
| Aufgabenverstaendnis | Erkennt ein Eigentuemer ohne Erklaerung, was als Naechstes zu tun ist? |
| Fachliche Vollstaendigkeit | Sind die Pflichtdaten fuer WEG, Finanzen oder Beschluss enthalten? |
| Rollenlogik | Sieht jede Rolle nur, was sie sehen und tun darf? |
| Fehler- und Leerlauf | Sind leerer Zustand, Validierung, Konflikt und Erfolg gestaltet? |
| Responsive Layout | Funktioniert die Ansicht auf Mobile, Tablet, Laptop und Desktop? |
| Accessibility | Sind Kontrast, Fokus, Labels und Tastaturfuehrung ausreichend? |
| Implementierbarkeit | Passt die UI zur bestehenden Angular/Spring-Architektur? |

## Figma Nachweis im Repo

Figma-Links werden hier gesammelt, sobald ein Artefakt erzeugt oder aktualisiert
wurde:

| Datum | Artefakt | Link | Status |
| --- | --- | --- | --- |
| 2026-06-04 | WEG Market Problem Map | [FigJam öffnen](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8?utm_source=chatgpt&utm_content=edit_in_figjam&oai_id=&request_id=d10a8ab9-bc1f-4f99-828b-94eab9cee443) | Erstellt |
| 2026-06-04 | Slice 1 WEG Onboarding And Roles | [FigJam öffnen](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Erstellt und lokal umgesetzt |
| 2026-06-04 | Slice 2 Finance Foundation | [FigJam öffnen](https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8) | Erstellt, lokal getestet und deploybereit |

## Integration ins Bewerbungsdeck

Die Case Study sollte spaeter diese Story erzaehlen:

1. Marktproblem: Verwalterknappheit, steigende Kosten, Transparenzdefizit.
2. Kundenverstehen: Rollen, Jobs, Pain Points und gesetzliche Kernpflichten.
3. Produktthese: Gefuehrte Selbstverwaltung statt generischer Verwaltungsliste.
4. Architektur: Modularer Spring/Angular/PostgreSQL-Stack passend zur
   Ausschreibung.
5. Designprozess: Figma Research Map, Flows, Screens und Review-Screenshots.
6. Umsetzung: echte Registrierung, leerer Workspace, rollenbasierte Commands,
   Audit, Finance, Beschluss, Dokumente und Kommunikation.
7. Testing: CI, lokale QA, Viewport-Pruefung, Stage-Health.
8. Ergebnis: sharebare Live-App, sharebarer Code und klare Roadmap.

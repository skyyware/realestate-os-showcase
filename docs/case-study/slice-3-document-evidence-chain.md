# Slice 3: Dokumente und Belegkette

Stand: 5. Juni 2026

## Ziel

WEG-Verwaltung scheitert im Alltag oft nicht an fehlenden Dokumenten, sondern
an fehlendem Kontext: Welche Rechnung gehoert zu welcher Buchung? Welches
Protokoll belegt welchen Beschluss? Welche Unterlage duerfen Eigentuemer,
Beirat oder Verwaltung sehen? Slice 3 macht Dokumente deshalb zu verknuepften
Nachweisen statt zu einer losen Dateiliste.

## Figma

- Case-Study-Board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artefakt: `RealEstate OS Slice 3 Documents And Evidence Chain`

Der Flow dokumentiert Import, Klassifikation, Zuordnung, Pruefung und Nutzung
von Dokumenten in Finanz-, Beschluss- und Versammlungskontexten.

## Umsetzung

Backend:
- Flyway V7 erweitert `property_document` um Status, Sichtbarkeit, Quelle,
  Beschreibung, Linktyp und Zielobjekt-ID.
- Flyway V11 erweitert `property_document` um Storage-Key, Content-Type,
  Dateigroesse, SHA-256-Checksumme und Upload-Zeitpunkt.
- Neue Typen: `DocumentStatus`, `DocumentVisibility`, `DocumentLinkType`.
- Dokumente koennen mit Finanzereignissen, Beschluessen oder Versammlungen
  derselben WEG verknuepft werden.
- `WorkspaceService` validiert jede Zuordnung serverseitig gegen die
  ausgewaehlte WEG.
- Allgemeine Dokumente duerfen kein Zielobjekt setzen; verknuepfte Dokumente
  muessen ein gueltiges Zielobjekt haben.
- `DocumentStorage` speichert echte Uploads ausserhalb der Domainmodule,
  begrenzt Dateigroessen, normalisiert Dateinamen und berechnet SHA-256.
- Downloads laufen ueber eine rollenbasierte Workspace-API mit
  Content-Disposition und Content-Type.

Frontend:
- Dokumentformular fuehrt Dokumenttyp, Status, Sichtbarkeit, Quelle,
  Zuordnung, Zielobjekt und Beschreibung.
- Der Dateiname wird aus dem echten File-Picker uebernommen; Nutzer muessen ihn
  nicht manuell eintippen.
- Zielobjekte werden aus vorhandenen Finanzereignissen, Beschluessen oder
  Versammlungen der aktuellen WEG angeboten.
- Dokumentlisten zeigen Kontext direkt sichtbar: Datei, Datum, Freigabe,
  Sichtbarkeit, Linktyp, Zielobjekt, Dateigroesse, Content-Type und Hash.
- Jede gespeicherte Datei kann direkt aus der Dokumentliste heruntergeladen
  werden.
- Suche findet Dokumente jetzt auch ueber Status, Sichtbarkeit, Quelle und
  Beschreibung.

## Akzeptanz

- Ein Finanzbeleg kann direkt an ein Finanzereignis gehaengt werden.
- Ein Protokoll kann direkt an einen Beschluss gehaengt werden.
- Dokumente koennen nicht versehentlich auf Objekte einer anderen WEG zeigen.
- Sichtbarkeit und Status sind Teil der Produktdaten, nicht nur UI-Text.
- Upload, Hash, Dateigroesse und Download sind Teil der Produktdaten.
- Der leere Workspace bleibt leer; alle Unterlagen werden bewusst angelegt.

## Tests

- `npm run backend:test`
- `npm run frontend:build`
- `npm run qa:local`
- `npm run ci`

Der lokale Browser-Smoke legt jetzt zwei verknuepfte Dokumente mit echten
Fixture-Dateien an: eine Hausmeister-Rechnung zum Finanzereignis und ein
JHV-Protokoll zum Beschluss. Beide Dateien werden nach dem Upload wieder
heruntergeladen und inhaltlich mit den Fixtures verglichen.

Screenshot-Artefakte:
- `output/qa/realestate-documents-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Naechster Slice

Slice 4 sollte Beschluss und Versammlung vertiefen: Einladung, Tagesordnung,
Beschlussvorlagen, Abstimmung, Protokoll, Umsetzungsaufgabe und Dokumentlink
als geschlossener Eigentuemerversammlungs-Workflow.

# Slice 2: Finanzraum, Hausgeld-Soll und Salden

Stand: 4. Juni 2026

## Ziel

Der Finanzslice macht aus der WEG-Struktur einen nutzbaren Arbeitsraum fuer
laufende Verwaltung. Eigentuemergemeinschaften brauchen nicht nur Kontostand
und Ruecklage, sondern nachvollziehbare Sollstellungen, offene Forderungen,
Belege, Faelligkeiten, Verteilung und einen klaren Blick auf Rueckstaende je
Einheit.

## Figma

- Case-Study-Board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artefakt: `RealEstate OS Slice 2 Finance Foundation`

Der Flow dokumentiert die fachliche Kette von Einheiten und MEA ueber
Hausgeld-Soll, Buchung, Belegkette, offene Forderung und Einheiten-Saldo bis
zur Priorisierung im Command Center.

## Umsetzung

Backend:
- Flyway V6 erweitert `finance_event` um Ereignistyp, Verteilerschluessel,
  Einheitsbezug, Faelligkeit, Zahlungstag, Gegenpartei, Belegnummer und
  Dokumentreferenz.
- Neues `house_money_assessment` speichert Hausgeld und Ruecklagenanteil je
  Einheit und Wirtschaftsjahr.
- `WorkspaceService` berechnet Einheiten-Salden aus Sollstellung und
  Eigentuemerzahlungen.
- Ausgaben und Erstattungen werden serverseitig negativ normalisiert,
  Eigentuemerzahlungen positiv. Damit bleibt die API robust, auch wenn Nutzer
  positive Betraege eingeben.
- Offene Forderungen werden aus allen Finanzereignissen der ausgewaehlten WEG
  berechnet, nicht nur aus den zuletzt sichtbaren Buchungen.

Frontend:
- Finanzbereich nutzt echte Tabs fuer Buchungserfassung und Buchungshistorie.
- Buchungsformular erfasst Ereignistyp, Kategorie, Betrag, Buchungsdatum,
  Faelligkeit, Verteilerschluessel, Einheitsbezug, Gegenpartei, Belegnummer,
  Dokumentreferenz und Status.
- Hausgeld-Soll kann je Einheit und Jahr angelegt werden.
- Einheiten-Salden zeigen Jahres-Soll, gezahlt und offen.
- Nach einer Buchung springt die Ansicht in die Historie und zeigt den
  erzeugten Belegkontext.

## Akzeptanz

- Eine WEG mit Einheiten kann Hausgeld-Sollstellungen je Einheit anlegen.
- Der Jahres-Sollbetrag setzt sich aus Hausgeld und Ruecklagenanteil zusammen.
- Eine positive Ausgaben-Eingabe wird als negativer Forderungs-/Kostenposten
  gespeichert.
- Offene Forderungen erscheinen in Kennzahlen, Command Center und
  Buchungshistorie.
- Finanzereignisse enthalten genug Kontext fuer spaetere Belegsuche,
  Dokumentenablage und Eigentuemerkommunikation.

## Tests

- `npm run backend:test`
- `npm run frontend:build`
- `npm run qa:local`
- `npm run ci`

Der lokale Browser-Smoke prueft den kompletten Weg: Registrierung,
Passwortvergabe, WEG-Anlage, Einheiten/Rollen, Hausgeld-Soll,
Einheiten-Saldo, positive Ausgaben-Eingabe mit serverseitiger
Normalisierung, Wirtschaftsplan, Dokumente, Beschluesse, Aufgaben,
Kommunikation, Workspace-Wechsel, Suche und Mobile-Dashboard.

Screenshot-Artefakte:
- `output/qa/realestate-finances-desktop.png`
- `output/qa/realestate-dashboard-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Naechster Slice

Slice 3 sollte die Dokumenten- und Belegkette vertiefen: echte Upload-Grenze,
Dokumenttypen, Zuordnung zu Finanzereignis/Beschluss/Versammlung,
Versionierung, Sichtbarkeit je Rolle und spaetere Ablage in S3-kompatiblem
Storage.

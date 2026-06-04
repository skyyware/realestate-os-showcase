# Slice 1: WEG-Onboarding und Rollen

Stand: 4. Juni 2026

## Ziel

Der erste echte Produktslice schliesst die wichtigste Demo-Luecke: Eine WEG
besteht nicht nur aus einer Immobilie, sondern aus Einheiten,
Miteigentumsanteilen, Eigentuemerinnen und Eigentuemer, Rollen und
Einladungen. Ohne diese Grundlage sind Finanzen, Beschluesse, Kommunikation
und Dokumente fachlich nicht belastbar.

## Figma

- Case-Study-Board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artefakt: `RealEstate OS Slice 1 WEG Onboarding And Roles`

Der Flow dokumentiert Registrierung, leeren Workspace, WEG-Profil, Einheiten,
MEA-Pruefung, Rollen, Einladungsstatus und Uebergang in den Finanzslice.

## Umsetzung

Backend:
- Flyway V5 erweitert `property_asset` um Wirtschaftsjahr, Ziel-Ruecklage,
  MEA-Gesamtsumme und Verwaltungsmodus.
- `owner_unit` fuehrt jetzt Eigentuemer-E-Mail, Nutzung und Stimmgewicht.
- Neues `community_member`-Modell bildet WEG-Rollen und Einladungsstatus ab.
- Service-Level-Rechte unterscheiden WEG-Admin, Verwaltung,
  Selbstverwalter, Beirat, Eigentuemer und externe Experten.
- Passwort-Aktivierung verknuepft eingeladene Mitgliedschaften automatisch.
- Dashboard liefert `members` und `readiness` fuer UI, Tests und spaetere
  Produktlogik.

Frontend:
- WEG-Anlage fragt jetzt Wirtschaftsjahr, Ziel-Ruecklage, MEA-Ziel und
  Verwaltungsmodell ab.
- Einheiten erfassen Eigentuemer-E-Mail, MEA, Stimmgewicht und Nutzung.
- Readiness-Karten zeigen MEA-Summe, Rollenstatus und Finanzraum-Bereitschaft.
- Rollen koennen aus der Einheitenansicht eingeladen werden.
- Onboarding-Checkliste fuehrt MEA-Verteilung und Rollen explizit.

## Akzeptanz

- Ein neuer Account startet leer.
- Eine WEG kann mit fachlichen Basisdaten angelegt werden.
- Die erste Einheit erzeugt eine nachvollziehbare Eigentuemerrolle.
- Rollen koennen eingeladen und im Aktivitaets-/Audit-Verlauf dokumentiert
  werden.
- Das System erkennt, ob Einheitenanzahl, MEA-Summe und Rollen bereit fuer den
  Finanzslice sind.

## Tests

- `npm run ci`
- `npm run qa:local`

Der lokale Browser-Smoke prueft Registrierung, Passwortvergabe, WEG-Anlage,
Einheiten/Rollen, Readiness, Finanzen, Dokumente, Beschluesse, Aufgaben,
Kommunikation, Workspace-Wechsel, Suche und Mobile-Dashboard.

Neue Screenshot-Artefakte:
- `output/qa/realestate-units-desktop.png`
- `output/qa/realestate-dashboard-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Naechster Slice

Slice 2 baut auf dieser Struktur auf: Finanzraum mit Buchung, Kostenart,
Belegkette, Hausgeld-Soll, Zahlungseingang, Rueckstand, Ruecklage und
Eigentuemeranteil.


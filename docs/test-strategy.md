# Test Strategy

Stand: 4. Juni 2026

## Testpyramide

- Unit-nahe Tests: E-Mail-Policy, Mail-Delivery-Fehler, externe Identity-Grenze
- Integrationstests: Spring Context, Flyway, JPA, Workspace-Service-Flow
- Architekturtests: Modulgrenzen per ArchUnit
- Frontend-Build: Angular Template- und TypeScript-Check
- Browser-Smoke: Registrierung, Passwortvergabe, leerer Workspace und Kernflows

## Kritische Produktpfade

Der lokale QA-Smoke muss diese Pfade abdecken:

- Registrierung ohne vorbefuellte Daten
- Aktivierungslink und Passwortvergabe
- Immobilie anlegen
- Einheiten und Eigentuemerstruktur erfassen
- Finanzbuchung und offenen Betrag erfassen
- Wirtschaftsplan anlegen
- Dokument ablegen
- Eigentuemerversammlung vorbereiten
- Beschluss erfassen und umsetzen
- Aufgabe erstellen, pruefen und erledigen
- Mitteilung vorbereiten
- Workspace wechseln
- Suche nutzen
- Desktop- und Mobile-Screenshot ohne sichtbare Layoutbrueche

## Definition Of Done

- `npm run ci` ist gruen.
- `npm run qa:local` ist gruen, wenn UI oder API-Flows betroffen sind.
- Neue persistente Daten haben Flyway-Migration und Service-Test.
- Neue Module verletzen keine ArchUnit-Regeln.
- Stage-Deployment prueft Healthcheck und Browser-Smoke.
- Bekannte Restrisiken werden im Abschluss genannt.

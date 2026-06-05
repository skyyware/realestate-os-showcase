# Test Strategy

Stand: 5. Juni 2026

## Testpyramide

- Unit-nahe Tests: E-Mail-Policy, Mail-Delivery-Fehler, externe Identity-Grenze
- Integrationstests: Spring Context, Flyway, JPA, Workspace-Service-Flow
- Architekturtests: Modulgrenzen per ArchUnit
- Frontend-Build: Angular Template- und TypeScript-Check
- Browser-Smoke: Registrierung, Passwortvergabe, leerer Workspace und Kernflows
- Security-Smoke: oeffentliche Auth-Endpunkte bleiben ohne Session erreichbar,
  Workspace-APIs bleiben geschuetzt

## Kritische Produktpfade

Der lokale QA-Smoke muss diese Pfade abdecken:

- Registrierung ohne vorbefuellte Daten
- Aktivierungslink und Passwortvergabe
- Passwort-Reset ueber Login-Maske
- Immobilie anlegen
- Einheiten und Eigentuemerstruktur erfassen
- Finanzbuchung und offenen Betrag erfassen
- Wirtschaftsplan anlegen
- Dokument hochladen, speichern und herunterladen
- Eigentuemerversammlung vorbereiten
- Beschluss erfassen und umsetzen
- Aufgabe erstellen, bearbeiten, pruefen, erledigen und loeschen
- Mitteilung vorbereiten
- Nutzerrolle aendern, Status setzen und Zugriff deaktivieren
- Schnellzugriff pro Nutzer konfigurieren
- Workspace wechseln
- Suche nutzen
- Desktop- und Mobile-Screenshot ohne sichtbare Layoutbrueche
- Dateiinhalt nach Download mit der hochgeladenen Fixture vergleichen

## Definition Of Done

- `npm run ci` ist gruen.
- `npm run qa:local` ist gruen, wenn UI oder API-Flows betroffen sind.
- Neue persistente Daten haben Flyway-Migration und Service-Test.
- Neue Module verletzen keine ArchUnit-Regeln.
- Stage-Deployment prueft Healthcheck und Browser-Smoke.
- Bekannte Restrisiken werden im Abschluss genannt.

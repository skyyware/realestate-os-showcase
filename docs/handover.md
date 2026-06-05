# Handover Runbook

Stand: 5. Juni 2026

## Lokaler Start

```bash
docker compose up -d postgres
cd backend && ./mvnw spring-boot:run
cd frontend && npm run start
```

Optional fuer Identity-Arbeit:

```bash
docker compose --profile identity up -d keycloak
```

## Wichtige Umgebungsvariablen

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `REALESTATE_PUBLIC_BASE_URL`
- `REALESTATE_JWT_SECRET`
- `REALESTATE_IDENTITY_MODE`
- `REALESTATE_KEYCLOAK_ISSUER_URI`
- `REALESTATE_KEYCLOAK_CLIENT_ID`
- `REALESTATE_MAIL_ENABLED`
- `REALESTATE_MAIL_FROM`
- `REALESTATE_MAIL_FROM_NAME`
- `REALESTATE_DOCUMENT_STORAGE_ROOT`
- `REALESTATE_DOCUMENT_STORAGE_MAX_FILE_SIZE_BYTES`

Secrets werden nicht im Repo gespeichert.

## Stage-Release

1. `npm run ci`
2. `npm run backend:package`
3. `npm run frontend:build`
4. Artefakte nach `/srv/www/realestate.stage.dev/releases/<release>` kopieren.
5. Symlink `current` aktualisieren.
6. `sudo systemctl restart realestate-api.service`
7. Healthcheck, oeffentlichen Passwort-Reset-Endpunkt und Browser-Smoke
   ausfuehren.

## Uebernahme-Checkliste

- Repo klonen und lokale CI ausfuehren.
- `.env.example` gegen eigene Secrets/Env-Dateien mappen.
- Keycloak-Realm, Client und Rollen definieren.
- Aurora-Parameter, Backups und Deletion Protection bestaetigen.
- SES-Domain verifizieren.
- Dokumentablage nutzt lokal/Stage einen Serverpfad mit Storage-Key,
  Content-Type, Dateigroesse und SHA-256; produktiv kann die Boundary auf S3
  oder kompatiblen Object Storage zeigen.
- Monitoring: API-Health, Fehlerquote, Mail-Delivery, DB-Verbindungen,
  Speicherauslastung und Antwortzeiten.

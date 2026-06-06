# Handover Runbook

Last updated: 2026-06-06

This runbook is for a developer or operator taking over RealEstate OS locally,
on stage, or as a managed-service production target.

## Local Start

```bash
docker compose up -d postgres
cd backend && ./mvnw spring-boot:run
cd frontend && npm run start
```

Optional identity work:

```bash
docker compose --profile identity up -d keycloak
```

## Important Environment Variables

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

Secrets are never stored in the repository.

## Stage Release

1. Run `npm run ci`.
2. Run `npm run backend:package`.
3. Ensure the frontend build in `frontend/dist/frontend/browser` is current.
4. Copy artifacts to `/srv/www/realestate.stage.dev/releases/<release>`.
5. Update the `current` symlink.
6. Restart `realestate-api.service`.
7. Verify health, public auth endpoints, and browser smoke behavior.

Stage health:

```bash
curl -fsS https://realestate.stage.dev/actuator/health
```

## Takeover Checklist

- Clone the repository and run local CI.
- Map `.env.example` or environment documentation to secure local files.
- Define Keycloak realm, client, and role mapping if OIDC mode is used.
- Confirm database parameters, backups, retention, and deletion protection.
- Verify SES domain or SMTP provider for transactional mail.
- Confirm document storage root, storage key format, content type, file size,
  and SHA-256 behavior.
- Decide when the storage boundary should move from disk to S3-compatible object
  storage.
- Monitor API health, error rate, mail delivery, database connections, disk
  usage, and response time.

## Operational Risk Notes

- Local and stage identity are app-native for testability; production should use
  the OIDC boundary.
- Document storage is abstracted but not yet a full retention/compliance system.
- Annual closing and asset report are product roadmap items, not complete
  accounting automation.
- The product supports WEG workflows and evidence; it does not replace legal or
  tax advice.

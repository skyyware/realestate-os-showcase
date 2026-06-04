# AGENTS.md

Diese Datei ist die Arbeitsanweisung fuer Coding-Agenten in RealEstate OS.

## Ziel

Baue die Anwendung wie eine intern uebernehmbare Product-Engineering-Codebasis:
Java 21+, Spring Boot, Angular, PostgreSQL/Aurora, Keycloak/OIDC und AWS
Managed Services, modular und ohne unnoetige Abstraktion.

## Vor Jeder Aenderung

- Lies `README.md`, `docs/architecture.md` und betroffene Dateien.
- Pruefe `git status --short --branch`.
- Suche mit `rg` oder `rg --files`.
- Schuetze fremde lokale Aenderungen.
- Schreibe keine Secrets, SMTP-Daten, Tokens oder privaten Dokumente ins Repo.

## Architekturregeln

- Domainmodule haengen nicht von `workspace`, `security`, `mail` oder Delivery
  Details ab.
- Schreibende Workspace-Commands muessen rollenbasiert geschuetzt sein.
- Persistente Fachobjekte brauchen Flyway-Migration, Repository, Service-Command,
  Activity-Eintrag und Audit-Log.
- Frontend-Formulare muessen echte API-Commands ausloesen; kein lokales
  Browser-only-Feature fuer produktrelevante Daten.
- Neue WEG-Funktionalitaet muss in den QA-Smoke aufgenommen werden.

## Standardchecks

```bash
npm run ci
npm run qa:local
```

Bei Deployment:

```bash
curl -fsS https://realestate.stage.dev/actuator/health
```

## Nicht Tun

- Keine Demo-Seeds in produktive Workspaces.
- Keine Secrets im Git.
- Keine Tests loeschen, um Gruen zu bekommen.
- Keine Framework-lastige Architektur, wenn ein modularer Monolith reicht.
- Keine finale Erfolgsmeldung ohne echte Verifikation.

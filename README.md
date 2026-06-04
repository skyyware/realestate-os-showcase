# RealEstate OS

Eine shipbare Product-Engineering-Codebasis für moderne digitale WEG-Verwaltung.

## Live

- Stage: https://realestate.stage.dev
- GitHub: https://github.com/skyyware/realestate-os-showcase
- Lokal: https://realestate.localhost

## Stack

- Java 21 target, Spring Boot 3.5, Spring Security, JPA, Flyway, Mail, Actuator
- Angular 21, standalone components, typed reactive forms, functional HTTP interceptor
- PostgreSQL 16 lokal und Aurora-kompatibles Schema
- Keycloak/OIDC-ready Identity Boundary via `docker-compose --profile identity`
- Onboarding mit Registrierung, E-Mail-Link, Passwortvergabe und leerem Workspace
- Produktflows für Immobilien, Einheiten, Finanzen, Hausgeld-Soll, Einheitensalden, Wirtschaftspläne, Versammlungen, Kommunikation, Aufgaben, Dokumente und Aktivität
- Rollenbasierte Workspace-Commands und Audit-Log für schreibende Vorgänge
- Operatives Command Center mit priorisierten nächsten Schritten je Immobilie
- Beschluss-Sammlung mit Datum, Ort, Wortlaut, Abstimmungsergebnis und Umsetzungsstatus
- Aufgaben-Lifecycle von offen über Prüfung bis erledigt mit Audit-Trail
- Consumer-grade UI mit ruhiger Finanz-/Eigentümer-Optik, leerem Startzustand und responsiver Arbeitsfläche

## Lokal starten

```bash
createdb realestate 2>/dev/null || true
cd backend && ./mvnw spring-boot:run
cd ../frontend && npm run start
```

Frontend: `http://localhost:4200`  
API: `http://localhost:8098/actuator/health`

Der Apache-VHost für den gebauten Stand nutzt `https://realestate.localhost`
und proxyt `/api` auf das Backend.

## CI

```bash
npm install
npm run ci
```

Der CI-Befehl testet das Backend und baut das Angular-Frontend.

## Registrierung testen

Lokal ist Mail standardmäßig deaktiviert. Die API gibt deshalb einen lokalen
Set-Password-Link zurück. Auf Stage werden SMTP-Secrets nur als Env-Datei auf
dem Server gesetzt.

Stage-Flow:

1. https://realestate.stage.dev öffnen.
2. Name, E-Mail und Organisation eintragen.
3. Aktivierungslink aus der E-Mail öffnen.
4. Passwort vergeben und das Dashboard testen.

Lokaler Browser-Smoke:

```bash
npm run qa:local
```

Der Smoke registriert einen frischen lokalen Nutzer, setzt ein Passwort, baut
einen Workspace ohne Seed-Daten auf, prüft die Empfehlungen, schließt eine
Aufgabe ab, wechselt zwischen zwei Immobilien und schreibt Screenshots nach
`output/qa`.

## Architektur

Siehe [docs/architecture.md](docs/architecture.md).

Weitere Übergabeartefakte:

- [docs/research/weg-market-2026.md](docs/research/weg-market-2026.md)
- [docs/product/weg-customer-problem-map.md](docs/product/weg-customer-problem-map.md)
- [docs/product/weg-product-brief.md](docs/product/weg-product-brief.md)
- [docs/design/figma-case-study-workflow.md](docs/design/figma-case-study-workflow.md)
- [docs/case-study/slice-1-weg-onboarding.md](docs/case-study/slice-1-weg-onboarding.md)
- [docs/case-study/slice-2-finance-foundation.md](docs/case-study/slice-2-finance-foundation.md)
- [docs/adr/0001-modular-monolith.md](docs/adr/0001-modular-monolith.md)
- [docs/test-strategy.md](docs/test-strategy.md)
- [docs/handover.md](docs/handover.md)
- [infra/aws](infra/aws)

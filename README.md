# RealEstate OS Showcase

Eine sharebare Bewerbungscodebasis für die dotega Senior-Engineer-Rolle.

## Live

- Stage: https://realestate.stage.dev
- GitHub: https://github.com/skyyware/realestate-os-showcase
- Bewerbungs-PDF: https://realestate.stage.dev/docs/sascha-dobrochynskyy-dotega-bewerbung.pdf
- Lokal: https://realestate.localhost

## Stack

- Java 21 target, Spring Boot 3.5, Spring Security, JPA, Flyway, Mail, Actuator
- Angular 21, standalone components, typed reactive forms, functional HTTP interceptor
- PostgreSQL 16 lokal und Aurora-kompatibles Schema
- Keycloak-ready Identity Boundary via `docker-compose --profile identity`
- SMTP-Onboarding mit Registrierung, E-Mail-Link und Passwortvergabe

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

Der CI-Befehl testet das Backend, baut das Angular-Frontend und erzeugt das
Bewerbungs-PDF unter `output/pdf/sascha-dobrochynskyy-dotega-bewerbung.pdf`.

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

Der Smoke registriert einen frischen lokalen Nutzer, setzt ein Passwort, legt
eine Aufgabe an und schreibt Screenshots nach `output/qa`.

## Architektur

Siehe [docs/architecture.md](docs/architecture.md).

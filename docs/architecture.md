# Architecture Notes

RealEstate OS ist als produktionsnaher, aber kleiner Vertical-Slice gebaut.
Der Slice orientiert sich an dotegas Produktversprechen: digitale
WEG-Selbstverwaltung, Transparenz, rechtssichere Ablaufe, Finanzueberblick und
weniger manueller Verwaltungsaufwand. Die Umsetzung folgt dem SKYYWARE-Anspruch,
qualitativ, pragmatisch und ohne unnoetige Komplexitaet zu liefern.

## Backend

Das Backend ist bewusst modular geschnitten:

- `identity`: Registrierung, Passwort-Setup, Session/JWT
- `property`: Immobilien, Einheiten, Eigentuemerdaten
- `finance`: Buchungsfeed und Zahlungsuebersicht
- `task`: Aufgabensteuerung
- `activity`: Audit-Trail
- `workspace`: produktorientierte Read-/Command-API fuer das Frontend
- `security`, `mail`, `config`, `common`: Infrastruktur und Querschnitt

Die Module enthalten jeweils ihre JPA-Entities und Repositories. Die API wird
nicht direkt aus Entities gespeist, sondern ueber DTOs/Records in Services.

## Identity

Die Demo nutzt eine app-native Registrierung mit einmaligem Token, BCrypt und
kurzlebigem HMAC-JWT. Das macht die Anwendung lokal und auf Stage direkt
testbar. Die Boundary ist so gehalten, dass sie in einer internen Uebernahme
gegen Keycloak/OIDC ausgetauscht werden kann:

- Token-Validierung sitzt zentral in `security`
- User-Kontext wird nur als Principal ins Produkt gereicht
- `docker-compose --profile identity` startet Keycloak fuer Integrationsarbeit

## Database

Flyway verwaltet das Schema. PostgreSQL wird lokal genutzt; die SQL-Typen und
Constraints sind Aurora-PostgreSQL-kompatibel. CI nutzt H2 im PostgreSQL-Modus,
damit Pull Requests ohne externen Service validierbar bleiben.

## Mail

SMTP-Konfiguration kommt ausschliesslich ueber Environment-Variablen. Reale
Zugangsdaten werden nicht in Git gespeichert. Lokal gibt die API bei
deaktiviertem Mailversand einen Setup-Link zurueck; Stage versendet echte
Transaktionsmails.

## AWS/Stage

Der Stage-Betrieb ist bewusst pragmatisch:

- statisches Angular-Bundle ueber Apache
- `/api` Reverse Proxy auf Spring Boot
- systemd-Service fuer das JAR
- PostgreSQL auf dem Host oder managed kompatibel
- Secrets als restriktive Env-Datei
- TLS ueber Let's Encrypt fuer `realestate.stage.dev`

Das ist nah an einem Seed/Pre-Series-A Setup: wenig Over-Engineering, aber
klare Upgrade-Pfade zu ECS/App Runner, Aurora, SES und Keycloak.

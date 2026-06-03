# Architecture Notes

RealEstate OS ist als produktionsnaher, aber kleiner Vertical-Slice gebaut.
Der Slice orientiert sich an dotegas Produktversprechen: digitale
WEG-Selbstverwaltung, Transparenz, rechtssichere Abläufe, Finanzüberblick und
weniger manueller Verwaltungsaufwand. Die Umsetzung folgt dem SKYYWARE-Anspruch,
qualitativ, pragmatisch und ohne unnötige Komplexität zu liefern.

## Backend

Das Backend ist bewusst modular geschnitten:

- `identity`: Registrierung, Passwort-Setup, Session/JWT
- `property`: Immobilien, Einheiten, Eigentümerdaten
- `finance`: Buchungsfeed und Zahlungsübersicht
- `task`: Aufgabensteuerung
- `activity`: Audit-Trail
- `workspace`: produktorientierte Read-/Command-API für das Frontend
- `security`, `mail`, `config`, `common`: Infrastruktur und Querschnitt

Die Module enthalten jeweils ihre JPA-Entities und Repositories. Die API wird
nicht direkt aus Entities gespeist, sondern über DTOs/Records in Services.

## Workspace Intelligence

Das Dashboard liefert nicht nur Rohlisten, sondern verdichtete Arbeitssignale:

- `PortfolioMetrics` fasst Kontostand, Rücklage, Forderungen und offene Aufgaben zusammen.
- `InsightView` erzeugt pro ausgewählter Immobilie priorisierte nächste Schritte.
- Task-Statuswechsel werden als Command verarbeitet und landen im Audit-Trail.

Damit bleibt das Frontend dünn: Es rendert Handlungsvorschläge, springt in den
passenden Arbeitsbereich und aktualisiert den Workspace nach jeder Aktion aus
einer konsistenten Backend-Sicht.

## Product Design

Die Oberfläche ist als verbrauchernahes Eigentümerprodukt gestaltet, nicht als
reines Admin-Tool. Die visuelle Sprache nutzt ruhige Finanzflächen, klare
Arbeitskarten, warmes Grün für Fortschritt, Amber für Klärungsbedarf und ein
priorisiertes Command Center. Der leere Workspace bleibt absichtlich leer:
Nutzer legen Immobilie, Einheiten, Finanzen, Aufgaben und Dokumente selbst an,
damit keine Demodaten mit echten Entscheidungsdaten verwechselt werden.

## Identity

Die Anwendung nutzt eine app-native Registrierung mit einmaligem Token, BCrypt und
kurzlebigem HMAC-JWT. Das macht die Anwendung lokal und auf Stage direkt
testbar. Die Boundary ist so gehalten, dass sie in einer internen Übernahme
gegen Keycloak/OIDC ausgetauscht werden kann:

- Token-Validierung sitzt zentral in `security`
- User-Kontext wird nur als Principal ins Produkt gereicht
- `docker-compose --profile identity` startet Keycloak für Integrationsarbeit

## Database

Flyway verwaltet das Schema. PostgreSQL wird lokal genutzt; die SQL-Typen und
Constraints sind Aurora-PostgreSQL-kompatibel. CI nutzt H2 im PostgreSQL-Modus,
damit Pull Requests ohne externen Service validierbar bleiben.

## Mail

SMTP-Konfiguration kommt ausschließlich über Environment-Variablen. Reale
Zugangsdaten werden nicht in Git gespeichert. Lokal gibt die API bei
deaktiviertem Mailversand einen Setup-Link zurück; Stage versendet echte
Transaktionsmails.

## AWS/Stage

Der Stage-Betrieb ist bewusst pragmatisch:

- statisches Angular-Bundle über Apache
- `/api` Reverse Proxy auf Spring Boot
- systemd-Service für das JAR
- PostgreSQL auf dem Host oder managed kompatibel
- Secrets als restriktive Env-Datei
- TLS über Let's Encrypt für `realestate.stage.dev`

Das ist nah an einem Seed/Pre-Series-A Setup: wenig Over-Engineering, aber
klare Upgrade-Pfade zu ECS/App Runner, Aurora, SES und Keycloak.

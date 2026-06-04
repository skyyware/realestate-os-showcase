# Architecture Notes

RealEstate OS ist als produktionsnaher, aber kleiner Vertical-Slice gebaut.
Der Slice orientiert sich an dotegas Produktversprechen: digitale
WEG-Selbstverwaltung, Transparenz, rechtssichere Abläufe, Finanzüberblick und
weniger manueller Verwaltungsaufwand. Die Umsetzung folgt dem SKYYWARE-Anspruch,
qualitativ, pragmatisch und ohne unnötige Komplexität zu liefern.

## Backend

Das Backend ist bewusst modular geschnitten:

- `identity`: Registrierung, Passwort-Setup, Session/JWT
- `property`: WEG-Profil, Einheiten, MEA, Rollen, Mitgliedschaften und
  Einladungsstatus
- `finance`: Buchungsfeed und Zahlungsübersicht
- `planning`: Wirtschaftspläne, Budgets und Rücklagenzuführung
- `task`: Aufgabensteuerung
- `meeting`: Eigentümerversammlungen, Tagesordnung und Einladungsstatus
- `communication`: vorbereitete Mitteilungen an Eigentümer, Beirat oder Verwaltung
- `audit`: technische Nachvollziehbarkeit schreibender Commands
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

## WEG Product Fit

Der nächste produktive Kern ist die Beschluss-Sammlung: Eigentümergemeinschaften
brauchen nachvollziehbare Entscheidungen mit Datum, Ort, Wortlaut,
Abstimmungsergebnis und späterer Umsetzung. RealEstate OS bildet diesen Weg als
eigenen Vertical Slice ab:

- Beschluss erfassen und der ausgewählten Immobilie zuordnen
- Status von Entwurf über beschlossen/abgelehnt bis umgesetzt führen
- Abstimmzahlen getrennt erfassen
- Audit-Trail bei Erfassung und Statuswechsel schreiben
- Command Center warnt, wenn eine Beschluss-Sammlung fehlt oder beschlossene
  Punkte noch nicht umgesetzt sind

Damit rückt die App näher an den Kern realer WEG-Verwaltung: nicht nur Daten
ablegen, sondern Entscheidungen sauber dokumentieren und in Arbeit übersetzen.

## Slice 1: WEG-Onboarding und Rollen

Die WEG-Struktur wurde als tragender Produktslice vertieft:

- WEG-Profil mit Wirtschaftsjahr, Verwaltungsmodus, Ziel-Ruecklage und
  erwarteter MEA-Summe
- Einheiten mit Eigentuemer-E-Mail, Nutzung, MEA und Stimmgewicht
- `community_member` als Rollen- und Einladungsmodell je WEG
- Dashboard-Readiness fuer Einheitenanzahl, MEA-Summe, Rollenstatus und
  Finanzraum-Bereitschaft
- Service-Level-Rechte fuer Admin, Selbstverwalter, Verwaltung und Beirat
- automatische Aktivierung eingeladener Mitgliedschaften beim Passwort-Setup

Dieser Slice ist die fachliche Voraussetzung fuer den naechsten Finanzraum:
Hausgeld, Rueckstaende, Ruecklage und Eigentuemeranteile koennen erst dann
robust berechnet werden, wenn Einheiten und Verteilung belastbar sind.

## Identity

Die Anwendung nutzt lokal eine app-native Registrierung mit einmaligem Token,
BCrypt und kurzlebigem HMAC-JWT. Das macht die Anwendung lokal und auf Stage
direkt testbar. Zusätzlich ist die produktive Identity Boundary Keycloak/OIDC-
fähig:

- Token-Validierung sitzt zentral in `security`
- User-Kontext wird nur als Principal ins Produkt gereicht
- `docker-compose --profile identity` startet Keycloak für Integrationsarbeit
- `REALESTATE_IDENTITY_MODE=keycloak` aktiviert den OIDC-Resource-Server
- Keycloak-Rollen werden auf `OWNER_ADMIN`, `PROPERTY_MANAGER` und
  `BOARD_MEMBER` gemappt
- externe Subjects werden in `app_user.identity_provider` und
  `app_user.external_subject` nachvollziehbar gespeichert

## Database

Flyway verwaltet das Schema. PostgreSQL wird lokal genutzt; die SQL-Typen und
Constraints sind Aurora-PostgreSQL-kompatibel. CI nutzt H2 im PostgreSQL-Modus,
damit Pull Requests ohne externen Service validierbar bleiben.

## Mail

SMTP-Konfiguration kommt ausschließlich über Environment-Variablen. Reale
Zugangsdaten werden nicht in Git gespeichert. Lokal gibt die API bei
deaktiviertem Mailversand einen Setup-Link zurück; Stage versendet echte
Transaktionsmails.

## Datenhaltung, Rollen und Audit

Produktrelevante Daten liegen serverseitig in PostgreSQL/Aurora-kompatiblen
Tabellen. Browser-only-Zustand ist nur für lokale UI-Präferenzen erlaubt.
Schreibende Workspace-Commands sind rollenbasiert geschützt und schreiben zwei
Spuren:

- `activity_event` für den sichtbaren Produktverlauf
- `audit_log` für technische Nachvollziehbarkeit und spätere Betreiberprüfungen

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

Ein Terraform-Blueprint liegt unter `infra/aws`. Er modelliert Aurora
PostgreSQL, S3-Dokumentablage, SES, CloudWatch und App Runner als Managed-
Service-Zielbild.

## ADRs und Betrieb

- `docs/adr/0001-modular-monolith.md`
- `docs/test-strategy.md`
- `docs/handover.md`

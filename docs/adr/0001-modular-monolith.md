# ADR 0001: Modularer Monolith Statt Frueher Microservices

Status: accepted  
Datum: 4. Juni 2026

## Kontext

Die Kundenvision beschreibt eine moderne, aber pragmatische Product-Engineering-
Organisation: Java 21+, Spring Boot, Angular, PostgreSQL/Aurora, Keycloak und AWS
Managed Services. Gleichzeitig soll Over-Engineering vermieden und eine
Agentur-Codebasis schrittweise intern uebernommen werden.

## Entscheidung

RealEstate OS bleibt ein modularer Monolith:

- ein Spring-Boot-Deployment fuer die API
- ein Angular-Frontend
- fachliche Module nach WEG-Domain: `property`, `finance`, `task`, `document`,
  `decision`, `planning`, `meeting`, `communication`, `identity`, `audit`
- `workspace` als produktorientierte Application/API-Schicht
- Infrastruktur wie `security`, `mail` und AWS bleibt ausserhalb der Domain

ArchUnit-Tests sichern, dass Domainmodule nicht von der Workspace-API oder
Delivery-Infrastruktur abhaengen.

## Konsequenzen

Positiv:

- leicht lokal startbar
- einfach zu debuggen und zu deployen
- klare fachliche Grenzen ohne verteilte Systemkomplexitaet
- spaeter extrahierbare Module, wenn Produktlast es rechtfertigt

Tradeoff:

- Teamdisziplin und Architekturtests sind wichtiger, weil keine physischen
  Service-Grenzen erzwingen, was fachlich getrennt bleiben soll.

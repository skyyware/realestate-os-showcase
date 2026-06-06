# ADR 0001: Modular Monolith

Status: accepted  
Date: 2026-06-04

## Context

The product target is a modern but pragmatic engineering setup: Java 21+,
Spring Boot, Angular, PostgreSQL/Aurora, Keycloak/OIDC, and AWS managed-service
readiness. The codebase should remain easy to run, review, and take over by a
small internal product team.

Distributed services would add operational and coordination cost before the
product has enough scale to justify them.

## Decision

RealEstate OS stays a modular monolith:

- one Spring Boot API deployment
- one Angular frontend
- domain modules by WEG product area: `property`, `finance`, `task`,
  `document`, `planning`, `meeting`, `communication`, `identity`, `audit`
- `workspace` as product-oriented application/API layer
- infrastructure such as `security`, `mail`, and AWS boundaries outside domain
  logic

ArchUnit tests protect domain modules from depending on workspace API or
delivery infrastructure.

## Consequences

Positive:

- easy local startup
- easier debugging
- simple stage deployment
- clear domain boundaries without distributed-system overhead
- modules can be extracted later when product load demands it

Tradeoff:

- Team discipline and architecture tests matter more because physical service
  boundaries do not enforce separation.

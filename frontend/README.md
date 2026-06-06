# Frontend Notes

The frontend is an Angular 21 application for the RealEstate OS workspace. It
uses standalone components, typed reactive forms, and a functional HTTP
interceptor.

## Useful Commands

```bash
npm run start
npm run build
```

From the repository root:

```bash
npm run frontend:build
npm run qa:local
npm run ci
```

## Local Development

```bash
cd frontend
npm run start
```

Open `http://localhost:4200`.

The built app can also be served at `https://realestate.localhost` through the
local Apache vhost, with `/api` proxied to the Spring Boot backend.

## UI Responsibilities

- Render backend workspace state.
- Submit typed commands for product changes.
- Keep product data server-backed.
- Support registration, login, password setup, password reset, dashboard,
  properties, units, finance, documents, meetings, decisions, tasks,
  communication, activity, audit, and settings.
- Stay usable across desktop and mobile viewports.

## Design Rules

- Use clear product language.
- Keep primary actions visible and meaningful.
- Prevent text overlap and layout shifts in responsive views.
- Make empty states useful.
- Show status, responsibility, deadlines, and evidence where users make
  decisions.

## Verification

```bash
npm run build
```

For product or UI changes, also run from the repository root:

```bash
npm run qa:local
```

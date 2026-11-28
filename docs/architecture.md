# Rebuild architecture

One React/TypeScript frontend, one Java Spring Boot backend, and one independently
configured PostgreSQL database. The original product supplies the UI/workflow
reference, not the backend or database.

```text
Browser (React)
  -> same-origin /api requests with session cookie and CSRF header
  -> Vite proxy in development / reverse proxy in production
  -> Spring Security
  -> feature controller -> service -> JPA repository -> PostgreSQL
```

## Backend

Feature packages group controllers, DTOs, services, entities and repositories:

- `auth` / `users`: sessions and accounts.
- `notes` / `reviewers`: documents, imports and linked study guides.
- `quizzes` / `attempts`: authoring/taking and persisted results.
- `flashcards`: cards, review history, spaced repetition and progress.
- `shared.security` / `shared.error`: common security and HTTP error contracts.

The application class stays above the features; tests mirror their packages.
Table/column mappings and endpoint URLs are retained. Cross-feature imports are
explicit; keep domain rules in their owning feature.

Books, sharing, workspaces, integrations, notifications, preferences and offline
sync become sibling packages when implemented. Avoid empty services/microservices
for planned domains.

## Frontend

`app/` composes the application; `features/` owns screens and domain API adapters.
`shared/api/` handles transport/session/CSRF. `shared/ui/` and `shared/styles/`
hold shared components and styles.

The fresh frontend contains a minimal entry screen. Old authentication and study
screens are intentionally removed. New screens must reflect real backend
capabilities and preserve the original theme and workflows.

## Data and security

- Database credentials/provider secrets remain on the backend.
- Authentication uses server-side sessions and HttpOnly cookies.
- Writes obtain CSRF tokens from `/api/auth/csrf`; authentication rotates tokens.
- Services check the authenticated owner for study content.
- Tests use in-memory H2; PostgreSQL configuration remains separate.
- Existing Hibernate schema updates remain for development. Production migrations
  and the expanded schema are separate tasks.

See [backend setup](../backend/README.md) and [frontend setup](../frontend/README.md).
Earlier course-project documents and `docs/reference/` guides are historical.
The replacement board defines the current rebuild scope.
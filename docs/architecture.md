# Architecture

## Proposed design

React JavaScript browser UI → Spring Boot REST API → Supabase PostgreSQL database (hosted).

Use a single Spring Boot application with controller, service, repository, and entity packages. React has pages/components and one small API helper; avoid extra architectural layers. The Supabase PostgreSQL database persists data between backend restarts and is shared across the team. Run one backend instance and use sample data only. A local PostgreSQL database is supported as a backup; see `backend/memoria/src/SETUP.md`.

## Folder layout

- `frontend/memoria/`: planned Vite React JavaScript application.
- `backend/memoria/`: planned Spring Boot Maven project.
- `tests/`: shared manual acceptance checklist; generated Java tests belong in `backend/memoria/src/test/java`.
- `docs/`: requirements, architecture, backlog, plans, and evidence.
- `.github/`: GitHub issue and pull request templates.

The frontend and backend scaffolds are present. See the [backend run guide](../backend/memoria/README.md) for startup and health verification. Application features below remain planned.

## Local configuration

After scaffolding, add this proxy to the Vite configuration's existing `defineConfig` object (preserve its React plugin):

```javascript
server: {
  proxy: {
    '/api': { target: 'http://localhost:8080', changeOrigin: true }
  }
}
```

Frontend calls `/api/...` using `credentials: 'include'`. Keep requests on the Vite origin through the proxy to avoid unnecessary CORS setup.

Configure Spring using `application.properties`, with environment overrides. Set the three `SPRING_DATASOURCE_*` environment variables before starting — see `backend/memoria/src/SETUP.md` for exact steps.

```properties
server.port=${SERVER_PORT:8080}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/memoria}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:memoria}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax
```

The default profile connects to a local PostgreSQL instance. Activate `supabase` profile (`SPRING_PROFILES_ACTIVE=supabase`) to use the hosted Supabase database; the same three variables are still required. Tests always use H2 in-memory (`@ActiveProfiles("test")`). `ddl-auto=update` is a prototype convenience only. A root `.env` is not loaded automatically; Vite separately reads `frontend/memoria/.env.local`. Variables beginning `VITE_` are browser-visible and must contain no secrets.

## Sessions and authorization

Use Spring Security session authentication with BCrypt. Provide registration, form-encoded login handled by Spring Security at `/api/auth/login`, logout, and current-user endpoints. Add a small `/api/auth/csrf` endpoint returning the session CSRF token's value and header name; load it before each modifying request, including login/logout, and reload after authentication changes. Keep CSRF protection enabled and configure API failures as JSON 401/403 rather than HTML redirects. Keep secure cookies enabled if HTTPS hosting is later added.

Derive the user ID from the authenticated session. Repository/service queries must combine resource ID with owner ID. Never accept client-supplied ownership or trust a frontend-only access check.

## Minimal entities

| Entity | Main fields | Relationships |
| --- | --- | --- |
| User | id, name, email (unique), passwordHash | Owns notes, reviewers, quizzes, attempts |
| Note | id, ownerId, title, body, updatedAt | User → many notes |
| Reviewer | id, ownerId, noteId, title, body | Note → many reviewers |
| Quiz | id, ownerId, reviewerId, title | Reviewer → many quizzes |
| Question | id, quizId, text, choiceA–D, correctChoice | Quiz → many questions |
| Attempt | id, ownerId, quizId, submittedAt, correctCount, totalCount, submissionToken (unique) | Quiz → many attempts |
| AttemptAnswer | id, attemptId, questionId, selectedChoice (nullable) | Attempt → many answers |

Check that parent objects belong to the same user. Save a quiz and its questions transactionally. Use a transaction for grading and saving an attempt. Enforce the edit/delete restrictions in requirements to retain history without snapshots or complex versioning.

## Planned API surface

| Route | Purpose |
| --- | --- |
| GET /api/auth/csrf | Obtain CSRF token |
| POST /api/auth/register | Register account |
| POST /api/auth/login | Start session |
| POST /api/auth/logout | Invalidate session |
| GET /api/auth/me | Current user |
| GET/POST /api/notes | List/create notes |
| GET/PUT/DELETE /api/notes/{id} | Read/update/delete owned note |
| GET/POST /api/reviewers | List/create reviewers |
| GET/PUT/DELETE /api/reviewers/{id} | Read/update/delete owned reviewer |
| GET/POST /api/quizzes | List/create quizzes |
| GET/PUT/DELETE /api/quizzes/{id} | Authoring operations; owned content only |
| GET /api/quizzes/{id}/take | Questions without answer keys; issue submission token |
| POST /api/quizzes/{id}/attempts | Validate token, grade on server, save result |
| GET /api/attempts | Personal history |
| GET /api/attempts/{id} | Saved result and answer review |

These are implementation contracts, not existing endpoints. No external AI API, cloud service, or messaging system is needed.

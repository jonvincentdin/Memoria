# Architecture

## Proposed design

React JavaScript browser UI → Spring Boot REST API → Supabase PostgreSQL database (hosted).

Use a single Spring Boot application with controller, service, repository, and entity packages. React has pages/components and one small API helper; avoid extra architectural layers. The Supabase PostgreSQL database persists data between backend restarts and is shared across the team. Run one backend instance and use sample data only. A local PostgreSQL database is supported as a backup; see `SETUP.md` at the repo root. Account registration, login, logout, and current-user lookup are implemented; note and quiz APIs remain planned.

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

Authentication uses Spring Security sessions and BCrypt. `POST /api/auth/register` accepts a JSON name, email, and password and starts a session; `POST /api/auth/login` accepts a JSON email and password; `POST /api/auth/logout` invalidates the session; and `GET /api/auth/me` returns the authenticated user's ID, name, email, and creation time without the password hash. Email is normalized to lowercase and unique in the database.

CSRF protection applies to all modifying requests. Fetch `GET /api/auth/csrf` before each POST and send its token in the returned header name, including registration, login, and logout. Authentication rotates the session ID and clears the old CSRF token; fetch a fresh token for the next modifying request. The session cookie is HttpOnly and SameSite=Lax. Set `SESSION_COOKIE_SECURE=true` when serving over HTTPS. API authentication failures are JSON 401 and CSRF/access failures are JSON 403, not HTML redirects.

Derive the user ID from the authenticated session. Repository/service queries must combine resource ID with owner ID. Never accept client-supplied ownership or trust a frontend-only access check.

## Minimal entities

| Entity | Main fields | Relationships |
| --- | --- | --- |
| User | UserID, Name, Email (unique, lowercase), PasswordHash (BCrypt), CreatedAt | Owns notes, reviewers, quizzes, attempts |
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

Authentication routes above are implemented. The remaining routes are implementation contracts, not existing endpoints. No external AI API, cloud service, or messaging system is needed.

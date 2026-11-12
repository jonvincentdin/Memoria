# Architecture

## Proposed design

React JavaScript browser UI → Spring Boot REST API → Supabase PostgreSQL database (hosted).

Use a single Spring Boot application with controller, service, repository, and entity packages. React has pages/components and one small API helper; avoid extra architectural layers. The Supabase PostgreSQL database persists data between backend restarts and is shared across the team. Run one backend instance and use sample data only. A local PostgreSQL database is supported as a backup; see `SETUP.md` at the repo root. Account registration, login, logout, and user CRUD are implemented; note and quiz APIs remain planned.

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

## Authentication and user API

Registration (`POST /api/auth/register`) and login (`POST /api/auth/login`) verify user credentials and create a server-side session. Passwords are hashed with BCrypt and never returned. Logout invalidates the session; `GET /api/auth/me` returns the current user's public profile. State-changing requests require a CSRF token from `GET /api/auth/csrf`; the frontend API helper fetches and sends it automatically. Authenticated user CRUD operations are available under `/api/users`.

## Minimal entities

| Entity | Main fields | Relationships |
| --- | --- | --- |
| User | UserID, Name, Email (unique), PasswordHash, CreatedAt | Owns notes, reviewers, quizzes, attempts |
| Note | id, ownerId, title, body, updatedAt | User → many notes |
| Reviewer | ReviewerID, UserID, Title, Style, Content, CreatedAt | User → many reviewers |
| ReviewerNote | ReviewerID + NoteID (composite PK) | Reviewer ↔ Note, many-to-many |
| Quiz | id, ownerId, reviewerId, title | Reviewer → many quizzes |
| Question | id, quizId, text, choiceA–D, correctChoice | Quiz → many questions |
| QuizAttempt | AttemptID, UserID, QuizID, Score, TotalQuestions, Status, CompletedAt | Quiz → many attempts |

**ERD decisions (Increment 1):**

- **Reviewer ↔ Note** is many-to-many through the `ReviewerNote` join table (`reviewer_notes`). A reviewer needs at least one note, and every linked note must belong to the reviewer's owner. This replaces the older one-note-per-reviewer design.
- **Quiz attempts** store only the result summary in `QuizAttempt` (`quiz_attempts`). Status is `COMPLETED` for a graded attempt. The ERD has no `AttemptAnswer` table, so per-question answer review is deferred as stretch work. Do not add that table without a formal ERD update.
- A reviewer used by a quiz (`QuizReviewer`) cannot be deleted. The API returns 409 with a clear message.

Check that parent objects belong to the same user. Save a quiz and its questions transactionally. Use a transaction for grading and saving an attempt. Enforce the edit/delete restrictions in requirements to retain history without snapshots or complex versioning.

## Planned API surface

| Route | Purpose |
| --- | --- |
| GET /api/auth/csrf | Obtain a CSRF token |
| POST /api/auth/register | Register an account |
| POST /api/auth/login | Start a session |
| POST /api/auth/logout | Invalidate the session |
| GET /api/auth/me | Get the current user |
| GET /api/users | List users |
| POST /api/users | Create a user |
| GET /api/users/{id} | Read a user |
| PUT /api/users/{id} | Update a user |
| DELETE /api/users/{id} | Delete a user |
| GET/POST /api/notes | List/create notes |
| GET/PUT/DELETE /api/notes/{id} | Read/update/delete owned note |
| GET/POST /api/reviewers | List/create reviewers |
| GET/PUT/DELETE /api/reviewers/{id} | Read/update/delete owned reviewer |
| GET/POST /api/quizzes | List/create quizzes |
| GET/PUT/DELETE /api/quizzes/{id} | Authoring operations; owned content only |
| GET /api/quizzes/{id}/take | Questions without answer keys; issue submission token |
| POST /api/quizzes/{id}/attempts | Validate token, grade on server, save result |
| GET /api/attempts | Personal history, newest first (optional `?quizId=`) |
| GET /api/attempts/{id} | Saved result summary |
| DELETE /api/attempts/{id} | Delete owned attempt |

Authentication, user CRUD, reviewer (`/api/reviewers`) and attempt history (`/api/attempts`) routes above are implemented. Reviewer create and update take `{ title, style, content, noteIds: [...] }`; on update, `noteIds` is the full list, so notes left out are unlinked. Another user's reviewer or attempt returns 404. The remaining routes are implementation contracts, not existing endpoints. No external AI API, cloud service, or messaging system is needed.

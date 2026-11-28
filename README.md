# Memoria

Memoria is being rebuilt around the original learning-journal UI and workflows,
with an independent Java Spring Boot API and PostgreSQL database.

## Restart status

- `frontend/`: fresh React + TypeScript + Vite project. Old screens and dependencies
  are removed. A minimal entry and tested API client are ready for the rebuild.
- `backend/`: working APIs retained and reorganized by feature.
- Additional replacement-plan features are pending; this restart does not implement them.

## Structure

```text
frontend/src/
  app/               Application entry and root component
  features/          Feature screens and domain API adapters
  shared/api/        Session/CSRF-aware Spring API transport
  shared/ui/         Reusable components
  shared/styles/     Global styles
backend/src/main/java/com/csit321g1/lukydizy/memoria/
  auth/              Session authentication
  users/             Accounts and profiles
  notes/             Notes, imports and archive
  reviewers/         Reviewers and linked notes
  quizzes/           Quiz authoring and taking
  attempts/          Saved attempts and results
  flashcards/        Cards, reviews and scheduling
  shared/security/   Security configuration and current identity
  shared/error/      API errors and exception handling
```

Tests mirror backend feature packages. Historical Java guides are in `docs/reference/`.

## Run

Use Node.js 24 LTS, Java 17+, and a separate PostgreSQL database configured with
[SETUP.md](SETUP.md). Start the apps in separate terminals:

```powershell
# backend/
.\mvnw.cmd spring-boot:run

# frontend/
npm ci
npm run dev
```

Open <http://localhost:5173>. Vite proxies `/api` and `/actuator` to port 8080.
Health: <http://localhost:8080/actuator/health>.

## Verify

```powershell
# backend/
.\mvnw.cmd clean test

# frontend/
npm run lint
npm test
npm run build
```

Backend tests use in-memory H2 and do not modify PostgreSQL.
See [backend setup](backend/README.md), [frontend setup](frontend/README.md),
and [architecture](docs/architecture.md).

## References

[Task board](https://trello.com/b/SdkcppLc/memoria-lukydizy) |
[Original product](https://github.com/DandD-Official/Memoria) |
[Contribution guide](CONTRIBUTING.md)

Older course-project requirements/backlog/sprint documents are historical.
The replacement board defines current rebuild scope. No license has been assigned;
confirm ownership/licensing before reusing external source.
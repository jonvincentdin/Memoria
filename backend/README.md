# Memoria backend

One Java 17 Spring Boot application built with the Maven wrapper in this folder.
The canonical entry is `src/main/java/com/csit321g1/lukydizy/memoria/MemoriaApplication.java`.
Open the repository root (or `Memoria.code-workspace`), then open this canonical
entry and use Java's Run button or the `MemoriaApplication` launch configuration.
The launch configuration selects the Maven project, backend working directory,
and local PostgreSQL connection.

If VS Code was open before the directory flattening, its old folder workspace can
retain a duplicate `memoria` project pointing at `backend/memoria`. If Run reports
`SpringApplication cannot be resolved`, inspect the launch command: a classpath
containing only `jdt.ls-java-project/bin` means Java is treating the entry point as
a loose file instead of importing `backend/pom.xml`.

Run **Java: Clean Java Language Server Workspace**, allow the restart, and wait
for the Maven import/build to finish. Reopen the canonical entry above. The
`memoria` project should point to `backend`, with a Maven Dependencies container.
Keep one `MemoriaApplication` launch entry with `projectName: memoria`; remove any
duplicate generated entry that contains only `mainClass`. Do not recreate the old
nested application directory or add Spring jars manually to the project.

**Terminal > Run Task > Backend: run (Local PostgreSQL)** runs the Maven wrapper
without depending on the Java language server's project cache. Stop any running
backend before starting another instance on port 8080.

## Feature packages

Code lives under `com.csit321g1.lukydizy.memoria`. Each feature owns its controller,
DTOs, services, entities, and repositories. Tests mirror those boundaries.

| Package | Responsibility | API prefix |
| --- | --- | --- |
| `auth` | Registration, login, session and CSRF | `/api/auth` |
| `users` | Account operations | `/api/users` |
| `notes` | CRUD, import and archive/restore | `/api/notes` |
| `reviewers` | Reviewers and linked notes | `/api/reviewers` |
| `quizzes` | Authoring, linked reviewers and taking | `/api/quizzes` |
| `attempts` | Grading, saved attempts and results | `/api/attempts` |
| `flashcards` | Cards, reviews, scheduling and progress | `/api/flashcards` |
| `shared.security` | Sessions, BCrypt, CSRF and current user | Shared |
| `shared.error` | Error payloads and exception handling | Shared |

Keep the application class above every feature for Spring component/entity/repository
discovery. Add new domains as sibling packages, not global controller/service folders.

## Run and verify

Configure PostgreSQL using [SETUP.md](../SETUP.md).

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean test
.\mvnw.cmd package
java -jar target/memoria-0.0.1-SNAPSHOT.jar
```

Use `sh ./mvnw` on macOS/Linux. Health: <http://localhost:8080/actuator/health>.
The backend does not serve the frontend at `/`.
Tests activate `application-test.properties` and use in-memory H2.

## Configuration

| Variable | Default / purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/memoria` |
| `SPRING_DATASOURCE_USERNAME` | `memoria` locally |
| `SPRING_DATASOURCE_PASSWORD` | Local development password; override for your database |
| `SPRING_PROFILES_ACTIVE` | Local by default; `supabase` for explicitly configured hosted access |
| `SERVER_PORT` | `8080` |
| `SESSION_COOKIE_SECURE` | `false` locally; `true` by default for Supabase |

The CLI does not automatically load `.env.local`. Export variables or use VS Code's
launch entry with `envFile`. Never put credentials in the frontend.
Existing Hibernate schema-update behavior is retained: use an isolated development
database and design reviewed migrations before production.

Old implementation/test guides were moved to [docs/reference/](../docs/reference/);
their package paths describe the previous structure.

# Running the backend

Requires a JDK 17 or newer. Maven is downloaded by the checked-in wrapper; the first run needs internet access.

From `backend/memoria` in PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

On macOS/Linux use `sh ./mvnw` in place of `.\mvnw.cmd`.

Open http://localhost:8080/actuator/health. A healthy backend returns HTTP 200 with `{"status":"UP"}`. The root URL has no page yet and returns 404; application features remain planned.

## Database

The backend connects to **Supabase PostgreSQL** (hosted, team-shared) as the primary database. A local PostgreSQL instance is supported as a backup option.

**Follow [`SETUP.md`](../../SETUP.md) at the repo root for step-by-step setup instructions for both options.**

Tests always use H2 in-memory — no PostgreSQL installation is needed to run `mvnw test`.

## Environment variables

Set these before starting the backend:

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_PROFILES_ACTIVE` | Set to `supabase` for the hosted database |
| `SERVER_PORT` | Overrides the default port `8080` |

Copy `.env.example` (repo root) to `.env.local` as a reference. Never commit real passwords.

## To build and run a standalone JAR

```powershell
.\mvnw.cmd package
java -jar target/memoria-0.0.1-SNAPSHOT.jar
```

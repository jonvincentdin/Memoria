# Database Setup Guide

The backend supports two PostgreSQL databases:

| Option | When to use |
|--------|------------|
| **Local PostgreSQL** | Daily development on your own machine |
| **Supabase (hosted)** | Shared team database; always-on data |

Both use the same JDBC driver and Hibernate dialect. Switch between them with environment variables — no code changes needed.

---

## Prerequisites

- JDK 17 or newer
- Maven wrapper (`mvnw`) — already in the repo, downloads Maven on first run

---

## Option A — Local PostgreSQL

### 1. Install PostgreSQL

Download and install from https://www.postgresql.org/download/ (keep the default port **5432**).

### 2. Create the database and user using pgAdmin 4

Follow these steps starting from your pgAdmin 4 main window:

#### Step 2.1 — Connect to the PostgreSQL Server
1. In the left **Object Explorer** panel, look under **Servers (1)**.
2. Click or double-click **PostgreSQL 18** (or right-click it and select **Connect Server...**).
3. When prompted, enter the password you created during PostgreSQL installation (for the `postgres` user) and click **OK**.
   > *Note:* If you get a connection timeout error, ensure the Windows PostgreSQL service is running: press `Win + R`, type `services.msc`, locate `postgresql-x64-18` (or your version), right-click and choose **Start**.

#### Step 2.2 — Open the Query Tool
1. In the left tree, click on the server, expand **Databases**, and select the default database named **postgres**.
2. From the top menu bar, click **Tools** → **Query Tool** (or press `Alt + Shift + Q`).

#### Step 2.3 — Create the `memoria` database and role
1. In the Query Editor panel, paste the following SQL commands:
   ```sql
   CREATE DATABASE memoria;
   CREATE USER memoria WITH PASSWORD 'memoria';
   GRANT ALL PRIVILEGES ON DATABASE memoria TO memoria;
   ```
2. Click the **Execute / Play button** (`▶`) on the toolbar (or press **F5**). You should see `Query returned successfully`.

#### Step 2.4 — Grant public schema permissions
1. In the left **Object Explorer**, right-click **Databases** and select **Refresh**. You will now see `memoria` in the list.
2. Click on the **memoria** database to select it.
3. Open a new Query Tool window for it (**Tools** → **Query Tool**).
4. Paste and execute (**F5**):
   ```sql
   GRANT ALL ON SCHEMA public TO memoria;
   ```
5. You should see `Query returned successfully`. The local database is ready!

---

*(Alternative via CLI `psql`)*:
```sql
CREATE DATABASE memoria;
CREATE USER memoria WITH PASSWORD 'memoria';
GRANT ALL PRIVILEGES ON DATABASE memoria TO memoria;
\c memoria
GRANT ALL ON SCHEMA public TO memoria;
```

### 3. Set environment variables (PowerShell)

```powershell
$env:SPRING_DATASOURCE_URL      = 'jdbc:postgresql://localhost:5432/memoria'
$env:SPRING_DATASOURCE_USERNAME = 'memoria'
$env:SPRING_DATASOURCE_PASSWORD = 'choose-a-local-password'
```

> **Tip — macOS / Linux (bash/zsh)**
> ```bash
> export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/memoria
> export SPRING_DATASOURCE_USERNAME=memoria
> export SPRING_DATASOURCE_PASSWORD=choose-a-local-password
> ```

### 4. Run the backend

```powershell
cd backend/memoria
.\mvnw.cmd spring-boot:run
```

Hibernate will create or update the tables automatically (`ddl-auto=update`).

### 5. Verify

```
GET http://localhost:8080/actuator/health
→ {"status":"UP"}
```

---

## Option B — Supabase (hosted, team database)

### 1. Get the connection string

1. Go to your Supabase project → **Project Settings → Database**.
2. Under **Connection string**, choose **JDBC** and copy the URI.  
   It looks like:  
   `jdbc:postgresql://db.<ref>.supabase.co:5432/postgres`

### 2. Set environment variables (PowerShell)

```powershell
$env:SPRING_PROFILES_ACTIVE    = 'supabase'
$env:SPRING_DATASOURCE_URL     = 'jdbc:postgresql://db.<ref>.supabase.co:5432/postgres'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = 'your-supabase-db-password'
```

> **Tip — macOS / Linux (bash/zsh)**
> ```bash
> export SPRING_PROFILES_ACTIVE=supabase
> export SPRING_DATASOURCE_URL=jdbc:postgresql://db.<ref>.supabase.co:5432/postgres
> export SPRING_DATASOURCE_USERNAME=postgres
> export SPRING_DATASOURCE_PASSWORD=your-supabase-db-password
> ```

> **Security** — never paste real passwords into Trello, the repo, or chat. Share credentials privately (Signal, 1Password, etc.).

### 3. Run the backend

```powershell
cd backend/memoria
.\mvnw.cmd spring-boot:run
```

### 4. Verify

```
GET http://localhost:8080/actuator/health
→ {"status":"UP"}
```

---

## Running Tests

Tests always use an in-memory H2 database — no real PostgreSQL needed.

```powershell
cd backend/memoria
.\mvnw.cmd test
```

---

## Switching Between Databases

| Goal | Action |
|------|--------|
| Use local PostgreSQL | Set the three `SPRING_DATASOURCE_*` vars; leave `SPRING_PROFILES_ACTIVE` unset (or set it to blank) |
| Use Supabase | Set the three vars **plus** `SPRING_PROFILES_ACTIVE=supabase` |
| Return to defaults | Open a fresh terminal (env vars don't persist across sessions) |

---

## Keeping Secrets Out of Git

- `.env`, `.env.local`, and `data/` are in `.gitignore`.
- Copy `.env.example` (at the repo root) to `.env.local` and fill in your values — that file is never committed.
- Never add passwords to `application.properties`, source files, or any tracked file.

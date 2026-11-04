# Running the backend

Requires a JDK 17 or newer. Maven is downloaded by the checked-in wrapper; the first run needs internet access.

From `backend/memoria` in PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

On macOS/Linux use `sh ./mvnw` in place of `.\mvnw.cmd`.

Open http://localhost:8080/actuator/health. A healthy backend returns HTTP 200 with `{"status":"UP"}`. The root URL has no page yet and returns 404; application features remain planned.

The default development database is H2, persisted in the ignored `data/` directory. No MySQL installation is needed. Run one backend instance at a time. Tests use a separate in-memory database. Stop the server with Ctrl+C.

To build and run a standalone application:

```powershell
.\mvnw.cmd package
java -jar target/memoria-0.0.1-SNAPSHOT.jar
```

## Optional MySQL

Create a `memoria` database and a database user first. Set these environment variables in the same terminal before starting:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/memoria'
$env:SPRING_DATASOURCE_USERNAME = 'memoria'
$env:SPRING_DATASOURCE_PASSWORD = '<your local password>'
.\mvnw.cmd spring-boot:run
```

Use your actual connection details. Hibernate detects the dialect automatically. Keep passwords out of tracked files. To return to H2, open a fresh terminal or remove these overrides. Spring does not load `.env` files automatically.

`SERVER_PORT` overrides the default 8080 port. Schema updates (`ddl-auto=update`) are intended for this local prototype. Authentication and the planned study APIs are not implemented yet.

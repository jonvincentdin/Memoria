# Java package guide

| Folder or file | Purpose |
| --- | --- |
| `memoria/` | Base package for the Memoria backend. |
| `memoria/config/` | Security, session, CSRF, and CORS configuration. |
| `memoria/controller/` | HTTP endpoints that call services. |
| `memoria/dto/` | Request and response data shapes. |
| `memoria/entity/` | JPA models mapped to database tables. |
| `memoria/exception/` | Application-specific exceptions. |
| `memoria/exception/handler/` | Converts exceptions into JSON error responses. |
| `memoria/repository/` | Database queries and persistence operations. |
| `memoria/service/` | Authentication and business logic. |
| `memoria/MemoriaApplication.java` | Spring Boot application entry point. |

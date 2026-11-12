# Postman API Testing

This guide covers the backend routes currently implemented in Memoria. To test against Supabase, start the backend with the **Spring Boot - MemoriaApplication (Supabase)** VS Code launch configuration.

## Postman setup

Create a Postman environment with:

| Variable | Value |
|---|---|
| `baseUrl` | `http://localhost:8080` |

The API uses a server-side session cookie (`JSESSIONID`), not a bearer token. Keep requests on the same host and allow Postman to save and send cookies.

State-changing requests (`POST`, `PUT`, `DELETE`) require a CSRF token:

1. Send `GET {{baseUrl}}/api/auth/csrf`.
2. Save the response's `token` as a collection variable named `csrfToken`. One way is to add this under **Scripts → Post-response**:

   ```javascript
   pm.collectionVariables.set("csrfToken", pm.response.json().token);
   ```

3. Add the response's `headerName` as a header on every state-changing request, with value `{{csrfToken}}`. The default header name is `X-CSRF-TOKEN`.
4. Fetch a fresh CSRF token after registering or logging in; authentication changes the session token.

If Postman receives a successful login but subsequent requests return `401`, check whether it retained the `JSESSIONID` cookie. The Supabase profile marks the cookie `Secure` by default; for local HTTP testing only, set `SESSION_COOKIE_SECURE=false` in the ignored root `.env.local` and restart the backend.

## Authentication and health checks

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| Backend health | `GET {{baseUrl}}/actuator/health` | None | `200 OK`, status `UP` |
| Get CSRF token | `GET {{baseUrl}}/api/auth/csrf` | None | `200 OK`; JSON includes `token` and `headerName` |
| Register | `POST {{baseUrl}}/api/auth/register` | JSON below; CSRF header | `201 Created`; returns the new profile and signs in |
| Duplicate registration | `POST {{baseUrl}}/api/auth/register` | Repeat the same email; fresh CSRF header | `409 Conflict` |
| Current signed-in user | `GET {{baseUrl}}/api/auth/me` | Keep the session cookie | `200 OK`; returns the current user's profile |
| Login | `POST {{baseUrl}}/api/auth/login` | Email and password; CSRF header | `200 OK`; creates a session |
| Invalid login | `POST {{baseUrl}}/api/auth/login` | Wrong email or password; CSRF header | `401 Unauthorized` |
| Logout | `POST {{baseUrl}}/api/auth/logout` | CSRF header | `200 OK`; invalidates the session |
| Unauthenticated check | Clear Postman's cookies, then `GET {{baseUrl}}/api/auth/me` | None | `401 Unauthorized` |

Register body (use a unique email for each new registration):

```json
{
  "name": "Postman Tester",
  "email": "postman-unique@example.com",
  "password": "TestPassword123!"
}
```

Login body:

```json
{
  "email": "postman-unique@example.com",
  "password": "TestPassword123!"
}
```

## Authenticated user routes

All `/api/users` routes require a signed-in session. These are generic user CRUD routes, separate from normal registration.

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| List users | `GET {{baseUrl}}/api/users` | Session cookie | `200 OK` with a list |
| Create test user | `POST {{baseUrl}}/api/users` | Synthetic data and CSRF header | `201 Created` |
| Get user | `GET {{baseUrl}}/api/users/{id}` | Session cookie | `200 OK`, or `404` if absent |
| Update test user | `PUT {{baseUrl}}/api/users/{id}` | Synthetic data and CSRF header | `200 OK` |
| Delete test user | `DELETE {{baseUrl}}/api/users/{id}` | CSRF header | `200 OK`; removes the row |

**Caution:** `/api/users` create/update accept `passwordHash` directly and do not hash a password. Do not send a real password or use these routes to create normal accounts. Prefer `/api/auth/register`; use synthetic data for CRUD tests and delete only test rows.

## Reviewer routes

All reviewer routes require a signed-in session. Creating or updating a reviewer requires at least one existing note ID belonging to the signed-in user.

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| List reviewers | `GET {{baseUrl}}/api/reviewers` | Session cookie | `200 OK` |
| Filter by note | `GET {{baseUrl}}/api/reviewers?noteId={noteId}` | Session cookie | `200 OK` |
| Create reviewer | `POST {{baseUrl}}/api/reviewers` | Reviewer JSON below; CSRF header | `201 Created` |
| Get reviewer | `GET {{baseUrl}}/api/reviewers/{id}` | Session cookie | `200 OK`, or `404` if absent/not owned |
| Update reviewer | `PUT {{baseUrl}}/api/reviewers/{id}` | Reviewer JSON below; CSRF header | `200 OK`; submitted `noteIds` replaces the links |
| Delete reviewer | `DELETE {{baseUrl}}/api/reviewers/{id}` | CSRF header | `204 No Content`; may return `409` if a quiz uses it |

```json
{
  "title": "Postman reviewer",
  "style": "summary",
  "content": "Synthetic test content",
  "noteIds": ["<existing-note-id-owned-by-this-user>"]
}
```

## Quiz attempt history

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| List attempts | `GET {{baseUrl}}/api/attempts` | Session cookie | `200 OK` |
| Filter by quiz | `GET {{baseUrl}}/api/attempts?quizId={quizId}` | Session cookie | `200 OK` |
| Get attempt | `GET {{baseUrl}}/api/attempts/{id}` | Session cookie | `200 OK`, or `404` if absent/not owned |
| Delete attempt | `DELETE {{baseUrl}}/api/attempts/{id}` | CSRF header | `204 No Content` |

Attempts cannot currently be created directly through Postman: there is no attempt `POST` route. The attempt-creation flow depends on quiz submission, which is not yet exposed by the backend.

## Verify the registered user's row in Supabase

After a successful `POST /api/auth/register`, open **Supabase → Table Editor → `public.users`** and filter by the email used in the request. Or run this in the Supabase SQL Editor:

```sql
select user_id, name, email, created_at
from public.users
where lower(email) = lower('postman-unique@example.com');
```

The normal registration route stores a BCrypt password hash. Do not select or share the `password_hash` column.

## Routes not currently available

The backend has no notes or quiz controllers at present, so `/api/notes`, `/api/quizzes`, and quiz submission routes cannot yet be tested in Postman. Reviewer creation also depends on an existing note. See the project architecture documentation for the implementation status.

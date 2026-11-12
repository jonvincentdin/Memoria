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

## Note routes

Create a note first; a reviewer needs at least one note owned by the signed-in user.

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| List notes | `GET {{baseUrl}}/api/notes` | Session cookie | `200 OK`, newest first |
| Create note | `POST {{baseUrl}}/api/notes` | Note JSON below; CSRF header | `201 Created`; save the returned `id` as `noteId` |
| Get note | `GET {{baseUrl}}/api/notes/{{noteId}}` | Session cookie | `200 OK`, or `404` if absent/not owned |
| Update note | `PUT {{baseUrl}}/api/notes/{{noteId}}` | Note JSON; CSRF header | `200 OK` |
| Delete note | `DELETE {{baseUrl}}/api/notes/{{noteId}}` | CSRF header | `204 No Content`; may return `409` while a reviewer uses it |

```json
{
  "title": "Postman test note",
  "content": "Synthetic study material for API testing."
}
```

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

## Quiz creation and taking

Create a quiz with a reviewer ID owned by the current user. The authoring response and `GET /api/quizzes/{id}` include `correctIndex`; the student-facing `take` response must not.

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| List quizzes | `GET {{baseUrl}}/api/quizzes` | Session cookie | `200 OK`, current user's quizzes |
| Create quiz | `POST {{baseUrl}}/api/quizzes` | Quiz JSON below; CSRF header | `201 Created`; save returned `id` as `quizId` |
| Get authoring view | `GET {{baseUrl}}/api/quizzes/{{quizId}}` | Session cookie | `200 OK`; includes correct answers |
| Get taking view | `GET {{baseUrl}}/api/quizzes/{{quizId}}/take` | Session cookie | `200 OK`; includes title, question text, and four choices, but no `correctIndex` |

Use the following body for a known 7-of-10 grading check. All questions have choice index `0` as the correct answer:

```json
{
  "title": "Postman grading test",
  "mode": "PRACTICE",
  "questions": [
    { "text": "Question 1", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 2", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 3", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 4", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 5", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 6", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 7", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 8", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 9", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 },
    { "text": "Question 10", "choices": ["Correct", "Choice B", "Choice C", "Choice D"], "correctIndex": 0 }
  ],
  "reviewerIds": ["<reviewer-id>"]
}
```

Submit seven correct answers, one wrong answer, and leave the last two unanswered:

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| Submit answers | `POST {{baseUrl}}/api/quizzes/{{quizId}}/attempts` | JSON below; CSRF header | `201 Created`; `score: 7`, `totalQuestions: 10`, `percentage: 70`, `status: "COMPLETED"`, and `completedAt`; save returned `id` as `attemptId` |
| List attempt history | `GET {{baseUrl}}/api/attempts` | Session cookie | `200 OK`; current user's attempts, newest first |
| Filter history by quiz | `GET {{baseUrl}}/api/attempts?quizId={{quizId}}` | Session cookie | `200 OK`; only attempts for that quiz |
| Read saved result | `GET {{baseUrl}}/api/attempts/{{attemptId}}` | Session cookie | `200 OK`; includes quiz title, score, total, percentage, status, and completion time |
| Delete attempt | `DELETE {{baseUrl}}/api/attempts/{{attemptId}}` | CSRF header | `204 No Content` |

```json
{
  "answers": [
    { "questionIndex": 0, "choiceIndex": 0 },
    { "questionIndex": 1, "choiceIndex": 0 },
    { "questionIndex": 2, "choiceIndex": 0 },
    { "questionIndex": 3, "choiceIndex": 0 },
    { "questionIndex": 4, "choiceIndex": 0 },
    { "questionIndex": 5, "choiceIndex": 0 },
    { "questionIndex": 6, "choiceIndex": 0 },
    { "questionIndex": 7, "choiceIndex": 1 }
  ]
}
```

Answers use zero-based question and choice indexes. Leave an unanswered question out of `answers`; it counts as wrong. An empty list (`{"answers":[]}`) submits an all-unanswered attempt. Do not send a score: the backend grades the submission. Results are summary-only; the API does not provide per-question answer review.

## Ownership and invalid submissions

| Test | Method and endpoint | Request body / headers | Expected result |
|---|---|---|---|
| Unauthenticated quiz request | Clear cookies, then `GET {{baseUrl}}/api/quizzes/{{quizId}}/take` | None | `401 Unauthorized` |
| Other user's quiz | Sign in as a different user, then `GET {{baseUrl}}/api/quizzes/{{quizId}}/take` or `POST {{baseUrl}}/api/quizzes/{{quizId}}/attempts` | Session cookie; CSRF for `POST` | `404 Not Found` |
| Other user's attempt | As that different user, `GET {{baseUrl}}/api/attempts/{{attemptId}}` | Session cookie | `404 Not Found` |
| Unknown question | Submit an answer with `questionIndex` beyond the quiz's last question | Valid session and CSRF header | `400 Bad Request` with an error message; no attempt saved |
| Unknown choice | Submit an answer with `choiceIndex: 4` | Valid session and CSRF header | `400 Bad Request` with an error message; no attempt saved |
| Duplicate question selection | Include two entries for the same `questionIndex` | Valid session and CSRF header | `400 Bad Request` with an error message; no attempt saved |

## Verify the registered user's row in Supabase

After a successful `POST /api/auth/register`, open **Supabase → Table Editor → `public.users`** and filter by the email used in the request. Or run this in the Supabase SQL Editor:

```sql
select user_id, name, email, created_at
from public.users
where lower(email) = lower('postman-unique@example.com');
```

The normal registration route stores a BCrypt password hash. Do not select or share the `password_hash` column.

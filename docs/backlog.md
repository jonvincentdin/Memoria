# Feature backlog

All entries are **Backlog**. Assignments, estimates (total implementation plus review hours), and target dates are proposed. No GitHub issues, Trello cards, commits, or reviews are represented as already created.

Five epics and twelve features meet the laboratory minimum. SETUP-01 is a prerequisite task, not a thirteenth feature. QUIZ-02 can begin its UI after QUIZ-01; end-to-end submission also requires RESULT-01. Use mocked data until grading is integrated.

## AUTH-01 — Register account

- Epic: E1; priority: Must; estimate: 5 hours; target: 2026-11-13.
- Owner: @Kaylooww; reviewer: @jonvincentdin; dependencies: SETUP-01.
- User story: As a student, i can register so my study material is personal.
- Acceptance criteria: Valid registration succeeds; duplicate email and short password fail; only a BCrypt hash is stored.
- Task checklist: Build registration form; implement validation and persistence; verify valid and invalid cases.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## AUTH-02 — Log in

- Epic: E1; priority: Must; estimate: 5 hours; target: 2026-11-13.
- Owner: @jonvincentdin; reviewer: @Lterckski; dependencies: AUTH-01.
- User story: As a student, i can log in so I can access my records.
- Acceptance criteria: Valid credentials start a session; invalid credentials fail; protected API rejects anonymous requests.
- Task checklist: Build login form; configure sessions and CSRF; verify login and protected routes.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## AUTH-03 — Log out

- Epic: E1; priority: Must; estimate: 3 hours; target: 2026-11-20.
- Owner: @Lterckski; reviewer: @ZyrilR; dependencies: AUTH-02.
- User story: As a student, i can log out so this browser no longer accesses my account.
- Acceptance criteria: Logout invalidates the session; protected requests fail afterward; UI returns to login.
- Task checklist: Add logout action; invalidate session; verify access after logout.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## NOTE-01 — Create note

- Epic: E2; priority: Must; estimate: 5 hours; target: 2026-11-13.
- Owner: @ZyrilR; reviewer: @Kaylooww; dependencies: AUTH-02.
- User story: As a student, i can save a note so my study content stays together.
- Acceptance criteria: Valid title/body persist under current user; empty/oversize input fails; note remains after restart.
- Task checklist: Build note form; implement owned create endpoint; verify validation and persistence.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## NOTE-02 — List and read notes

- Epic: E2; priority: Must; estimate: 4 hours; target: 2026-11-20.
- Owner: @Lterckski; reviewer: @ZyrilR; dependencies: NOTE-01.
- User story: As a student, i can browse my notes so I can find material to study.
- Acceptance criteria: List and detail show only current user notes; empty state works; cross-user detail returns 404.
- Task checklist: Build note list/detail; add owner-filtered queries; verify using two accounts.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## NOTE-03 — Edit and delete notes

- Epic: E2; priority: Must; estimate: 4 hours; target: 2026-11-20.
- Owner: @Kaylooww; reviewer: @jonvincentdin; dependencies: NOTE-02.
- User story: As a student, i can maintain my notes so outdated content can be corrected.
- Acceptance criteria: Edits persist; confirmation precedes delete; delete is blocked with linked reviewers; cross-user writes fail.
- Task checklist: Add edit/delete UI and API; enforce dependency rule; verify both owners.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## REV-01 — Manage reviewer

- Epic: E3; priority: Must; estimate: 5 hours; target: 2026-11-20.
- Owner: @jonvincentdin; reviewer: @Lterckski; dependencies: NOTE-02.
- User story: As a student, i can write or paste a reviewer so I have a concise study guide.
- Acceptance criteria: Create/edit/delete owned reviewer works; source note is required; deletion with quizzes is blocked.
- Task checklist: Build reviewer form; implement CRUD and note link; verify ownership and delete restriction.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## REV-02 — Browse and read reviewers

- Epic: E3; priority: Must; estimate: 3 hours; target: 2026-11-27.
- Owner: @ZyrilR; reviewer: @Kaylooww; dependencies: REV-01.
- User story: As a student, i can open my reviewers so I can review a topic.
- Acceptance criteria: Owned reviewer list and detail render plain text; source note is linked; other users cannot read it.
- Task checklist: Build list/detail; link source note; verify empty state and ownership.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## QUIZ-01 — Manage multiple-choice quiz

- Epic: E4; priority: Must; estimate: 7 hours; target: 2026-11-27.
- Owner: @Lterckski; reviewer: @ZyrilR; dependencies: REV-01.
- User story: As a student, i can prepare practice questions so I can test my understanding.
- Acceptance criteria: Accept 1–20 questions with four choices and one key; reject invalid questions; lock questions after first attempt; block deletion with attempts.
- Task checklist: Build quiz form; implement transactional save and validation; verify edit/delete restrictions.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## QUIZ-02 — Take quiz

- Epic: E4; priority: Must; estimate: 5 hours; target: 2026-11-27.
- Owner: @Kaylooww; reviewer: @jonvincentdin; dependencies: QUIZ-01.
- User story: As a student, i can answer a quiz so I can practice recalling the topic.
- Acceptance criteria: Take endpoint omits keys; selection and unanswered items work; submit once with token; navigate to saved result.
- Task checklist: Build taking screen; obtain token; submit selected answers; verify duplicate submission handling.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## RESULT-01 — Grade and display result

- Epic: E5; priority: Must; estimate: 6 hours; target: 2026-12-04.
- Owner: @jonvincentdin; reviewer: @Lterckski; dependencies: QUIZ-01.
- User story: As a student, i can see my score and correct answers so I know what to review.
- Acceptance criteria: Server grades correctly; unanswered items are wrong; one transaction saves score and answers; result shows correct choices after submission only.
- Task checklist: Implement grading and token uniqueness; save attempt/answers; build result view; check known scores.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.


## RESULT-02 — View attempt history

- Epic: E5; priority: Must; estimate: 4 hours; target: 2026-12-04.
- Owner: @ZyrilR; reviewer: @Kaylooww; dependencies: RESULT-01.
- User story: As a student, i can revisit attempts so I can compare practice results.
- Acceptance criteria: Newest attempts show quiz/date/score; results reopen after login; cross-user attempts cannot be read.
- Task checklist: Build history and result links; query owned attempts; verify persistence and two-account isolation.
- Status: Backlog. Trello card URL: pending creation. GitHub issue URL: pending creation.

# Proposed one-week sprint

**November 9–13, 2026 (Monday–Friday)**. These are suggested planning dates, not a supplied instructor deadline. Shift them together if your class uses a different week.

Goal: initialize both applications, demonstrate account registration/login, and save one personal note through the frontend and backend. Use local sample accounts and H2. Do not attempt the full twelve-feature MVP in one week.

| Task | Owner | Reviewer | Hours incl. review | Due | Dependencies | Done check |
| --- | --- | --- | --- | --- | --- | --- |
| SETUP-01: generate scaffolds, configure proxy/session groundwork, verify local startup | Lterckski | ZyrilR | 5 | Nov 10 | Repository created | Both apps start; proxy reaches backend; setup evidence recorded |
| AUTH-01: registration | Kaylooww | jonvincentdin | 5 | Nov 11 | SETUP-01 | Valid/invalid registration verified; password hashed |
| AUTH-02: login/session/CSRF | jonvincentdin | Lterckski | 5 | Nov 12 | AUTH-01 | Login succeeds; invalid login and anonymous protected request fail |
| NOTE-01: create note | ZyrilR | Kaylooww | 5 | Nov 13 | AUTH-02 | Note saved for current user and persists across backend restart |

Each member has about five hours of estimated work including peer review; this is a starting estimate, not a recorded contribution. Forms can be developed with mock responses while dependencies are unfinished. Reserve review time daily rather than merging everything on Friday.

## Daily checkpoints

- Monday: create board/repository; link setup task; generate scaffolds; build registration/login/note form shells in parallel.
- Tuesday: finish scaffold and local configuration; verify setup; complete registration endpoint.
- Wednesday: review registration; implement login/session/CSRF; continue note API behind authentication.
- Thursday: review login; integrate note creation; run two-account access checks.
- Friday: verify persistence and validation; resolve findings; demonstrate two linked issues and at least one peer-reviewed PR.

## Blockers and handling

| Potential blocker | Owner | Response |
| --- | --- | --- |
| Node/Java/Maven mismatch | Lterckski | Record selected compatible versions after scaffold generation |
| Authentication/CSRF delays | jonvincentdin | Pair with reviewer; defer optional visual polish |
| Repository or Trello access missing | Kaylooww | Request membership from repository/board owner |
| Integration takes longer than estimate | ZyrilR | Report early; move unfinished card to next sprint with revised target |

Next proposed targets: remaining account/note/reviewer editing by Nov 20; reviewer browsing and quiz work by Nov 27; results/history and integrated acceptance verification by Dec 4. Optional polish only follows acceptance of Must features.

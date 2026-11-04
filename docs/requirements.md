# Requirements

## Proposal summary

Memoria helps students organize personal study notes, prepare reviewers, and practice multiple-choice quizzes with saved scores. The subject version replaces the inspiration project's Next.js/TypeScript stack with React JavaScript and Spring Boot Java. It deliberately limits the product to a local demonstration with manual content entry.

## Scope and modules

| Epic | Module | Included features |
| --- | --- | --- |
| E1 | Accounts | AUTH-01, AUTH-02, AUTH-03 |
| E2 | Notes | NOTE-01, NOTE-02, NOTE-03 |
| E3 | Reviewers | REV-01, REV-02 |
| E4 | Quizzes | QUIZ-01, QUIZ-02 |
| E5 | Results | RESULT-01, RESULT-02 |

### Must have

Students register with a unique email, sign in, and sign out. Each student can manage personal plain-text notes and reviewers linked to a note. Students can manage multiple-choice quizzes and submit answers for server-side grading. The application saves each attempt and displays a score, correct answers after submission, and personal attempt history. Backend ownership checks prevent users accessing another student's records.

### Should have

Lists should include an empty state and order recent records first. Forms should show readable validation messages and request confirmation before deleting data. Screens should remain usable on a phone-sized browser. These polish tasks must fit inside existing features rather than expanding the MVP.

### Could have

A client-side keyword filter may be added after all Must features pass verification. Copying reviewer content to the clipboard is optional. Neither is a dependency of the assessed MVP.

### Won't have in this subject version

AI generation or provider keys, multi-format import/export, OCR, OAuth connections, sharing or collections, a separate admin role, flashcards/spaced repetition, timers, advanced analytics, guest mode, rich editors, and production hosting.

## Simple behavior rules

- Email is unique and normalized; passwords use BCrypt and are never returned by an API. Use a minimum of eight characters for this prototype.
- Notes and reviewers have a non-empty title (maximum 120 characters) and body (maximum 20,000 characters). Render as plain text; do not interpret HTML.
- A reviewer belongs to one note. A quiz belongs to one reviewer. Store all content under the current user.
- A quiz contains 1–20 questions, each with four non-empty choices and exactly one correct choice. Question text is limited to 1,000 characters; each choice to 300 characters.
- The question structure of a quiz becomes read-only after its first attempt. A title may still be edited. This preserves the meaning of saved results without versioning.
- Score = correct answers / total questions × 100. Unanswered questions count as incorrect. Do not send correct answers in the quiz-taking response.
- Reject malformed answers and repeat submission of the same attempt token. Persist answers and score together; show correct choices only after submission.
- Block deletion of a note with reviewers, a reviewer with quizzes, or a quiz with attempts. Show a clear explanation. Do not silently delete study history.
- Anonymous requests to protected endpoints return 401; records owned by another user return 404. Validate all input on the server.

## Acceptance and feasibility

The twelve features, owners, estimates, dependencies, and acceptance criteria are defined in the backlog. A proposed four-week development window is November 9–December 4, 2026, weekdays only. Dates and estimates are planning assumptions, not instructor deadlines or completed work. Re-estimate after the first sprint; reduce optional polish before adding integrations.

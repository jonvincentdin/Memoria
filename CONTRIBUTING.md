# Contributing to memoria

All four members contribute as Software Developers. Each implementation task has a different peer reviewer.

1. Select a Trello card with its ID, owner, reviewer, deadline, estimate, acceptance criteria, and dependencies.
2. Create or link a GitHub issue. Paste the issue URL into the Trello card, and paste the card URL into the issue.
3. Update local `main`, then create `feature/<issue>-<short-name>` or `fix/<issue>-<short-name>`. Replace `<issue>` with the actual GitHub issue number.
4. Make one focused change. Use descriptive commits such as `feat(notes): add note creation` or `fix(quiz): prevent duplicate submissions`.
5. Verify acceptance criteria and update documentation when behavior changes. Run relevant generated frontend/backend checks once available.
6. Open a pull request against `main`, use `Closes #<actual-issue-number>`, link the Trello card, and attach test output or screenshots with sample data.
7. Request at least one other member's review. Address findings before merging. Use repository branch protection where available; otherwise enforce this rule manually.
8. Move the card to Done only after peer review and verification evidence are recorded. Never claim that a planned check has passed.

## Board workflow

Backlog → To Do → In Progress → For Review → Testing → Done.

- To Do: scope and dependencies understood.
- In Progress: owner and acceptance criteria required; limit one active card per member.
- For Review: PR ready and reviewer assigned.
- Testing: reviewer has checked the change; verify acceptance criteria.
- Done: approved/merged PR and verification evidence linked. Reopen if verification fails.

Never commit credentials, real student data, local databases, build output, or dependencies. Keep API authorization on the backend. All resource access must check the authenticated owner.

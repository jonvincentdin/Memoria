# Repository and task board setup

## GitHub initialization

This ZIP supplies files, not a hosted repository or Git history. Create a **private** GitHub repository named `memoria`, without an automatically generated README, license, or gitignore. Invite `Kaylooww`, `jonvincentdin`, `Lterckski`, and `ZyrilR` as collaborators; add the instructor where applicable after obtaining the correct account. The inspiration repository is not assumed to be this new repository.

Extract the ZIP and open a terminal inside the `memoria` folder:

```bash
git init -b main
git add .
git commit -m "docs: initialize memoria project setup"
```

Copy your new repository's remote URL from GitHub, then run:

```bash
git remote add origin <YOUR_NEW_REPOSITORY_URL>
git push -u origin main
```

Replace the angle-bracket placeholder; do not run it literally. Record the actual repository URL in `docs/submission.md`. Keep the repository private as instructed.

Protect `main` where your GitHub plan supports it: require a pull request and at least one peer approval. Otherwise apply the same policy manually. Invite members through GitHub; this package does not claim that invitations or protection already exist.

## Trello

Use the board associated with the invitation supplied in chat: [memoria board](https://trello.com/b/6ac6eddf43b1215e8ab78dc7/memoria-lukydizy). The token-free link should identify the board but has not been verified; use the original invite privately if access is needed.

Create these six lists: **Backlog, To Do, In Progress, For Review, Testing, Done**. Invite all four members through the board interface. Do not store the invitation token in a repository.

Use `docs/backlog.csv` as copy-ready card data; it is not an automatic Trello import or a completed board. For each feature create one card with ID/title, story, owner, reviewer, MoSCoW priority, estimate, deadline, dependencies, checklist, acceptance criteria, and actual GitHub issue URL. A bundled small checklist is sufficient; only create extra task cards when useful.

Create SETUP-01 separately with the sprint-plan fields. Add reviewer, priority Must, deadline Nov 10, estimate five hours, checklist for generation/configuration/startup verification, and its real GitHub issue URL.

## Required live work

1. Create two GitHub issues from `.github/issue-drafts/AUTH-01.md` and `NOTE-01.md`.
2. Link each actual issue to its actual Trello card in both directions.
3. Use a branch named with an actual GitHub issue number. Make a small real scaffold or documentation change and open a linked PR.
4. Ask another member to review it; record actual findings and verification evidence.
5. Update card status only when the required work has occurred.

The included templates help the team do these steps. Files alone do not satisfy the live collaboration demonstration.

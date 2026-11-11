# memoria

Turn scattered notes into structured knowledge.

Memoria is a small student study web application for **CSIT340 - Industry Elective**, under **Sir Eugene C. Bussico**. Students keep personal notes, organize reviewers, take multiple-choice quizzes, and revisit saved results in one place.

## Problem and target users

Students often store notes in separate files and applications, manually prepare reviewers, and lose track of previous quiz scores. Memoria serves students who want a simple place to organize study material and practice what they have learned.

## Project status

This package initializes documentation, collaboration templates, and source/test folders. Application features are **planned, not implemented**. It is a simplified subject-project adaptation inspired by [DandD-Official/Memoria](https://github.com/DandD-Official/Memoria), not a copy of its existing application. Do not overlay these files on that repository without reviewing the differences.

## Planned features

| Module | Features |
| --- | --- |
| Accounts | Register, log in, log out |
| Notes | Create, list/read, edit/delete personal notes |
| Reviewers | Create/edit/delete a reviewer linked to a note; browse/read reviewers |
| Quizzes | Create/edit/delete multiple-choice quizzes; take a quiz |
| Results | View a graded result; view attempt history |

The MVP contains these **5 modules and 12 backlog features**. Reviewers and questions are entered manually; students may paste text prepared elsewhere. Quizzes have four choices and one correct answer. No AI subscription is required.

Excluded: direct AI API integration, Google Drive/Notion connections, document parsing/OCR, public sharing, admin panels, flashcards, spaced repetition, timers, mobile apps, and production deployment. See [requirements](docs/requirements.md) and the [feature backlog](docs/backlog.md).

## Technology stack

- Frontend: React with JavaScript, Vite, basic CSS, browser fetch.
- Backend: Java with Spring Boot, Spring Web, Spring Security, and Spring Data JPA.
- Database: Supabase PostgreSQL (hosted, team-shared). Local PostgreSQL is supported as a backup. H2 is used only for running tests.
- Authentication: Spring Security sessions and BCrypt password hashing.
- Collaboration: GitHub and Trello.

Use one frontend and one backend application; no microservices or extra infrastructure.

## Team

| Name | GitHub username | Role |
| --- | --- | --- |
| Kyle Angelo C. Castro | @Kaylooww | Software Developer |
| Jon Vincent F. Din | @jonvincentdin | Software Developer |
| Luther Derrick E. Dira | @Lterckski | Software Developer |
| Zyril Ryle R. Restauro | @ZyrilR | Software Developer |

Everyone is a Software Developer. Feature ownership and peer review assignments are recorded in [team roles](docs/team-roles.md).

## Local setup

The React/Vite scaffold is in `frontend/memoria`; the Spring Boot scaffold is in `backend/memoria`. Features remain planned.

1. Install Node.js LTS and a JDK 17 or newer. Install PostgreSQL locally or obtain Supabase credentials from a teammate.
2. Follow [SETUP.md](SETUP.md) to configure your database (local PostgreSQL or Supabase) and set environment variables.
3. In `backend/memoria`, run `.\mvnw.cmd spring-boot:run` (PowerShell) or `sh ./mvnw spring-boot:run` (macOS/Linux). The Maven wrapper downloads Maven automatically.
4. Check http://localhost:8080/actuator/health for `{"status":"UP"}`.
5. In a second terminal, run `npm install` and `npm run dev` from `frontend/memoria`. Open the address printed by Vite.

See the [backend run guide](backend/memoria/README.md) for tests and packaging.

## Collaboration

[Task board](https://trello.com/b/6ac6eddf43b1215e8ab78dc7/memoria-lukydizy) · [Contribution guide](CONTRIBUTING.md) · [Sprint plan](docs/sprint-plan.md)

The board URL uses the board ID from the supplied invite without its invitation token; verify it after signing in. Board visibility and membership have not been checked. The new subject-project GitHub URL must be recorded in [submission](docs/submission.md) after repository creation.

Keep `main` stable. Use `feature/<issue>-<short-name>` or `fix/<issue>-<short-name>`, link a GitHub issue and Trello card, and request at least one peer review. Never commit real secrets.

## Documentation

- [Database setup](SETUP.md)
- [Requirements and scope](docs/requirements.md)
- [Architecture and data model](docs/architecture.md)
- [Team assignments](docs/team-roles.md)
- [Backlog](docs/backlog.md) and [CSV card data](docs/backlog.csv)
- [One-week sprint plan](docs/sprint-plan.md)
- [GitHub and board setup](docs/repository-setup.md)
- [Traceability exercise](docs/traceability.md)
- [Submission and contribution evidence](docs/submission.md)
- [Manual verification checklist](tests/README.md)

No license has been assigned. Decide ownership and licensing together before publishing code or reusing source code from another project.

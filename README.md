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
- Database: H2 file database for a simple local classroom demonstration (proposed default).
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

**The application scaffolds have not been generated yet.** The ZIP intentionally contains no `package.json`, `pom.xml`, or runnable application. Complete these initial steps as the first team task:

1. Install a supported Node.js LTS release, a Java LTS release compatible with the Spring Boot version selected in Spring Initializr, Maven, and Git.
2. Extract this folder and follow [repository setup](docs/repository-setup.md) to initialize a private `memoria` repository.
3. From the repository root, create the React JavaScript scaffold:

   ```bash
   npm create vite@latest src/frontend -- --template react
   cd src/frontend
   npm install
   ```

   `src/frontend` currently contains `.env.example`. If Vite prompts because the folder is not empty, choose **Ignore files and continue** to preserve it. Commit the generated lockfile.
4. At [Spring Initializr](https://start.spring.io/), generate a Maven/Java project with group `com.memoria`, artifact `backend`, and dependencies Spring Web, Spring Security, Spring Data JPA, Validation, and H2 Database. Extract the generated project's contents into `src/backend` so `src/backend/pom.xml` exists directly.
5. Copy the root `.env.example` to a local `.env` for reference. Copy `src/frontend/.env.example` to `src/frontend/.env.local`. Configure Spring as described in [architecture](docs/architecture.md). Spring Boot does not automatically load a root `.env` file.
6. Once configuration and a minimal application exist, run the backend in `src/backend` with `mvn spring-boot:run` and the frontend in `src/frontend` with `npm run dev` in separate terminals.
7. Open the frontend address printed by Vite (normally `http://localhost:5173`). The backend uses `http://localhost:8080`. Implement and verify features through reviewed pull requests.

## Collaboration

[Task board](https://trello.com/b/6ac6eddf43b1215e8ab78dc7/memoria-lukydizy) · [Contribution guide](CONTRIBUTING.md) · [Sprint plan](docs/sprint-plan.md)

The board URL uses the board ID from the supplied invite without its invitation token; verify it after signing in. Board visibility and membership have not been checked. The new subject-project GitHub URL must be recorded in [submission](docs/submission.md) after repository creation.

Keep `main` stable. Use `feature/<issue>-<short-name>` or `fix/<issue>-<short-name>`, link a GitHub issue and Trello card, and request at least one peer review. Never commit real secrets.

## Documentation

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

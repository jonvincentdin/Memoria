# Feature implementation

Build the listed features with new TypeScript implementation, following RESOURCES.md and the established green-and-paper identity. Do not restore the previous frontend source.
Each feature is verified against the retained Spring APIs, committed and pushed separately.

| Order | Feature | Status |
| --- | --- | --- |
| 1 | Accounts: signup, login, logout, sessions and route protection | Verified |
| 2 | Dashboard and account/navigation overview | Verified |
| 3 | Notes CRUD, archive and restore | Verified |
| 4 | MD/TXT/PDF/DOCX/PPTX imports | Verified |
| 5 | Reviewers, styles and linked notes | Pending |
| 6 | Practice quiz authoring | Pending |
| 7 | Quiz taking, grading and saved results | Pending |
| 8 | Flashcard CRUD and study sessions | Pending |
| 9 | Spaced repetition and due queues | Pending |
| 10 | Flashcard progress and review history | Pending |

Existing user CRUD and attempt-list/delete APIs remain backend capabilities. The
standalone Results/history navigation page is outside the previously implemented
screens; individual quiz results are restored in feature 7.

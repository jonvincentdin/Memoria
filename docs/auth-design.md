# Authentication design

Approved direction: green and paper. Audience: students returning to personal study material or creating their first account. Primary action: log in or create an account.

The study material becomes the illustration: one biology topic is shown as a note, reviewer, and quiz question. This is labeled as a study preview, not a screenshot of implemented functionality. A pale sage editorial panel occupies approximately half the desktop screen, and the form sits directly on the warm-white surface. On mobile, the illustration is removed and the brand remains above the form.

Colors: paper #fafbf8, sage #e9efe6, forest #245747, ink #202a25, muted #5e6c63, border #a8b3a9, error #a33131. Serif headings and sans-serif controls. Generous spacing, 48px minimum form inputs and submit buttons, 44px password visibility targets, visible keyboard focus, reduced-motion support. No social login or password recovery controls until those capabilities exist.

Login: email and password. Signup: name, email, password, confirmation. Both support password visibility and inline errors. Signup enforces the documented eight-character minimum; login allows existing passwords without imposing a new policy. Valid submissions show an honest connection-pending status and never send or save credentials. Navigation uses hash routes so links and browser back work without server rewrite configuration.

User approval: the green direction and split-layout proposal in the conversation. Build directly from this approved proposal; no replacement visual direction or generated imagery is needed.

## Verification

- Frontend ESLint and Vite production build passed.
- Browser checks passed for required fields, email format, signup password length and confirmation, first-error focus, password visibility, truthful submission feedback, route switching, browser back, preview quiz interaction, and the skip link preserving signup route/focus/entered data.
- Both screens have no horizontal overflow at 320, 390, 768, and 1440px. Desktop, tablet, mobile, and signup validation screenshots were reviewed.
- The mechanical detector reported no findings. The final responsive inspector had no failures; its warnings concern compact preview text and the intentionally larger serif authentication heading. These retain the user-approved editorial direction.
- Independent final review scored the navigation and contract corrections resolved and returned ship.

Local review captures and browser-check output live in the ignored `.taste-skill/` directory. These checks establish frontend behavior only. Real authentication, registration, server validation, sessions, and a post-login destination remain unimplemented.

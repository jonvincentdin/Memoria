# Authentication design

Approved direction: green and paper. Audience: students returning to personal study material or creating their first account. Primary action: log in or create an account.

The study material becomes the illustration: one biology topic is shown as a note, reviewer, and quiz question. It is labeled as a study preview, not as saved account content. A pale sage editorial panel occupies approximately half the desktop screen, and the form sits directly on the warm-white surface. On mobile, the illustration is removed and the brand remains above the form.

Colors: paper #fafbf8, sage #e9efe6, forest #245747, ink #202a25, muted #5e6c63, border #a8b3a9, error #a33131. Serif headings and sans-serif controls. Generous spacing, 48px minimum form inputs and submit buttons, 44px password visibility targets, visible keyboard focus, reduced-motion support. No social login or password recovery controls until those capabilities exist.

Login: email and password. Signup: name, email, password, confirmation. Both support password visibility and inline errors. Signup enforces the documented eight-character minimum; login allows existing passwords without imposing a new policy. Both use the backend's session API with CSRF tokens, and successful registration or login opens the authenticated dashboard. Logout invalidates the server session. Password recovery and social login remain absent because they are not implemented. Navigation uses hash routes so links and browser back work without server rewrite configuration.

User approval: the green direction and split-layout proposal in the conversation. Build directly from this approved proposal; no replacement visual direction or generated imagery is needed.

## Verification

- Backend authentication tests cover validation, lowercase unique emails, BCrypt storage, safe profile responses, login, CSRF enforcement, protected routes, and logout invalidation.
- Frontend ESLint and Vite production build passed. The login/signup UI uses the API with credentials and displays server validation/authentication errors.
- The dashboard's study metrics and study sets remain sample content; only its user identity and session are live.

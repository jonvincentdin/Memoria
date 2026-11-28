# Memoria frontend

Fresh React + TypeScript + Vite project. The old screens are removed; feature
screens will be rebuilt incrementally.

## Development

Use Node.js 24 LTS (tests use Node's native TypeScript support).

```powershell
npm ci
npm run dev
```

Open <http://localhost:5173>. Start Spring Boot separately on port 8080.
Development and preview servers proxy `/api` and `/actuator` to the backend.
Ports are strict so a second instance cannot silently change the origin.

For another backend address, copy `.env.example` to `.env.local`, set
`BACKEND_URL`, and restart Vite. This value belongs to the server proxy and
is not exposed as a browser-side `VITE_*` variable.

## Structure

- `src/app/`: entry and application composition.
- `src/features/`: domain screens and API adapters; see its README.
- `src/shared/api/`: transport matching Spring's session and CSRF contract.
- `src/shared/ui/`: shared components added as needed.
- `src/shared/styles/`: global styles.
- `tests/`: API client behavior tests using Node's built-in runner.

Spring Boot owns data, permissions, integrations and secrets. Do not add
a separate Node backend or browser database SDK.

## API calls

```ts
import { apiRequest } from './shared/api/client'

const notes = await apiRequest<Note[]>('/api/notes')
await apiRequest('/api/notes', {
  method: 'POST',
  body: JSON.stringify({ title: 'Biology', content: 'Cell structure' }),
})
```

`Note` is the response type defined by the feature implementing that API.
The client sends cookies, gets a fresh CSRF token for every write, preserves
multipart boundaries, rejects external URLs, and throws `ApiError` with the
backend status/message. Callers own loading/error state and may pass an
`AbortSignal`.

## Verify

```powershell
npm run lint
npm test
npm run build
npm run preview
```

Assets go into `dist/`. Preview is for local verification. A production host
must route `/api` and `/actuator` to Spring Boot and configure HTTPS/cookies.
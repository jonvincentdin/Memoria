/** Spring Boot returns validation/auth failures as { error: string }. */
export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

type CsrfToken = { headerName: string; token: string }

async function readResponse(response: Response): Promise<unknown> {
  if (response.status === 204) return undefined
  const text = await response.text()
  if (!text) return undefined
  return response.headers.get('content-type')?.includes('application/json')
    ? JSON.parse(text)
    : text
}

async function checkedResponse(response: Response): Promise<unknown> {
  const body = await readResponse(response)
  if (!response.ok) {
    const message = typeof body === 'object' && body !== null && 'error' in body
      && typeof body.error === 'string'
      ? body.error
      : `Request failed (${response.status}).`
    throw new ApiError(message, response.status)
  }
  return body
}

/** Use same-origin /api paths so the Vite proxy carries session cookies. */
export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  if (!path.startsWith('/api/') || path.startsWith('//')) {
    throw new Error('API requests must use a same-origin /api/ path.')
  }
  const method = (options.method || 'GET').toUpperCase()
  const headers = new Headers(options.headers)
  headers.set('Accept', 'application/json')

  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    // Fetch per write: login/logout rotate both the session and CSRF token.
    const csrf = await checkedResponse(await fetch('/api/auth/csrf', {
      credentials: 'include', signal: options.signal,
    })) as CsrfToken
    if (!csrf?.headerName || !csrf.token) throw new Error('Missing CSRF token.')
    headers.set(csrf.headerName, csrf.token)
  }

  // Keep the browser-generated multipart boundary for FormData uploads.
  if (typeof options.body === 'string' && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  return await checkedResponse(await fetch(path, {
    ...options, method, headers, credentials: 'include',
  })) as T
}

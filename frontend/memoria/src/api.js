const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

export async function apiRequest(path, { method = 'GET', body } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const csrfResponse = await fetch(`${API_BASE}/api/auth/csrf`, {
      credentials: 'include',
      cache: 'no-store',
    })
    const csrf = await csrfResponse.json()
    if (!csrfResponse.ok) throw new ApiError(csrf.error || 'Could not prepare a secure request.', csrfResponse.status)
    headers[csrf.headerName] = csrf.token
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    credentials: 'include',
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  const payload = response.status === 204 ? null : await response.json()
  if (!response.ok) throw new ApiError(payload?.error || 'The request could not be completed.', response.status)
  return payload
}

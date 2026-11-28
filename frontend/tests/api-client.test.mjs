import { afterEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { apiRequest, ApiError } from '../src/shared/api/client.ts'

const originalFetch = globalThis.fetch
afterEach(() => { globalThis.fetch = originalFetch })
const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status, headers: { 'Content-Type': 'application/json' },
})

test('reads use the same-origin API and include session cookies', async () => {
  globalThis.fetch = async (path, options) => {
    assert.equal(path, '/api/notes')
    assert.equal(options.credentials, 'include')
    return json([{ id: 'note-1' }])
  }
  assert.deepEqual(await apiRequest('/api/notes'), [{ id: 'note-1' }])
})

test('writes retrieve a fresh CSRF token after authentication rotates the session', async () => {
  let token = 0
  const headers = []
  globalThis.fetch = async (path, options) => {
    assert.equal(options.credentials, 'include')
    if (path === '/api/auth/csrf') return json({ headerName: 'X-CSRF-TOKEN', token: `token-${++token}` })
    headers.push(options.headers.get('X-CSRF-TOKEN'))
    assert.equal(options.headers.get('Content-Type'), 'application/json')
    return json({ id: 'user-1' })
  }
  await apiRequest('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: 'learner@example.test' }) })
  await apiRequest('/api/notes', { method: 'POST', body: JSON.stringify({ title: 'Biology' }) })
  assert.deepEqual(headers, ['token-1', 'token-2'])
})

test('multipart uploads retain the browser boundary and support empty responses', async () => {
  const form = new FormData()
  form.set('file', new Blob(['notes']), 'notes.txt')
  globalThis.fetch = async (path, options) => {
    if (path === '/api/auth/csrf') return json({ headerName: 'X-CSRF-TOKEN', token: 'token' })
    assert.equal(options.body, form)
    assert.equal(options.headers.has('Content-Type'), false)
    assert.equal(options.headers.get('X-CSRF-TOKEN'), 'token')
    return new Response(null, { status: 204 })
  }
  assert.equal(await apiRequest('/api/notes/import', { method: 'POST', body: form }), undefined)
})

test('Spring authorization errors retain their HTTP status and message', async () => {
  globalThis.fetch = async () => json({ error: 'Unauthorized' }, 401)
  await assert.rejects(apiRequest('/api/auth/me'), (error) =>
    error instanceof ApiError && error.status === 401 && error.message === 'Unauthorized')
})

test('CSRF failure prevents the write', async () => {
  let requests = 0
  globalThis.fetch = async () => { requests++; return json({ error: 'Forbidden' }, 403) }
  await assert.rejects(apiRequest('/api/notes', { method: 'DELETE' }), ApiError)
  assert.equal(requests, 1)
})

test('external URLs are rejected before cookies or CSRF tokens can be sent', async () => {
  globalThis.fetch = async () => { assert.fail('Must not send a request') }
  await assert.rejects(apiRequest('https://example.test/api/notes'), /same-origin/)
})

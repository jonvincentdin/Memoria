import { useEffect, useState } from 'react'
import type { User } from '../features/auth/types'
import { AuthPage } from '../features/auth/AuthPage'
import { apiRequest, ApiError } from '../shared/api/client'
import { Failure, Loading, PageTitle } from '../shared/ui/common'
import { Shell } from './Shell'
import { Dashboard } from '../features/dashboard/Dashboard'
import { NotesPage } from '../features/notes/NotesPage'
import { ReviewersPage } from '../features/reviewers/ReviewersPage'

const pathFromHash = () => window.location.hash.replace(/^#\/?/, '').split('/').map(value => { try { return decodeURIComponent(value) } catch { return value } })

export function App() {
  const [path, setPath] = useState(pathFromHash)
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<unknown>(null)
  const [retry, setRetry] = useState(0)
  const page = path[0] || 'login'
  const publicPage = page === 'login' || page === 'signup'
  useEffect(() => {
    function navigate() { setPath(pathFromHash()); window.scrollTo(0, 0); requestAnimationFrame(() => document.querySelector<HTMLElement>('main')?.focus()) }
    function expired() { setUser(null); window.location.hash = '#/login' }
    window.addEventListener('hashchange', navigate); window.addEventListener('memoria:session-expired', expired)
    return () => { window.removeEventListener('hashchange', navigate); window.removeEventListener('memoria:session-expired', expired) }
  }, [])
  useEffect(() => {
    const controller = new AbortController()
    apiRequest<User>('/api/auth/me', { signal: controller.signal }).then(profile => { if (!controller.signal.aborted) setUser(profile) }).catch(failure => { if (!controller.signal.aborted && !(failure instanceof ApiError && failure.status === 401)) setError(failure) }).finally(() => { if (!controller.signal.aborted) setLoading(false) })
    return () => controller.abort()
  }, [retry])
  useEffect(() => { if (loading || error) return; if (!publicPage && !user) window.location.replace('#/login'); if (publicPage && user) window.location.replace('#/dashboard') }, [loading, error, publicPage, user])
  useEffect(() => { document.title = `${page === 'signup' ? 'Create account' : page === 'login' ? 'Log in' : page.charAt(0).toUpperCase() + page.slice(1)} · Memoria` }, [page])
  if (loading) return <main className="standalone"><Loading label="Checking your session…" /></main>
  if (error) return <main className="standalone"><Failure error={error} retry={() => { setLoading(true); setError(null); setRetry(value => value + 1) }} /></main>
  if (publicPage) return user ? null : <AuthPage key={page} signup={page === 'signup'} onAuthenticated={profile => { setUser(profile); window.location.hash = '#/dashboard' }} />
  if (!user) return null
  async function logout() { await apiRequest('/api/auth/logout', { method: 'POST' }); setUser(null); window.location.hash = '#/login' }
  return <Shell user={user} current={page} logout={logout}><div key={path.join('/')}>
    {page === 'dashboard' ? <Dashboard user={user} /> : page === 'notes' ? <NotesPage path={path.slice(1)} /> : page === 'reviewers' ? <ReviewersPage path={path.slice(1)} /> : <PageTitle title="Page not found" description="Choose a destination from the navigation to get back to your study space." />}
  </div></Shell>
}

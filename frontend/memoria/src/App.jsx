import { useEffect, useRef, useState } from 'react'
import './App.css'
import { ApiError, apiRequest } from './api'
import AppShell from './components/AppShell'
import AuthPage from './pages/AuthPage'
import ComingSoon from './pages/ComingSoon'
import Dashboard from './pages/Dashboard'
import { SECTIONS } from './sections'

const ROUTES = { '#/signup': 'signup', '#/dashboard': 'dashboard', ...Object.fromEntries(SECTIONS.map(({ route }) => [`#/${route}`, route])) }
const getMode = () => ROUTES[window.location.hash] || 'login'
const isPublic = (mode) => mode === 'login' || mode === 'signup'

function App() {
  const [mode, setMode] = useState(getMode)
  const [user, setUser] = useState(null)
  const [sessionLoaded, setSessionLoaded] = useState(false)
  const [sessionError, setSessionError] = useState('')
  const headingRef = useRef(null)

  useEffect(() => {
    function navigate() { setMode(getMode()); window.scrollTo(0, 0); requestAnimationFrame(() => (headingRef.current || document.getElementById('auth-main'))?.focus()) }
    window.addEventListener('hashchange', navigate)
    return () => window.removeEventListener('hashchange', navigate)
  }, [])

  useEffect(() => {
    let active = true
    apiRequest('/api/auth/me')
      .then((profile) => {
        if (active) setUser(profile)
      })
      .catch((error) => {
        if (!active) return
        setUser(null)
        if (!(error instanceof ApiError && error.status === 401)) {
          setSessionError(error instanceof ApiError
            ? error.message
            : 'Could not verify your session. Check your connection and try again.')
        }
      })
      .finally(() => {
        if (active) setSessionLoaded(true)
      })
    return () => { active = false }
  }, [])

  useEffect(() => {
    if (!sessionLoaded) return
    if (!isPublic(mode) && !user) window.location.replace('#/login')
    else if (isPublic(mode) && user) window.location.replace('#/dashboard')
  }, [mode, user, sessionLoaded])

  async function logout() {
    await apiRequest('/api/auth/logout', { method: 'POST' })
    setUser(null)
    window.location.replace('#/login')
  }

  if (!isPublic(mode)) {
    // Nothing private renders until the session check confirms a logged-in user.
    if (!sessionLoaded || !user) return null
    const profile = { ...user, name: user.name || user.email.split('@')[0] }
    const section = SECTIONS.find(({ route }) => route === mode)
    return <AppShell user={profile} current={mode} onLogout={logout}>
      {section ? <ComingSoon key={mode} section={section} /> : <Dashboard user={profile} />}
    </AppShell>
  }
  return <AuthPage
    mode={mode}
    headingRef={headingRef}
    sessionError={sessionError}
    onAuthenticated={(profile) => {
      setUser(profile)
      setSessionError('')
    }}
  />
}

export default App

import { useEffect, useRef, useState } from 'react'
import { ApiError, apiRequest } from '../api'
import Field from './Field'

export default function AuthForm({ mode, onAuthenticated }) {
  const signup = mode === 'signup'
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const firstField = useRef(null)

  useEffect(() => { document.title = `${signup ? 'Create account' : 'Log in'} · Memoria` }, [signup])

  async function submit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    const name = String(data.get('name') ?? '').trim()
    const email = String(data.get('email') ?? '').trim()
    const password = String(data.get('password') ?? '')
    const confirmation = String(data.get('confirmation') ?? '')
    const next = {}
    if (signup && !name) next.name = 'Enter your name.'
    if (!email) next.email = 'Enter your email address.'
    else if (!form.elements.email.validity.valid) next.email = 'Enter a valid email address, such as name@example.com.'
    if (!password) next.password = 'Enter your password.'
    else if (signup && password.length < 8) next.password = 'Use at least 8 characters for your password.'
    if (signup && !confirmation) next.confirmation = 'Re-enter your password.'
    else if (signup && password !== confirmation) next.confirmation = 'Your passwords don’t match. Try again.'
    setErrors(next)
    setStatus('')
    const invalid = Object.keys(next)[0]
    if (invalid) {
      form.elements[invalid].focus()
      return
    }

    setSubmitting(true)
    try {
      const user = await apiRequest(`/api/auth/${signup ? 'register' : 'login'}`, {
        method: 'POST',
        body: signup ? { name, email, password } : { email, password },
      })
      onAuthenticated(user)
      window.location.hash = '#/dashboard'
    } catch (error) {
      setStatus(error instanceof ApiError
        ? error.message
        : 'Memoria could not be reached. Check your connection and try again.')
    } finally {
      setSubmitting(false)
    }
  }

  function clearError(event) {
    const name = event.target.name
    setErrors((current) => ({ ...current, [name]: undefined }))
    setStatus('')
  }

  return <div className="auth-form-content">
    <div className="form-intro"><h1>{signup ? 'Make room for learning.' : 'Welcome back.'}</h1><p>{signup ? 'Your notes, reviewers, and practice. All together.' : 'A fresh start, or right where you left off.'}</p></div>
    <form noValidate onSubmit={submit} onChange={clearError}>
      {signup && <Field name="name" label="Full name" autoComplete="name" error={errors.name} maxLength={120} inputRef={firstField} />}
      <Field name="email" label="Email address" type="email" autoComplete="email" error={errors.email} inputRef={signup ? undefined : firstField} maxLength={254} />
      <Field name="password" label="Password" type="password" autoComplete={signup ? 'new-password' : 'current-password'} hint={signup ? 'Use at least 8 characters.' : undefined} error={errors.password} />
      {signup && <Field name="confirmation" label="Confirm password" type="password" autoComplete="new-password" error={errors.confirmation} />}
      <button className="submit-button" type="submit" disabled={submitting}>{submitting ? 'Working…' : signup ? 'Create account' : 'Log in'}<svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg></button>
      <p className="form-status" role="alert">{status}</p>
    </form>
    <p className="switch-mode">{signup ? 'Already have an account?' : 'New to Memoria?'} <a href={signup ? '#/login' : '#/signup'}>{signup ? 'Log in' : 'Create an account'}</a></p>
    <div className="preview-notice"><span className="notice-dot" /> Secure account <span>Your password is hashed before storage and never returned.</span></div>
  </div>
}

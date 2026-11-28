import { useState, type FormEvent } from 'react'
import { ArrowRight, Eye, EyeOff, FileText, Layers, Target } from 'lucide-react'
import { apiRequest } from '../../shared/api/client'
import { Brand, Field, Notice } from '../../shared/ui/common'
import { Button } from '../../shared/ui/button'
import { Input } from '../../shared/ui/input'
import type { User } from './types'

export function AuthPage({ signup, onAuthenticated }: { signup: boolean; onAuthenticated: (user: User) => void }) {
  const [visible, setVisible] = useState(false)
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<unknown>(null)
  const [errors, setErrors] = useState<Record<string, string>>({})
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (pending) return
    const form = event.currentTarget
    const values = new FormData(form)
    const name = String(values.get('name') || '').trim()
    const email = String(values.get('email') || '').trim()
    const password = String(values.get('password') || '')
    const next: Record<string, string> = {}
    if (signup && !name) next.name = 'Enter your name.'
    if (!email || !(form.elements.namedItem('email') as HTMLInputElement).validity.valid) next.email = 'Enter a valid email address.'
    if (!password) next.password = 'Enter your password.'
    else if (signup && password.length < 8) next.password = 'Use at least 8 characters.'
    if (signup && password !== values.get('confirmation')) next.confirmation = 'Your passwords do not match.'
    setErrors(next); setError(null)
    if (Object.keys(next).length) { (form.elements.namedItem(Object.keys(next)[0]) as HTMLElement)?.focus(); return }
    setPending(true)
    try {
      const user = await apiRequest<User>(`/api/auth/${signup ? 'register' : 'login'}`, { method: 'POST', body: JSON.stringify(signup ? { name, email, password } : { email, password }) })
      onAuthenticated(user)
    } catch (failure) { setError(failure) } finally { setPending(false) }
  }
  function input(name: string, label: string, type = 'text', hint?: string) { return <Field id={name} label={label} hint={hint} error={errors[name]}><Input id={name} name={name} type={type} required maxLength={name === 'name' ? 120 : name === 'email' ? 254 : undefined} autoComplete={name === 'name' ? 'name' : name === 'email' ? 'email' : signup ? 'new-password' : 'current-password'} aria-invalid={!!errors[name]} aria-describedby={errors[name] ? `${name}-error` : hint ? `${name}-hint` : undefined} /></Field> }
  return <div className="auth-layout">
    <a className="skip-link" href="#auth-main" onClick={event => { event.preventDefault(); document.getElementById('auth-main')?.focus() }}>Skip to form</a>
    <aside className="auth-story"><Brand /><div className="auth-story-body"><h2>Good notes.<br />Lasting knowledge.</h2><p>Give what you learn a place to grow.</p><ol className="learning-path"><li><FileText aria-hidden="true" /><div><strong>Capture your notes</strong><span>Keep the ideas you want to remember.</span></div></li><li><Layers aria-hidden="true" /><div><strong>Make the connections</strong><span>Shape your material into study guides.</span></div></li><li><Target aria-hidden="true" /><div><strong>Put it into practice</strong><span>Build confidence, one session at a time.</span></div></li></ol></div><p className="auth-footnote">A little progress, every day.</p></aside>
    <main id="auth-main" tabIndex={-1} className="auth-main"><div className="mobile-brand"><Brand /></div><div className="auth-form"><h1>{signup ? 'Start your next chapter.' : 'Welcome back.'}</h1><p>{signup ? 'Create a space for your notes and learning.' : 'Your study space is right where you left it.'}</p><form noValidate onSubmit={submit} onChange={() => { setError(null); setErrors({}) }}>
      {signup && input('name', 'Full name')}{input('email', 'Email address', 'email')}
      <Field id="password" label="Password" hint={signup ? 'At least 8 characters.' : undefined} error={errors.password}><div className="password-wrap"><Input id="password" name="password" type={visible ? 'text' : 'password'} required autoComplete={signup ? 'new-password' : 'current-password'} aria-invalid={!!errors.password} aria-describedby={errors.password ? 'password-error' : signup ? 'password-hint' : undefined} /><Button variant="ghost" size="icon" aria-label={visible ? 'Hide password' : 'Show password'} aria-pressed={visible} onClick={() => setVisible(value => !value)}>{visible ? <EyeOff aria-hidden="true" /> : <Eye aria-hidden="true" />}</Button></div></Field>
      {signup && input('confirmation', 'Confirm password', visible ? 'text' : 'password')}{!!error && <Notice error={error} />}<Button type="submit" className="auth-submit" disabled={pending}>{pending ? 'Please wait…' : signup ? 'Create account' : 'Log in'}<ArrowRight aria-hidden="true" /></Button>
    </form><p className="auth-switch">{signup ? 'Already have an account?' : 'New to Memoria?'} <a href={signup ? '#/login' : '#/signup'}>{signup ? 'Log in' : 'Create an account'}</a></p></div><p className="auth-footnote">Your learning, in one place.</p></main>
  </div>
}

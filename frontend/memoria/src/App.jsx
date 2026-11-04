import { useEffect, useRef, useState } from 'react'
import './App.css'

function BookMark({ className = '' }) {
  return <svg className={className} width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true"><path d="M14 7.5C10.5 4.5 6.5 5 3 6.5V23c3.5-1.5 7.5-2 11 1 3.5-3 7.5-2.5 11-1V6.5c-3.5-1.5-7.5-2-11 1Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round"/><path d="M14 7.5V24M7 10h3M7 14h3M18 10h3M18 14h3" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/></svg>
}

function Eye({ visible }) {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" strokeWidth="1.5"/><circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.5"/>{visible && <path d="m4 3 16 18" stroke="currentColor" strokeWidth="1.5"/>}</svg>
}

const getMode = () => window.location.hash === '#/signup' ? 'signup' : 'login'

function StudyPreview() {
  const [tab, setTab] = useState('Notes')
  const [answer, setAnswer] = useState(null)
  return <div className="study-preview">
    <div className="preview-heading"><span className="preview-dot" /> Your study space <span className="preview-caption">A little preview</span></div>
    <div className="preview-tabs" role="tablist" aria-label="Explore the study preview">
      {['Notes', 'Reviewer', 'Quiz'].map((item) => <button key={item} id={`tab-${item}`} role="tab" aria-selected={tab === item} aria-controls="study-panel" tabIndex={tab === item ? 0 : -1} onClick={() => { setTab(item); setAnswer(null) }} onKeyDown={(event) => {
        const tabs = ['Notes', 'Reviewer', 'Quiz']
        let index = tabs.indexOf(tab)
        if (event.key === 'ArrowRight') index = (index + 1) % tabs.length
        else if (event.key === 'ArrowLeft') index = (index + tabs.length - 1) % tabs.length
        else if (event.key === 'Home') index = 0
        else if (event.key === 'End') index = tabs.length - 1
        else return
        event.preventDefault(); setTab(tabs[index]); setAnswer(null); document.getElementById(`tab-${tabs[index]}`).focus()
      }}>{item}</button>)}
    </div>
    <div className="preview-content" id="study-panel" role="tabpanel" aria-labelledby={`tab-${tab}`} tabIndex="0">
      <div className="subject-label">Biology / The basics</div>
      <h3>Small cells. Big ideas.</h3>
      {tab === 'Notes' && <><p>Every living thing begins with a cell. Think of it as a tiny workspace, with each part doing its own job.</p><div className="note-excerpt"><span className="note-heading">Remember this</span><p>The <mark>mitochondria</mark> produce most of the energy a cell needs.</p></div><div className="note-bottom"><span>One idea at a time.</span><svg width="40" height="22" viewBox="0 0 40 22" fill="none" aria-hidden="true"><path d="M2 16c10-13 15 9 26-7m-6 0 7-1-1 7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/></svg></div></>}
      {tab === 'Reviewer' && <><p>A quick recap, in your own words.</p><dl className="reviewer-list"><div><dt>Nucleus</dt><dd>Stores the cell's genetic instructions.</dd></div><div><dt>Mitochondria</dt><dd>Produce energy the cell can use.</dd></div><div><dt>Cell membrane</dt><dd>Controls what enters and leaves.</dd></div></dl></>}
      {tab === 'Quiz' && <><p>Which part produces most of the cell's energy?</p><div className="quiz-answers">{['Nucleus', 'Mitochondria', 'Cell membrane'].map((option) => <button key={option} type="button" className={answer === option ? 'answer-selected' : ''} aria-pressed={answer === option} onClick={() => setAnswer(option)}>{option}<span aria-hidden="true">{answer === option ? '✓' : '→'}</span></button>)}</div><p className="quiz-feedback" role="status">{answer ? answer === 'Mitochondria' ? 'Exactly. A little practice goes a long way.' : 'Try again. Look for the cell’s energy producer.' : 'Try a practice question.'}</p></>}
    </div>
  </div>
}

function Field({ name, label, type = 'text', error, hint, autoComplete, inputRef, onChange, maxLength }) {
  const [visible, setVisible] = useState(false)
  const isPassword = type === 'password'
  return <div className="field">
    <label htmlFor={name}>{label}</label>
    <div className="input-wrap">
      <input ref={inputRef} id={name} name={name} type={isPassword && visible ? 'text' : type} autoComplete={autoComplete} aria-invalid={Boolean(error)} aria-describedby={error ? `${name}-error` : hint ? `${name}-hint` : undefined} onChange={onChange} maxLength={maxLength} required />
      {isPassword && <button className="password-toggle" type="button" aria-label={`${visible ? 'Hide' : 'Show'} ${label.toLowerCase()}`} aria-pressed={visible} onClick={() => setVisible(!visible)}><Eye visible={visible} /></button>}
    </div>
    {hint && !error && <p className="field-hint" id={`${name}-hint`}>{hint}</p>}
    {error && <p className="field-error" id={`${name}-error`}>{error}</p>}
  </div>
}

function AuthForm({ mode }) {
  const signup = mode === 'signup'
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState('')
  const firstField = useRef(null)

  useEffect(() => { document.title = `${signup ? 'Create account' : 'Log in'} · Memoria` }, [signup])

  function submit(event) {
    event.preventDefault()
    const form = event.currentTarget
    const data = new FormData(form)
    const next = {}
    if (signup && !data.get('name').trim()) next.name = 'Enter your name.'
    if (!data.get('email').trim()) next.email = 'Enter your email address.'
    else if (!form.elements.email.validity.valid) next.email = 'Enter a valid email address, such as name@example.com.'
    if (!data.get('password')) next.password = 'Enter your password.'
    else if (signup && data.get('password').length < 8) next.password = 'Use at least 8 characters for your password.'
    if (signup && !data.get('confirmation')) next.confirmation = 'Re-enter your password.'
    else if (signup && data.get('password') !== data.get('confirmation')) next.confirmation = 'Your passwords don’t match. Try again.'
    setErrors(next)
    setStatus('')
    const invalid = Object.keys(next)[0]
    if (invalid) form.elements[invalid].focus()
    else setStatus('This is a design preview. Account access isn’t connected yet. Your details haven’t been sent or saved.')
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
      <button className="submit-button" type="submit">{signup ? 'Create account' : 'Log in'}<svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M5 12h14m-6-6 6 6-6 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round"/></svg></button>
      <p className="form-status" role="status">{status}</p>
    </form>
    <p className="switch-mode">{signup ? 'Already have an account?' : 'New to Memoria?'} <a href={signup ? '#/login' : '#/signup'}>{signup ? 'Log in' : 'Create an account'}</a></p>
    <div className="preview-notice"><span className="notice-dot" /> Design preview <span>Account access is coming next.</span></div>
  </div>
}

function App() {
  const [mode, setMode] = useState(getMode)
  const headingRef = useRef(null)
  useEffect(() => {
    function navigate() { setMode(getMode()); window.scrollTo(0, 0); requestAnimationFrame(() => headingRef.current?.focus()) }
    window.addEventListener('hashchange', navigate)
    return () => window.removeEventListener('hashchange', navigate)
  }, [])
  return <div className={`auth-layout ${mode}`}>
    <a className="skip-link" href="#auth-main" onClick={(event) => {
      event.preventDefault()
      headingRef.current?.focus()
      headingRef.current?.scrollIntoView({ block: 'start' })
    }}>Skip to form</a>
    <aside className="story-panel" aria-label="About Memoria">
      <a className="brand" href="#/login" aria-label="Memoria log in"><BookMark /><span>memoria<span className="brand-period">.</span></span></a>
      <div className="story-content"><h2>A little more organized.<br /><em>A little more prepared.</em></h2><p>A place for your notes, the connections you make,<br className="desktop-break" /> and the things you want to remember.</p><StudyPreview /></div>
      <div className="story-footer"><span>Made for the way you learn.</span><span className="small-book"><BookMark /></span></div>
    </aside>
    <main className="form-panel" id="auth-main" ref={headingRef} tabIndex="-1">
      <a className="brand mobile-brand" href="#/login" aria-label="Memoria log in"><BookMark /><span>memoria.</span></a>
      <div className="top-link"><span>{mode === 'signup' ? 'Already part of Memoria?' : 'A new chapter?'} </span><a href={mode === 'signup' ? '#/login' : '#/signup'}>{mode === 'signup' ? 'Log in' : 'Join Memoria'}<span aria-hidden="true"> ↗</span></a></div>
      <AuthForm key={mode} mode={mode} />
      <footer className="form-footer">A little progress, every day.</footer>
    </main>
  </div>
}

export default App

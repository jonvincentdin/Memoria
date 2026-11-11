import AuthForm from '../components/AuthForm'
import { BookMark } from '../components/Icons'
import StudyPreview from '../components/StudyPreview'

export default function AuthPage({ mode, headingRef, sessionError, onAuthenticated }) {
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
      <div className="top-link"><span>{mode === 'login' ? 'A new chapter?' : 'Already part of Memoria?'} </span><a href={mode === 'login' ? '#/signup' : '#/login'}>{mode === 'login' ? 'Join Memoria' : 'Log in'}<span aria-hidden="true"> ↗</span></a></div>
      {sessionError && <p className="form-status" role="alert">{sessionError}</p>}
      <AuthForm key={mode} mode={mode} onAuthenticated={onAuthenticated} />
      <footer className="form-footer">A little progress, every day.</footer>
    </main>
  </div>
}

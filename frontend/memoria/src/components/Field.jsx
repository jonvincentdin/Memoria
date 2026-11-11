import { useState } from 'react'
import { Eye } from './Icons'

export default function Field({ name, label, type = 'text', error, hint, autoComplete, inputRef, onChange, maxLength }) {
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

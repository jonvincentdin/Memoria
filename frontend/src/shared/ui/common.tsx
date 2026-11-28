import { AlertCircle, ArrowLeft, BookOpen, RotateCcw } from 'lucide-react'
import type { ReactNode } from 'react'
import { ApiError } from '../api/client'
import { Button } from './button'
import { message } from '../errors'

export function Brand() { return <a className="brand" href="#/dashboard" aria-label="Memoria home"><BookOpen aria-hidden="true" /><span>memoria<span className="brand-dot">.</span></span></a> }
export function Notice({ error, children }: { error?: unknown; children?: ReactNode }) { return <div className="notice" role="alert"><AlertCircle size={18} aria-hidden="true" /><div>{children || message(error)}</div></div> }
export function Loading({ label = 'Loading your study space…' }: { label?: string }) { return <div className="loading" role="status"><span className="skeleton" /><span className="skeleton" /><p>{label}</p></div> }
export function Failure({ error, retry }: { error: unknown; retry: () => void }) { const missing = error instanceof ApiError && error.status === 404; return <div className="empty"><h2>{missing ? 'This material is unavailable' : 'Could not load this page'}</h2><p>{missing ? 'It may have been removed, or you may not have access.' : message(error)}</p>{!missing && <Button variant="outline" onClick={retry}><RotateCcw aria-hidden="true" />Try again</Button>}</div> }
export function Empty({ title, children, action }: { title: string; children: ReactNode; action?: ReactNode }) { return <div className="empty"><BookOpen aria-hidden="true" size={28} /><h2>{title}</h2><p>{children}</p>{action}</div> }
export function Back({ href, children }: { href: string; children: ReactNode }) { return <a className="back-link" href={href}><ArrowLeft size={16} aria-hidden="true" />{children}</a> }
export function PageTitle({ title, description, actions }: { title: string; description?: string; actions?: ReactNode }) { return <header className="page-title"><div><h1>{title}</h1>{description && <p>{description}</p>}</div>{actions && <div className="actions">{actions}</div>}</header> }
export function Field({ id, label, hint, error, children }: { id: string; label: string; hint?: string; error?: string; children: ReactNode }) { return <div className="field"><label htmlFor={id}>{label}</label>{children}{error ? <p className="field-error" id={`${id}-error`}>{error}</p> : hint ? <p className="field-hint" id={`${id}-hint`}>{hint}</p> : null}</div> }

export function BookMark({ className = '' }) {
  return <svg className={className} width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden="true"><path d="M14 7.5C10.5 4.5 6.5 5 3 6.5V23c3.5-1.5 7.5-2 11 1 3.5-3 7.5-2.5 11-1V6.5c-3.5-1.5-7.5-2-11 1Z" stroke="currentColor" strokeWidth="1.7" strokeLinejoin="round"/><path d="M14 7.5V24M7 10h3M7 14h3M18 10h3M18 14h3" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/></svg>
}

export function Eye({ visible }) {
  return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" strokeWidth="1.5"/><circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.5"/>{visible && <path d="m4 3 16 18" stroke="currentColor" strokeWidth="1.5"/>}</svg>
}

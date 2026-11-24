export const RATING_LABELS = {
  1: 'Not recalled',
  2: 'A little',
  3: 'Mostly',
  4: 'Correctly',
  5: 'Easily',
}

export const STATUS_LABELS = {
  NEW: 'New',
  LEARNING: 'Learning',
  MASTERED: 'Mastered',
}

// Spring Boot serializes UTC LocalDateTime without a trailing 'Z'; treat timezone-less ISO strings as UTC.
export function parseUtcDate(value) {
  if (!value) return null
  const normalized = typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(value) && !/[zZ]|[+-]\d{2}:?\d{2}$/.test(value)
    ? `${value}Z`
    : value
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

// Formats a saved timestamp as a readable date, e.g. "November 12, 2026".
export function formatDate(value) {
  const date = parseUtcDate(value)
  return date
    ? date.toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })
    : ''
}

// Formats a compact date, e.g. "Nov 28, 2026".
export function formatShortDate(value) {
  const date = parseUtcDate(value)
  return date
    ? date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
    : ''
}

// Formats a saved timestamp with date and time, e.g. "Nov 25, 2026, 3:15 PM".
export function formatDateTime(value) {
  const date = parseUtcDate(value)
  return date
    ? date.toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    })
    : ''
}

export function formatInterval(days) {
  if (!days || days <= 0) return 'Not scheduled'
  return days === 1 ? '1 day' : `${days} days`
}

export function scopeQuery(reviewerId?: string, dueOnly = false) {
  const params = new URLSearchParams()
  if (reviewerId) params.set('reviewerId', reviewerId)
  if (dueOnly) params.set('dueOnly', 'true')
  return params.size ? `?${params}` : ''
}

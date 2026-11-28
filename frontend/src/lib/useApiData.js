import { useEffect, useState } from 'react'
import { ApiError, apiRequest } from '@/api'

// Turns any request error into a message the student can read.
export function errorMessage(error) {
  return error instanceof ApiError
    ? error.message
    : 'Memoria could not be reached. Check your connection and try again.'
}

// Loads data from a GET endpoint. Returns { status: 'loading' | 'ready' | 'error', data, error } and a retry function.
// Pass null as the path to skip the request.
export function useApiData(path) {
  const [state, setState] = useState(path ? { status: 'loading' } : { status: 'ready', data: null })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (!path) return undefined
    let active = true
    apiRequest(path)
      .then((data) => { if (active) setState({ status: 'ready', data }) })
      .catch((error) => { if (active) setState({ status: 'error', error }) })
    return () => { active = false }
  }, [path, attempt])

  function retry() {
    setState({ status: 'loading' })
    setAttempt((count) => count + 1)
  }

  return [state, retry]
}

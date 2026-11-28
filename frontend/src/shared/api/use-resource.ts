import { useCallback, useEffect, useState } from 'react'
import { apiRequest } from './client'

export function useResource<T>(url: string | null) {
  const [revision, setRevision] = useState(0)
  const [state, setState] = useState<{ key: string | null; revision: number; data: T | null; error: unknown }>({ key: null, revision: -1, data: null, error: null })
  const reload = useCallback(() => setRevision(value => value + 1), [])
  useEffect(() => {
    if (!url) return
    const controller = new AbortController()
    apiRequest<T>(url, { signal: controller.signal }).then(data => { if (!controller.signal.aborted) setState({ key: url, revision, data, error: null }) })
      .catch(error => { if (!controller.signal.aborted) setState({ key: url, revision, data: null, error }) })
    return () => controller.abort()
  }, [url, revision])
  const current = state.key === url && state.revision === revision
  return { data: current ? state.data : null, error: current ? state.error : null, loading: !!url && !current, reload }
}

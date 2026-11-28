import { apiRequest as request } from './client'
export { ApiError } from './client'

type Options = Omit<RequestInit, 'body'> & { body?: unknown }

/** Feature adapters submit JSON objects or native multipart uploads. */
export function apiRequest<T = unknown>(path: string, { body, ...options }: Options = {}): Promise<T> {
  return request<T>(path, {
    ...options,
    body: body === undefined ? undefined : body instanceof FormData ? body : JSON.stringify(body),
  })
}

import type { AuthSession } from '../../features/auth/domain/types'

export const API_ORIGIN = import.meta.env.VITE_API_ORIGIN ?? 'http://localhost:8080'
const API_BASE_URL = `${API_ORIGIN}/api/v1`
const SESSION_KEY = 'clothwise.session'

export class ApiError extends Error {
  status: number
  details?: unknown

  constructor(
    message: string,
    status: number,
    details?: unknown,
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

export function readStoredSession(): AuthSession | null {
  const raw = localStorage.getItem(SESSION_KEY)
  if (!raw) return null

  try {
    return JSON.parse(raw) as AuthSession
  } catch {
    localStorage.removeItem(SESSION_KEY)
    return null
  }
}

export function persistSession(session: AuthSession | null) {
  if (!session) {
    localStorage.removeItem(SESSION_KEY)
    return
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
}

async function parseResponse<T>(response: Response): Promise<T> {
  const text = await response.text()
  const payload = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new ApiError(
      payload?.message ?? `Error HTTP ${response.status}`,
      response.status,
      payload,
    )
  }

  return payload as T
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const session = readStoredSession()
  const headers = new Headers(options.headers)

  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData

  if (!headers.has('Content-Type') && options.body && !isFormData) {
    headers.set('Content-Type', 'application/json')
  }

  if (session?.token) {
    headers.set('Authorization', `Bearer ${session.token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  })

  return parseResponse<T>(response)
}

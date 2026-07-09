import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../../../infra/api/authApi'
import { persistSession, readStoredSession, SESSION_EXPIRED_EVENT, sessionExpirationMs } from '../../../infra/api/httpClient'
import type { AuthSession } from '../domain/types'
import { AuthContext, type AuthContextValue } from './AuthContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => readStoredSession())
  const logout = useCallback(() => {
    setSession(null)
    persistSession(null)
  }, [])

  useEffect(() => {
    if (!session) return

    const timeoutId = window.setTimeout(logout, Math.max(sessionExpirationMs(session), 0))
    return () => window.clearTimeout(timeoutId)
  }, [logout, session])

  useEffect(() => {
    window.addEventListener(SESSION_EXPIRED_EVENT, logout)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, logout)
  }, [logout])

  const value = useMemo<AuthContextValue>(() => ({
    session,
    isAuthenticated: Boolean(session?.token),
    async login(payload) {
      const nextSession = await authApi.login(payload)
      setSession(nextSession)
      persistSession(nextSession)
    },
    logout,
  }), [logout, session])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

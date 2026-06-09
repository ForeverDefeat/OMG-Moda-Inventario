import { useMemo, useState, type ReactNode } from 'react'
import { authApi } from '../../../infra/api/authApi'
import { persistSession, readStoredSession } from '../../../infra/api/httpClient'
import type { AuthSession } from '../domain/types'
import { AuthContext, type AuthContextValue } from './AuthContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => readStoredSession())

  const value = useMemo<AuthContextValue>(() => ({
    session,
    isAuthenticated: Boolean(session?.token),
    async login(payload) {
      const nextSession = await authApi.login(payload)
      setSession(nextSession)
      persistSession(nextSession)
    },
    logout() {
      setSession(null)
      persistSession(null)
    },
  }), [session])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

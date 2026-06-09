import { createContext } from 'react'
import type { AuthSession, LoginRequest } from '../domain/types'

export interface AuthContextValue {
  session: AuthSession | null
  isAuthenticated: boolean
  login: (payload: LoginRequest) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

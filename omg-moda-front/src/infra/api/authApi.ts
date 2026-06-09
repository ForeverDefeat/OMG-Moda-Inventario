import type { AuthSession, LoginRequest } from '../../features/auth/domain/types'
import { apiRequest } from './httpClient'

export const authApi = {
  login(payload: LoginRequest) {
    return apiRequest<AuthSession>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
}

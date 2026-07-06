import type {
  AppUser,
  CreateUserRequest,
  ResetPasswordRequest,
  UpdateUserRoleRequest,
} from '../../features/users/domain/types'
import { apiRequest } from './httpClient'

export const usersApi = {
  list() {
    return apiRequest<AppUser[]>('/usuarios')
  },
  create(payload: CreateUserRequest) {
    return apiRequest<AppUser>('/usuarios', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
  updateRole(id: number, payload: UpdateUserRoleRequest) {
    return apiRequest<AppUser>(`/usuarios/${id}/rol`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  resetPassword(id: number, payload: ResetPasswordRequest) {
    return apiRequest<AppUser>(`/usuarios/${id}/contrasenia`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    })
  },
  deactivate(id: number) {
    return apiRequest<AppUser>(`/usuarios/${id}/desactivar`, {
      method: 'PUT',
    })
  },
  reactivate(id: number) {
    return apiRequest<AppUser>(`/usuarios/${id}/reactivar`, {
      method: 'PUT',
    })
  },
}

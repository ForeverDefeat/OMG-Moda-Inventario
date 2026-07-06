import type { UserRole } from '../../auth/domain/types'

export type { UserRole }

export interface AppUser {
  id: number
  nombre: string
  correo: string
  rol: UserRole
  activo: boolean
}

export interface CreateUserRequest {
  nombre: string
  correo: string
  contrasenia: string
  rol: UserRole
}

export interface UpdateUserRoleRequest {
  rol: UserRole
}

export interface ResetPasswordRequest {
  nuevaContrasenia: string
}

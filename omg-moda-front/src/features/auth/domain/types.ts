export type UserRole = 'ADMIN' | 'VENDEDOR'

export interface LoginRequest {
  correo: string
  contrasenia: string
}

export interface AuthSession {
  token: string
  tipo: 'Bearer'
  rol: UserRole
  nombre: string
  correo: string
  expiracion: string
}

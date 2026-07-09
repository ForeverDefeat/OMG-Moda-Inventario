import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth/application/useAuth'
import { canAccessPath, getHomePathForRole } from '../layout/navigation'

export function RoleRoute() {
  const { session } = useAuth()
  const location = useLocation()

  if (!canAccessPath(session?.rol, location.pathname)) {
    return <Navigate to={getHomePathForRole(session?.rol)} replace />
  }

  return <Outlet />
}

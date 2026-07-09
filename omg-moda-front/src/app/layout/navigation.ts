import {
  BarChart3,
  Boxes,
  ClipboardList,
  LayoutDashboard,
  PackagePlus,
  ReceiptText,
  ShoppingCart,
  UserCog,
  Users,
  type LucideIcon,
} from 'lucide-react'
import type { UserRole } from '../../features/auth/domain/types'

export interface NavItem {
  label: string
  href: string
  icon: LucideIcon
  adminOnly?: boolean
}

const sellerAllowedPaths = new Set(['/catalogo', '/ventas', '/clientes', '/compras'])

export function getVisibleNavItems(role?: UserRole | null) {
  if (role === 'VENDEDOR') {
    return navItems.filter((item) => sellerAllowedPaths.has(item.href))
  }

  return navItems.filter((item) => !item.adminOnly || role === 'ADMIN')
}

export function getHomePathForRole(role?: UserRole | null) {
  return role === 'VENDEDOR' ? '/catalogo' : '/dashboard'
}

export function canAccessPath(role: UserRole | undefined | null, pathname: string) {
  if (role === 'VENDEDOR') {
    return sellerAllowedPaths.has(pathname)
  }

  return role === 'ADMIN'
}

export const navItems: NavItem[] = [
  { label: 'Panel de Control', href: '/dashboard', icon: LayoutDashboard, adminOnly: true },
  { label: 'Catalogo de Productos', href: '/catalogo', icon: Boxes },
  { label: 'Gestion de Stock', href: '/stock', icon: PackagePlus, adminOnly: true },
  { label: 'Ventas y POS', href: '/ventas', icon: ShoppingCart },
  { label: 'Pagos', href: '/pagos', icon: ReceiptText, adminOnly: true },
  { label: 'Reportes', href: '/reportes', icon: BarChart3, adminOnly: true },
  { label: 'Clientes', href: '/clientes', icon: Users },
  { label: 'Ordenes de Compra', href: '/compras', icon: ClipboardList },
  { label: 'Usuarios', href: '/usuarios', icon: UserCog, adminOnly: true },
]

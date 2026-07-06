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

export interface NavItem {
  label: string
  href: string
  icon: LucideIcon
  adminOnly?: boolean
}

export const navItems: NavItem[] = [
  { label: 'Panel de Control', href: '/dashboard', icon: LayoutDashboard },
  { label: 'Catalogo de Productos', href: '/catalogo', icon: Boxes },
  { label: 'Gestion de Stock', href: '/stock', icon: PackagePlus },
  { label: 'Ventas y POS', href: '/ventas', icon: ShoppingCart },
  { label: 'Pagos', href: '/pagos', icon: ReceiptText, adminOnly: true },
  { label: 'Reportes', href: '/reportes', icon: BarChart3 },
  { label: 'Clientes', href: '/clientes', icon: Users },
  { label: 'Ordenes de Compra', href: '/compras', icon: ClipboardList },
  { label: 'Usuarios', href: '/usuarios', icon: UserCog, adminOnly: true },
]

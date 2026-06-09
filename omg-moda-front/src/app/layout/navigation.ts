import {
  BarChart3,
  Boxes,
  ClipboardList,
  LayoutDashboard,
  PackagePlus,
  ShoppingCart,
  Users,
} from 'lucide-react'

export const navItems = [
  { label: 'Panel de Control', href: '/dashboard', icon: LayoutDashboard },
  { label: 'Catalogo de Productos', href: '/catalogo', icon: Boxes },
  { label: 'Gestion de Stock', href: '/stock', icon: PackagePlus },
  { label: 'Ventas y POS', href: '/ventas', icon: ShoppingCart },
  { label: 'Reportes', href: '/reportes', icon: BarChart3 },
  { label: 'Clientes', href: '/clientes', icon: Users },
  { label: 'Ordenes de Compra', href: '/compras', icon: ClipboardList },
] as const

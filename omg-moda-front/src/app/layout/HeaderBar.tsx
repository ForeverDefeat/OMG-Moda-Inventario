import { useState } from 'react'
import { Bell, LogOut, Menu, Search, Shirt } from 'lucide-react'
import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth/application/useAuth'
import { Drawer } from '../../shared/components/Drawer'
import { IconButton } from '../../shared/components/IconButton'
import { cn } from '../../shared/utils/cn'
import { navItems } from './navigation'

const titles: Record<string, { title: string; subtitle: string }> = {
  '/dashboard': {
    title: 'Panel de Control',
    subtitle: 'Resumen de la semana, alertas y acciones rapidas.',
  },
  '/catalogo': {
    title: 'Catalogo de Productos',
    subtitle: 'Gestiona prendas, variantes, precios y stock.',
  },
  '/stock': {
    title: 'Gestion de Stock',
    subtitle: 'Entradas, ajustes e inventario operativo.',
  },
  '/ventas': {
    title: 'Ventas y POS',
    subtitle: 'Registra ventas, arma carrito y revisa tendencias.',
  },
  '/clientes': {
    title: 'Clientes',
    subtitle: 'Perfiles, segmentos y valor historico del cliente.',
  },
  '/compras': {
    title: 'Ordenes de Compra',
    subtitle: 'Sugerencias y reposicion inteligente.',
  },
  '/reportes': {
    title: 'Reportes',
    subtitle: 'Perspectivas sobre ventas, inventario y rotacion.',
  },
}

export function HeaderBar() {
  const { pathname } = useLocation()
  const { session, logout } = useAuth()
  const [mobileOpen, setMobileOpen] = useState(false)
  const copy = titles[pathname] ?? titles['/dashboard']

  return (
    <header className="sticky top-0 z-20 border-b border-[var(--color-border)] bg-[var(--color-surface)]">
      <div className="flex min-h-[var(--header-height)] items-center justify-between gap-4 px-4 py-3 sm:px-6">
        <div className="flex min-w-[210px] items-center gap-3">
          <div className="grid size-10 shrink-0 place-items-center rounded-[var(--radius-lg)] bg-[var(--color-primary)] text-white">
            <Shirt size={20} />
          </div>
          <div className="min-w-0">
            <p className="text-sm font-bold leading-tight text-[var(--color-text)]">OMG MODA</p>
            <p className="truncate text-xs text-[var(--color-muted)]">Helping retailers manage stock smarter.</p>
          </div>
        </div>

        <nav className="hidden min-w-0 flex-1 items-center justify-center gap-1 overflow-x-auto lg:flex">
          {navItems.map(({ href, label }) => (
            <NavLink
              key={href}
              to={href}
              className={({ isActive }) => cn(
                'shrink-0 whitespace-nowrap rounded-xl px-3 py-2 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-[var(--color-primary)] text-white'
                  : 'text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
              )}
            >
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="hidden min-w-[220px] max-w-sm items-center rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 md:flex">
          <Search size={16} className="text-[var(--color-muted)]" />
          <input
            className="ml-2 w-full bg-transparent text-sm outline-none placeholder:text-[var(--color-muted)]"
            placeholder="Buscar..."
          />
        </div>

        <div className="flex items-center gap-2">
          <IconButton label="Menu" icon={Menu} className="lg:hidden" onClick={() => setMobileOpen(true)} />
          <IconButton label="Notificaciones" icon={Bell} />
          <div className="hidden min-w-[150px] text-right sm:block">
            <p className="text-sm font-semibold text-[var(--color-text)]">{session?.nombre ?? 'Usuario'}</p>
            <p className="text-xs text-[var(--color-muted)]">{session?.rol ?? 'ADMIN'}</p>
          </div>
          <IconButton label="Cerrar sesion" icon={LogOut} onClick={logout} />
        </div>
      </div>

      <div className="border-t border-[var(--color-border)] px-4 py-3 sm:px-6 lg:hidden">
        <h1 className="truncate text-xl font-bold text-[var(--color-text)]">{copy.title}</h1>
        <p className="truncate text-sm text-[var(--color-muted)]">{copy.subtitle}</p>
      </div>

      <Drawer title="Navegacion" isOpen={mobileOpen} onClose={() => setMobileOpen(false)} side="left">
        <nav className="grid gap-2">
          {navItems.map(({ href, icon: Icon, label }) => (
            <NavLink
              key={href}
              to={href}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => cn(
                'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium',
                isActive ? 'bg-[var(--color-primary)] text-white' : 'text-[var(--color-muted)] hover:bg-[var(--color-bg)]',
              )}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
      </Drawer>
    </header>
  )
}

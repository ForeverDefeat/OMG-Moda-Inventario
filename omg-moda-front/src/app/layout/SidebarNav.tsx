import { NavLink } from 'react-router-dom'
import { Crown } from 'lucide-react'
import { useAuth } from '../../features/auth/application/useAuth'
import { navItems } from './navigation'
import { cn } from '../../shared/utils/cn'

export function SidebarNav() {
  const { session } = useAuth()
  const visibleNavItems = navItems.filter((item) => !item.adminOnly || session?.rol === 'ADMIN')

  return (
    <aside className="hidden w-[var(--sidebar-width)] shrink-0 border-r border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-5 lg:block">
      <div className="mb-7 flex items-center gap-3 px-2">
        <div className="grid size-11 place-items-center rounded-[var(--radius-md)] bg-[var(--color-primary)] text-white shadow-[var(--shadow-card)]">
          <Crown size={22} />
        </div>
        <div>
          <p className="display-title text-xl font-semibold text-[var(--color-primary-strong)]">OMG MODA</p>
          <p className="text-xs uppercase tracking-[0.18em] text-[var(--color-muted)]">Inventario</p>
        </div>
      </div>

      <nav className="space-y-1">
        {visibleNavItems.map(({ href, icon: Icon, label }) => (
          <NavLink
            key={href}
            to={href}
            className={({ isActive }) => cn(
              'flex items-center gap-3 rounded-[var(--radius-md)] px-3 py-2.5 text-sm font-medium transition',
              isActive
                ? 'bg-[var(--color-primary-soft)] text-[var(--color-primary-strong)]'
                : 'text-[var(--color-muted)] hover:bg-[var(--color-bg-soft)] hover:text-[var(--color-text)]',
            )}
          >
            <Icon size={18} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}

import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Bell, LogOut, Menu, Search, Shirt } from 'lucide-react'
import { NavLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/application/useAuth'
import type { Variant } from '../../features/catalog/domain/types'
import { productsApi } from '../../infra/api/productsApi'
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

function normalizeSearch(value: string) {
  return value.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()
}

export function HeaderBar() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { session, logout } = useAuth()
  const [mobileOpen, setMobileOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [searchFocused, setSearchFocused] = useState(false)
  const [productSuggestionsSource, setProductSuggestionsSource] = useState<Variant[]>([])
  const copy = titles[pathname] ?? titles['/dashboard']
  const searchTerm = search.trim()
  const encodedSearch = encodeURIComponent(searchTerm)

  useEffect(() => {
    productsApi.listVariants()
      .then(setProductSuggestionsSource)
      .catch(() => setProductSuggestionsSource([]))
  }, [])

  const searchSuggestions = useMemo(() => {
    if (!searchTerm) return []

    const normalized = normalizeSearch(searchTerm)
    const pageMatches = navItems
      .filter((item) => normalizeSearch(item.label).includes(normalized))
      .map((item) => ({
        id: `page-${item.href}`,
        label: item.label,
        meta: 'Modulo',
        href: item.href,
      }))

    const productMatches = productSuggestionsSource
      .filter((variant) =>
        [variant.nombreProducto, variant.categoria, variant.color, variant.talla]
          .some((field) => normalizeSearch(field).includes(normalized)),
      )
      .slice(0, 4)
      .map((variant) => ({
        id: `product-${variant.idVariante}`,
        label: variant.nombreProducto,
        meta: `${variant.categoria} / ${variant.talla} / ${variant.color}`,
        href: `/catalogo?q=${encodeURIComponent(variant.nombreProducto)}`,
      }))

    const suggestions = [...pageMatches, ...productMatches].slice(0, 6)

    if (suggestions.length) return suggestions

    return [{
      id: 'catalog-search',
      label: `Buscar "${searchTerm}"`,
      meta: 'Catalogo de productos',
      href: `/catalogo?q=${encodedSearch}`,
    }]
  }, [encodedSearch, productSuggestionsSource, searchTerm])

  function goToSearchResult(href: string) {
    navigate(href)
    setSearch('')
    setSearchFocused(false)
  }

  function handleSearchSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!searchTerm) return
    goToSearchResult(searchSuggestions[0]?.href ?? `/catalogo?q=${encodedSearch}`)
  }

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
                  ? 'bg-[var(--color-primary-soft)] text-[var(--color-text)] ring-1 ring-inset ring-black/10'
                  : 'text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
              )}
            >
              {label}
            </NavLink>
          ))}
        </nav>

        <form
          onSubmit={handleSearchSubmit}
          className="relative hidden min-w-[220px] max-w-sm items-center rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 transition focus-within:border-[var(--color-text)] md:flex"
          role="search"
          aria-label="Busqueda global"
        >
          <Search size={16} className="text-[var(--color-muted)]" />
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            onFocus={() => setSearchFocused(true)}
            onBlur={() => window.setTimeout(() => setSearchFocused(false), 120)}
            className="ml-2 w-full bg-transparent text-sm outline-none placeholder:text-[var(--color-muted)]"
            placeholder="Buscar modulo o producto..."
          />
          {searchFocused && searchSuggestions.length > 0 && (
            <div className="absolute right-0 top-[calc(100%+8px)] z-30 w-[min(360px,calc(100vw-2rem))] overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-[var(--shadow-float)]">
              {searchSuggestions.map((suggestion) => (
                <button
                  key={suggestion.id}
                  type="button"
                  onMouseDown={(event) => {
                    event.preventDefault()
                    goToSearchResult(suggestion.href)
                  }}
                  className="flex w-full items-center justify-between gap-3 px-4 py-3 text-left text-sm transition hover:bg-[var(--color-bg)]"
                >
                  <span className="min-w-0">
                    <span className="block truncate font-semibold text-[var(--color-text)]">{suggestion.label}</span>
                    <span className="block truncate text-xs text-[var(--color-muted)]">{suggestion.meta}</span>
                  </span>
                  <Search size={15} className="shrink-0 text-[var(--color-muted)]" />
                </button>
              ))}
            </div>
          )}
        </form>

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
                isActive ? 'bg-[var(--color-primary-soft)] text-[var(--color-text)] ring-1 ring-inset ring-black/10' : 'text-[var(--color-muted)] hover:bg-[var(--color-bg)]',
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

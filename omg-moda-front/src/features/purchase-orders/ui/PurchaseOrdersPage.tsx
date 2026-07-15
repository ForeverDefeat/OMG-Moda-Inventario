import { useEffect, useMemo, useState } from 'react'
import { ClipboardList, Link2, PackageSearch, Truck } from 'lucide-react'
import type { PurchaseSuggestion } from '../domain/types'
import { purchaseApi } from '../../../infra/api/purchaseApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { SearchInput } from '../../../shared/components/SearchInput'
import { useAuth } from '../../auth/application/useAuth'

const columns: Column<PurchaseSuggestion>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.producto}</span>, sortable: true, sortValue: (row) => row.producto },
  { key: 'proveedor', header: 'Proveedor', render: (row) => row.proveedor, sortable: true, sortValue: (row) => row.proveedor },
  { key: 'cantidad', header: 'Cantidad sugerida', render: (row) => row.cantidadSugerida, sortable: true, sortValue: (row) => row.cantidadSugerida, sortType: 'number' },
  { key: 'costo', header: 'Costo estimado', render: (row) => `S/ ${row.costoEstimado.toFixed(2)}`, sortable: true, sortValue: (row) => row.costoEstimado, sortType: 'number' },
  { key: 'prioridad', header: 'Prioridad', render: (row) => <StatusBadge status={row.prioridad} />, sortable: true, sortValue: (row) => row.prioridad },
]

export function PurchaseOrdersPage() {
  const { session } = useAuth()
  const [suggestions, setSuggestions] = useState<PurchaseSuggestion[]>([])
  const [query, setQuery] = useState('')
  const [priorityFilter, setPriorityFilter] = useState('Todas')
  const [providerFilter, setProviderFilter] = useState('Todos')

  const providers = useMemo(() => Array.from(new Set(suggestions.map((item) => item.proveedor)))
    .sort((a, b) => a.localeCompare(b, 'es', { sensitivity: 'base' })), [suggestions])
  const filteredSuggestions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase()

    return suggestions.filter((item) => {
      const matchesQuery = !normalizedQuery
        || item.producto.toLowerCase().includes(normalizedQuery)
        || item.proveedor.toLowerCase().includes(normalizedQuery)
        || item.motivo.toLowerCase().includes(normalizedQuery)
        || item.prioridad.toLowerCase().includes(normalizedQuery)
      const matchesPriority = priorityFilter === 'Todas' || item.prioridad === priorityFilter
      const matchesProvider = providerFilter === 'Todos' || item.proveedor === providerFilter

      return matchesQuery && matchesPriority && matchesProvider
    })
  }, [providerFilter, priorityFilter, query, suggestions])
  const total = filteredSuggestions.reduce((sum, item) => sum + item.costoEstimado, 0)
  const visibleProviders = new Set(filteredSuggestions.map((item) => item.proveedor)).size
  const hasActiveFilters = Boolean(query.trim()) || priorityFilter !== 'Todas' || providerFilter !== 'Todos'
  const canManagePurchaseOrders = session?.rol === 'ADMIN'

  useEffect(() => {
    purchaseApi.listSuggestions()
      .then((data) => {
        setSuggestions(data)
      })
      .catch(() => {
        setSuggestions([])
      })
  }, [])

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-8 lg:flex-row">
        <aside className="grid shrink-0 gap-4 sm:grid-cols-3 lg:flex lg:w-64 lg:flex-col">
          <KpiCard label="Sugerencias" value={String(filteredSuggestions.length)} helper={`${suggestions.length} totales`} icon={PackageSearch} />
          <KpiCard label="Costo estimado" value={`S/ ${total.toLocaleString('es-PE', { maximumFractionDigits: 2 })}`} icon={ClipboardList} tone="warning" />
          <KpiCard label="Proveedores activos" value={String(visibleProviders)} icon={Truck} tone="success" />
        </aside>

        <div className="min-w-0 flex-1">
          <section className="mb-4 grid gap-3 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 md:grid-cols-[1fr_auto_auto_auto] md:items-center">
            <SearchInput value={query} onChange={setQuery} placeholder="Buscar producto, proveedor o motivo" />
            <select
              value={priorityFilter}
              onChange={(event) => setPriorityFilter(event.target.value)}
              className="min-h-11 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-sm font-semibold text-[var(--color-text)] outline-none md:min-h-10 md:w-auto"
              aria-label="Filtrar por prioridad"
            >
              <option value="Todas">Todas las prioridades</option>
              <option value="Alta">Alta</option>
              <option value="Media">Media</option>
              <option value="Baja">Baja</option>
            </select>
            <select
              value={providerFilter}
              onChange={(event) => setProviderFilter(event.target.value)}
              className="min-h-11 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-sm font-semibold text-[var(--color-text)] outline-none md:min-h-10 md:w-auto"
              aria-label="Filtrar por proveedor"
            >
              <option value="Todos">Todos los proveedores</option>
              {providers.map((provider) => (
                <option key={provider} value={provider}>{provider}</option>
              ))}
            </select>
            {hasActiveFilters && (
              <button
                type="button"
                onClick={() => {
                  setQuery('')
                  setPriorityFilter('Todas')
                  setProviderFilter('Todos')
                }}
                className="min-h-10 rounded-xl border border-[var(--color-border)] px-3 text-sm font-semibold text-[var(--color-muted)] transition hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]"
              >
                Limpiar
              </button>
            )}
          </section>

          <section className="grid max-h-[420px] gap-4 overflow-y-auto pr-1 md:grid-cols-2 xl:grid-cols-4">
            {filteredSuggestions.map((item) => (
              <article key={item.id} className="dashboard-card p-5">
                <StatusBadge status={item.prioridad} />
                <h3 className="mt-3 font-bold">{item.producto}</h3>
                <p className="mt-1 text-sm text-[var(--color-muted)]">{item.motivo}</p>
                {canManagePurchaseOrders && (
                  <ActionButton variant="secondary" className="mt-4 w-full"><Link2 size={16} /> Vincular a Stock</ActionButton>
                )}
              </article>
            ))}
          </section>

          <div className="mt-6">
            <DataTable rows={filteredSuggestions} columns={columns} emptyText="Sin sugerencias de compra" maxHeight="380px" />
          </div>
        </div>
      </section>
    </div>
  )
}

import { ClipboardList, Link2, PackageSearch, Truck } from 'lucide-react'
import type { PurchaseSuggestion } from '../domain/types'
import { mockPurchaseSuggestions } from '../../../infra/mock/mockData'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'

const columns: Column<PurchaseSuggestion>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.producto}</span> },
  { key: 'proveedor', header: 'Proveedor', render: (row) => row.proveedor },
  { key: 'cantidad', header: 'Cantidad sugerida', render: (row) => row.cantidadSugerida },
  { key: 'costo', header: 'Costo estimado', render: (row) => `S/ ${row.costoEstimado.toFixed(2)}` },
  { key: 'prioridad', header: 'Prioridad', render: (row) => <StatusBadge status={row.prioridad} /> },
]

export function PurchaseOrdersPage() {
  const total = mockPurchaseSuggestions.reduce((sum, item) => sum + item.costoEstimado, 0)

  return (
    <div className="page-grid">
      <section>
        <h1 className="panel-title">Ordenes de Compra</h1>
        <p className="text-sm text-[var(--color-muted)]">Sugerencias por bajo stock y proveedores listos para vincular.</p>
      </section>

      <section className="flex flex-col gap-8 lg:flex-row">
        <aside className="flex shrink-0 flex-col gap-4 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 lg:w-64">
          <KpiCard label="Sugerencias" value={String(mockPurchaseSuggestions.length)} icon={PackageSearch} />
          <KpiCard label="Costo estimado" value={`S/ ${total.toLocaleString('es-PE')}`} icon={ClipboardList} tone="warning" />
          <KpiCard label="Proveedores activos" value="4" icon={Truck} tone="success" />
        </aside>

        <div className="min-w-0 flex-1">
          <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {mockPurchaseSuggestions.map((item) => (
              <article key={item.id} className="dashboard-card p-5">
                <StatusBadge status={item.prioridad} />
                <h3 className="mt-3 font-bold">{item.producto}</h3>
                <p className="mt-1 text-sm text-[var(--color-muted)]">{item.motivo}</p>
                <ActionButton variant="secondary" className="mt-4 w-full"><Link2 size={16} /> Vincular a Stock</ActionButton>
              </article>
            ))}
          </section>

          <div className="mt-6">
            <DataTable rows={mockPurchaseSuggestions} columns={columns} />
          </div>
        </div>
      </section>
    </div>
  )
}

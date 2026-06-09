import { AlertTriangle, Boxes, CircleDollarSign, ClipboardList, ShoppingBag } from 'lucide-react'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { DonutMetricChart } from '../../../shared/charts/DonutMetricChart'
import { LineTrendChart } from '../../../shared/charts/LineTrendChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { categoryData, mockPurchaseSuggestions, mockVariants, reportBars, trendData } from '../../../infra/mock/mockData'
import type { PurchaseSuggestion } from '../../purchase-orders/domain/types'

const columns: Column<PurchaseSuggestion>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.producto}</span> },
  { key: 'cantidad', header: 'Cantidad', render: (row) => row.cantidadSugerida },
  { key: 'prioridad', header: 'Prioridad', render: (row) => <StatusBadge status={row.prioridad} /> },
  { key: 'motivo', header: 'Motivo', render: (row) => <span className="text-[var(--color-muted)]">{row.motivo}</span> },
]

export function DashboardPage() {
  const stockValue = mockVariants.reduce((total, variant) => total + variant.precioCosto * variant.stockActual, 0)
  const lowStock = mockVariants.filter((variant) => variant.stockActual <= variant.stockMinimo).length

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="panel-title">Panel de Control</h1>
          <p className="text-sm text-[var(--color-muted)]">Resumen ejecutivo de stock, ventas y reposicion.</p>
        </div>
        <div className="flex gap-2">
          <button className="rounded-xl border border-[var(--color-border)] px-4 py-2 text-sm font-medium">7 dias</button>
          <button className="rounded-xl bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-white">Hoy</button>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <KpiCard label="Valor del stock" value={`S/ ${stockValue.toLocaleString('es-PE')}`} helper="+8% esta semana" icon={CircleDollarSign} />
        <KpiCard label="SKUs activos" value={String(mockVariants.length)} helper="6 categorias" icon={Boxes} tone="success" />
        <KpiCard label="Alertas de stock" value={String(lowStock)} helper="requieren accion" icon={AlertTriangle} tone="warning" />
        <KpiCard label="Ventas de hoy" value="S/ 2,410" helper="POS activo" icon={ShoppingBag} tone="primary" />
        <KpiCard label="Compras sugeridas" value={String(mockPurchaseSuggestions.length)} helper="bajo stock" icon={ClipboardList} tone="danger" />
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.4fr_0.8fr]">
        <ChartCard title="Tendencia de ventas" subtitle="Lectura semanal desde POS">
          <LineTrendChart data={trendData} />
        </ChartCard>
        <ChartCard title="Ventas por categoria" subtitle="Distribucion actual">
          <DonutMetricChart data={categoryData} />
        </ChartCard>
      </section>

      <section className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
        <ChartCard title="Rotacion mensual" subtitle="Indice de salida por mes">
          <BarMetricChart data={reportBars} />
        </ChartCard>
        <div>
          <div className="mb-3">
            <h2 className="text-xl font-bold">Sugerencias de reposicion</h2>
            <p className="text-sm text-[var(--color-muted)]">Acciones rapidas inspiradas en el panel Banana AI.</p>
          </div>
          <DataTable rows={mockPurchaseSuggestions.slice(0, 3)} columns={columns} />
        </div>
      </section>
    </div>
  )
}

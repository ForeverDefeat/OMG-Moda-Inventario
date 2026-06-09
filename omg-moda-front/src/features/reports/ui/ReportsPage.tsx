import { BarChart3, CircleDollarSign, PackageMinus, TrendingUp } from 'lucide-react'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { DonutMetricChart } from '../../../shared/charts/DonutMetricChart'
import { LineTrendChart } from '../../../shared/charts/LineTrendChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { categoryData, mockVariants, reportBars, trendData } from '../../../infra/mock/mockData'
import type { Variant } from '../../catalog/domain/types'

const columns: Column<Variant>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.nombreProducto}</span> },
  { key: 'categoria', header: 'Categoria', render: (row) => row.categoria },
  { key: 'stock', header: 'Stock', render: (row) => row.stockActual },
  { key: 'accion', header: 'Accion sugerida', render: (row) => <StatusBadge status={row.stockActual <= row.stockMinimo ? 'Reponer' : 'Mantener'} /> },
]

export function ReportsPage() {
  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Reportes y Analisis</h1>
          <p className="text-sm text-[var(--color-muted)]">Perspectivas sobre rendimiento de ventas, salud del stock y rotacion.</p>
        </div>
        <button className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-2 text-sm font-medium">
          Ultimos 30 dias
        </button>
      </section>

      <section className="grid gap-4 md:grid-cols-4">
        <KpiCard label="Ventas mes" value="S/ 24,880" icon={CircleDollarSign} />
        <KpiCard label="Crecimiento" value="+18%" icon={TrendingUp} tone="success" />
        <KpiCard label="SKUs con alerta" value="2" icon={PackageMinus} tone="warning" />
        <KpiCard label="Reportes activos" value="6" icon={BarChart3} tone="primary" />
      </section>

      <section className="grid gap-4 xl:grid-cols-3">
        <ChartCard title="Rendimiento de ventas" subtitle="Ingresos diarios">
          <LineTrendChart data={trendData} />
        </ChartCard>
        <ChartCard title="Ventas por canal" subtitle="Participacion por categoria">
          <DonutMetricChart data={categoryData} />
        </ChartCard>
        <ChartCard title="Rotacion de inventario" subtitle="Indice mensual">
          <BarMetricChart data={reportBars} />
        </ChartCard>
      </section>

      <DataTable rows={mockVariants} columns={columns} />
    </div>
  )
}

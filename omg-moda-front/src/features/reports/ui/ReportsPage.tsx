import { useEffect, useState } from 'react'
import { BarChart3, CircleDollarSign, PackageMinus, TrendingUp } from 'lucide-react'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { DonutMetricChart } from '../../../shared/charts/DonutMetricChart'
import { LineTrendChart } from '../../../shared/charts/LineTrendChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { reportsApi } from '../../../infra/api/reportsApi'
import type { Variant } from '../../catalog/domain/types'
import type { MetricDatum, ReportSummary } from '../domain/types'

const columns: Column<Variant>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.nombreProducto}</span> },
  { key: 'categoria', header: 'Categoria', render: (row) => row.categoria },
  { key: 'stock', header: 'Stock', render: (row) => row.stockActual },
  { key: 'accion', header: 'Accion sugerida', render: (row) => <StatusBadge status={row.stockActual <= row.stockMinimo ? 'Reponer' : 'Mantener'} /> },
]

const emptySummary: ReportSummary = {
  ventasMes: 0,
  crecimientoPorcentaje: 0,
  skusConAlerta: 0,
  reportesActivos: 0,
}

function formatCurrency(value: number) {
  return `S/ ${value.toLocaleString('es-PE', { maximumFractionDigits: 2 })}`
}

export function ReportsPage() {
  const [summary, setSummary] = useState<ReportSummary>(emptySummary)
  const [trend, setTrend] = useState<MetricDatum[]>([])
  const [category, setCategory] = useState<MetricDatum[]>([])
  const [rotation, setRotation] = useState<MetricDatum[]>([])
  const [alerts, setAlerts] = useState<Variant[]>([])
  const [status, setStatus] = useState('Cargando reportes desde backend.')

  useEffect(() => {
    Promise.all([
      reportsApi.getSummary(),
      reportsApi.getSalesTrend('30d'),
      reportsApi.getSalesByCategory(),
      reportsApi.getRotation({ meses: 6 }),
      reportsApi.getStockAlerts(),
    ])
      .then(([summaryData, trendResponse, categoryResponse, rotationData, alertData]) => {
        setSummary(summaryData)
        setTrend(trendResponse)
        setCategory(categoryResponse)
        setRotation(rotationData)
        setAlerts(alertData)
        setStatus('Reportes conectados al backend.')
      })
      .catch(() => {
        setSummary(emptySummary)
        setTrend([])
        setCategory([])
        setRotation([])
        setAlerts([])
        setStatus('Backend no disponible. No se muestran datos mock.')
      })
  }, [])

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Reportes y Analisis</h1>
          <p className="text-sm text-[var(--color-muted)]">{status}</p>
        </div>
        <button className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-2 text-sm font-medium">
          Ultimos 30 dias
        </button>
      </section>

      <section className="grid gap-4 md:grid-cols-4">
        <KpiCard label="Ventas mes" value={formatCurrency(summary.ventasMes)} icon={CircleDollarSign} />
        <KpiCard label="Crecimiento" value={`${summary.crecimientoPorcentaje.toFixed(2)}%`} icon={TrendingUp} tone="success" />
        <KpiCard label="SKUs con alerta" value={String(summary.skusConAlerta)} icon={PackageMinus} tone="warning" />
        <KpiCard label="Reportes activos" value={String(summary.reportesActivos)} icon={BarChart3} tone="primary" />
      </section>

      <section className="grid gap-4 xl:grid-cols-3">
        <ChartCard title="Rendimiento de ventas" subtitle="Ingresos diarios">
          <LineTrendChart data={trend} />
        </ChartCard>
        <ChartCard title="Ventas por canal" subtitle="Participacion por categoria">
          <DonutMetricChart data={category} />
        </ChartCard>
        <ChartCard title="Rotacion de inventario" subtitle="Indice mensual">
          <BarMetricChart data={rotation} />
        </ChartCard>
      </section>

      <DataTable rows={alerts} columns={columns} emptyText="Sin alertas de stock" />
    </div>
  )
}

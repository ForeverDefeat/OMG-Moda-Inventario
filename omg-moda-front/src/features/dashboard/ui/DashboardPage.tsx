import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { AlertTriangle, Boxes, CircleDollarSign, ClipboardList, ShoppingBag, SlidersHorizontal } from 'lucide-react'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { DonutMetricChart } from '../../../shared/charts/DonutMetricChart'
import { LineTrendChart } from '../../../shared/charts/LineTrendChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { dashboardApi } from '../../../infra/api/dashboardApi'
import { cn } from '../../../shared/utils/cn'
import type { DashboardResponse, MetricDatum } from '../domain/types'
import type { PurchaseSuggestion } from '../../purchase-orders/domain/types'

const periodOptions = [
  { value: '7d', label: '7 dias' },
  { value: 'today', label: 'Hoy' },
] as const

type DashboardPeriod = (typeof periodOptions)[number]['value']

const channelOptions = [
  { value: 'all', label: 'Todos los canales' },
  { value: 'pos', label: 'POS tienda' },
  { value: 'online', label: 'Online' },
] as const

type SalesChannel = (typeof channelOptions)[number]['value']

const emptyDashboard: DashboardResponse = {
  kpis: {
    valorStock: 0,
    skusActivos: 0,
    alertasStock: 0,
    ventasPeriodo: 0,
    comprasSugeridas: 0,
  },
  ventasTendencia: [],
  ventasCategoria: [],
  rotacion: [],
  sugerencias: [],
}

const columns: Column<PurchaseSuggestion>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.producto}</span> },
  { key: 'cantidad', header: 'Cantidad', render: (row) => row.cantidadSugerida },
  { key: 'prioridad', header: 'Prioridad', render: (row) => <StatusBadge status={row.prioridad} /> },
  { key: 'motivo', header: 'Motivo', render: (row) => <span className="text-[var(--color-muted)]">{row.motivo}</span> },
  {
    key: 'accion',
    header: 'Accion',
    render: () => (
      <Link
        to="/compras"
        className="inline-flex min-h-9 items-center rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 text-sm font-semibold transition hover:bg-[var(--color-bg)]"
      >
        Abrir orden
      </Link>
    ),
  },
]

function formatCurrency(value: number) {
  return `S/ ${value.toLocaleString('es-PE', { maximumFractionDigits: 2 })}`
}

function formatCurrencyCompact(value: number) {
  if (value >= 1000) return `S/ ${(value / 1000).toFixed(value >= 10000 ? 0 : 1)}k`
  return `S/ ${value}`
}

function SelectControl({ label, value, options, onChange }: {
  label: string
  value: string
  options: readonly { value: string; label: string }[]
  onChange: (value: string) => void
}) {
  return (
    <label className="flex items-center gap-2 rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm">
      <SlidersHorizontal size={15} className="text-[var(--color-muted)]" />
      <span className="sr-only">{label}</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="bg-transparent text-sm font-semibold text-[var(--color-text)] outline-none"
        aria-label={label}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>{option.label}</option>
        ))}
      </select>
    </label>
  )
}

function MetricPill({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2">
      <p className="text-[0.68rem] font-semibold uppercase tracking-[0.08em] text-[var(--color-muted)]">{label}</p>
      <p className="mt-1 text-sm font-bold text-[var(--color-text)]">{value}</p>
    </div>
  )
}

export function DashboardPage() {
  const [period, setPeriod] = useState<DashboardPeriod>('today')
  const [salesChannel, setSalesChannel] = useState<SalesChannel>('all')
  const [rotationCategory, setRotationCategory] = useState('Todas')
  const [dashboard, setDashboard] = useState<DashboardResponse>(emptyDashboard)
  const [status, setStatus] = useState('Cargando panel desde backend.')

  useEffect(() => {
    const categoria = rotationCategory === 'Todas' ? undefined : rotationCategory
    dashboardApi.getDashboard({ periodo: period, canal: salesChannel, categoria })
      .then((data) => {
        setDashboard(data)
        setStatus('Panel conectado al backend.')
      })
      .catch(() => {
        setDashboard(emptyDashboard)
        setStatus('Backend no disponible. No se muestran datos mock.')
      })
  }, [period, rotationCategory, salesChannel])

  const rotationOptions = useMemo(() => {
    const categories = dashboard.ventasCategoria.map((item) => item.name)
    return ['Todas', ...Array.from(new Set(categories))]
  }, [dashboard.ventasCategoria])

  const activeTrend = dashboard.ventasTendencia
  const totalSales = dashboard.kpis.ventasPeriodo
  const peakSale = activeTrend.reduce<MetricDatum>(
    (peak, item) => item.value > peak.value ? item : peak,
    { name: '-', value: 0 },
  )
  const averageSale = activeTrend.length ? Math.round(totalSales / activeTrend.length) : 0
  const activeCategories = dashboard.ventasCategoria
  const activeRotation = dashboard.rotacion
  const rotationPeak = activeRotation.reduce<MetricDatum>(
    (peak, item) => item.value > peak.value ? item : peak,
    { name: '-', value: 0 },
  )
  const rotationAverage = activeRotation.length
    ? Math.round(activeRotation.reduce((sum, item) => sum + item.value, 0) / activeRotation.length)
    : 0
  const salesLabel = period === 'today' ? 'Ventas de hoy' : 'Ventas 7 dias'
  const salesHelper = salesChannel === 'all'
    ? `${period === 'today' ? 'jornada activa' : 'semana movil'}`
    : channelOptions.find((option) => option.value === salesChannel)?.label ?? 'Canal filtrado'

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="panel-title">Panel de Control</h1>
          <p className="text-sm text-[var(--color-muted)]">{status}</p>
        </div>
        <div className="flex gap-2" role="group" aria-label="Periodo del panel">
          {periodOptions.map((option) => (
            <button
              key={option.value}
              type="button"
              aria-pressed={period === option.value}
              onClick={() => setPeriod(option.value)}
              className={cn(
                'rounded-xl px-4 py-2 text-sm font-semibold transition',
                period === option.value
                  ? 'bg-[var(--color-primary)] text-[var(--color-primary-foreground)] shadow-sm'
                  : 'border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-muted)] hover:text-[var(--color-text)]',
              )}
            >
              {option.label}
            </button>
          ))}
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        <KpiCard label="Valor del stock" value={formatCurrency(dashboard.kpis.valorStock)} helper="desde inventario" icon={CircleDollarSign} />
        <KpiCard label="SKUs activos" value={String(dashboard.kpis.skusActivos)} helper="productos reales" icon={Boxes} tone="success" />
        <KpiCard label="Alertas de stock" value={String(dashboard.kpis.alertasStock)} helper="requieren accion" icon={AlertTriangle} tone="warning" />
        <KpiCard label={salesLabel} value={formatCurrency(totalSales)} helper={salesHelper} icon={ShoppingBag} tone="primary" />
        <KpiCard label="Compras sugeridas" value={String(dashboard.kpis.comprasSugeridas)} helper="bajo stock" icon={ClipboardList} tone="danger" />
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.4fr_0.8fr]">
        <ChartCard
          title="Tendencia de ventas"
          subtitle={period === 'today' ? 'Lectura por hora desde POS' : 'Lectura semanal desde POS'}
          actions={(
            <SelectControl
              label="Filtrar canal de ventas"
              value={salesChannel}
              options={channelOptions}
              onChange={(value) => setSalesChannel(value as SalesChannel)}
            />
          )}
        >
          <div className="grid h-full min-h-0 grid-rows-[minmax(0,1fr)_auto] gap-3">
            <LineTrendChart data={activeTrend} valueFormatter={formatCurrencyCompact} />
            <div className="grid gap-2 sm:grid-cols-3">
              <MetricPill label="Total" value={formatCurrency(totalSales)} />
              <MetricPill label="Pico" value={`${peakSale.name} / ${formatCurrency(peakSale.value)}`} />
              <MetricPill label="Promedio" value={formatCurrency(averageSale)} />
            </div>
          </div>
        </ChartCard>
        <ChartCard title="Ventas por categoria" subtitle={period === 'today' ? 'Distribucion de hoy' : 'Distribucion de 7 dias'}>
          <DonutMetricChart data={activeCategories} />
        </ChartCard>
      </section>

      <section className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
        <ChartCard
          title="Rotacion mensual"
          subtitle="Indice de salida por mes"
          actions={(
            <SelectControl
              label="Filtrar categoria de rotacion"
              value={rotationCategory}
              options={rotationOptions.map((option) => ({ value: option, label: option }))}
              onChange={setRotationCategory}
            />
          )}
        >
          <div className="grid h-full min-h-0 grid-rows-[minmax(0,1fr)_auto] gap-3">
            <BarMetricChart data={activeRotation} />
            <div className="grid gap-2 sm:grid-cols-3">
              <MetricPill label="Categoria" value={rotationCategory} />
              <MetricPill label="Mes pico" value={`${rotationPeak.name} / ${rotationPeak.value}`} />
              <MetricPill label="Promedio" value={String(rotationAverage)} />
            </div>
          </div>
        </ChartCard>
        <div>
          <div className="mb-3 flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h2 className="text-xl font-bold">Sugerencias de reposicion</h2>
              <p className="text-sm text-[var(--color-muted)]">Acciones calculadas desde stock real.</p>
            </div>
            <Link
              to="/compras"
              className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-md)] border border-[var(--color-border)] px-4 text-sm font-semibold transition hover:bg-[var(--color-bg)]"
            >
              Ver compras
            </Link>
          </div>
          <DataTable rows={dashboard.sugerencias} columns={columns} emptyText="Sin sugerencias de reposicion" />
        </div>
      </section>
    </div>
  )
}

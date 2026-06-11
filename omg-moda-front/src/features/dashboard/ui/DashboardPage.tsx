import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { AlertTriangle, Boxes, CircleDollarSign, ClipboardList, ShoppingBag, SlidersHorizontal } from 'lucide-react'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { DonutMetricChart } from '../../../shared/charts/DonutMetricChart'
import { LineTrendChart } from '../../../shared/charts/LineTrendChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { categoryData, mockPurchaseSuggestions, mockVariants, reportBars, trendData } from '../../../infra/mock/mockData'
import { cn } from '../../../shared/utils/cn'
import type { PurchaseSuggestion } from '../../purchase-orders/domain/types'

type ChartDatum = { name: string; value: number }

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

const channelFactors: Record<SalesChannel, number> = {
  all: 1,
  pos: 0.78,
  online: 0.22,
}

const trendByPeriod: Record<DashboardPeriod, ChartDatum[]> = {
  today: [
    { name: '09h', value: 280 },
    { name: '11h', value: 390 },
    { name: '13h', value: 315 },
    { name: '15h', value: 525 },
    { name: '17h', value: 420 },
    { name: '19h', value: 480 },
  ],
  '7d': trendData,
}

const categoriesByPeriod: Record<DashboardPeriod, ChartDatum[]> = {
  today: [
    { name: 'Camisas', value: 18 },
    { name: 'Vestidos', value: 12 },
    { name: 'Sacos', value: 10 },
    { name: 'Tops', value: 9 },
  ],
  '7d': categoryData,
}

const rotationOptions = ['Todas', 'Camisas', 'Vestidos', 'Sacos', 'Tops'] as const
type RotationCategory = (typeof rotationOptions)[number]

const rotationByCategory: Record<RotationCategory, ChartDatum[]> = {
  Todas: reportBars,
  Camisas: [
    { name: 'Ene', value: 24 },
    { name: 'Feb', value: 32 },
    { name: 'Mar', value: 38 },
    { name: 'Abr', value: 35 },
    { name: 'May', value: 48 },
    { name: 'Jun', value: 52 },
  ],
  Vestidos: [
    { name: 'Ene', value: 18 },
    { name: 'Feb', value: 21 },
    { name: 'Mar', value: 29 },
    { name: 'Abr', value: 26 },
    { name: 'May', value: 34 },
    { name: 'Jun', value: 41 },
  ],
  Sacos: [
    { name: 'Ene', value: 12 },
    { name: 'Feb', value: 16 },
    { name: 'Mar', value: 22 },
    { name: 'Abr', value: 18 },
    { name: 'May', value: 25 },
    { name: 'Jun', value: 30 },
  ],
  Tops: [
    { name: 'Ene', value: 30 },
    { name: 'Feb', value: 34 },
    { name: 'Mar', value: 37 },
    { name: 'Abr', value: 42 },
    { name: 'May', value: 47 },
    { name: 'Jun', value: 55 },
  ],
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
  return `S/ ${value.toLocaleString('es-PE')}`
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
  const [rotationCategory, setRotationCategory] = useState<RotationCategory>('Todas')

  const stockValue = mockVariants.reduce((total, variant) => total + variant.precioCosto * variant.stockActual, 0)
  const lowStock = mockVariants.filter((variant) => variant.stockActual <= variant.stockMinimo).length

  const activeTrend = useMemo(() => {
    const factor = channelFactors[salesChannel]
    return trendByPeriod[period].map((item) => ({
      ...item,
      value: Math.round(item.value * factor),
    }))
  }, [period, salesChannel])

  const totalSales = activeTrend.reduce((sum, item) => sum + item.value, 0)
  const peakSale = activeTrend.reduce((peak, item) => item.value > peak.value ? item : peak, activeTrend[0])
  const averageSale = Math.round(totalSales / activeTrend.length)
  const activeCategories = categoriesByPeriod[period]
  const activeRotation = rotationByCategory[rotationCategory]
  const rotationPeak = activeRotation.reduce((peak, item) => item.value > peak.value ? item : peak, activeRotation[0])
  const rotationAverage = Math.round(activeRotation.reduce((sum, item) => sum + item.value, 0) / activeRotation.length)
  const salesLabel = period === 'today' ? 'Ventas de hoy' : 'Ventas 7 dias'
  const salesHelper = salesChannel === 'all'
    ? `${period === 'today' ? 'jornada activa' : 'semana movil'}`
    : channelOptions.find((option) => option.value === salesChannel)?.label ?? 'Canal filtrado'

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="panel-title">Panel de Control</h1>
          <p className="text-sm text-[var(--color-muted)]">Resumen ejecutivo de stock, ventas y reposicion.</p>
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
        <KpiCard label="Valor del stock" value={formatCurrency(stockValue)} helper="+8% esta semana" icon={CircleDollarSign} />
        <KpiCard label="SKUs activos" value={String(mockVariants.length)} helper="6 categorias" icon={Boxes} tone="success" />
        <KpiCard label="Alertas de stock" value={String(lowStock)} helper="requieren accion" icon={AlertTriangle} tone="warning" />
        <KpiCard label={salesLabel} value={formatCurrency(totalSales)} helper={salesHelper} icon={ShoppingBag} tone="primary" />
        <KpiCard label="Compras sugeridas" value={String(mockPurchaseSuggestions.length)} helper="bajo stock" icon={ClipboardList} tone="danger" />
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
              onChange={(value) => setRotationCategory(value as RotationCategory)}
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
              <p className="text-sm text-[var(--color-muted)]">Acciones rapidas inspiradas en el panel Banana AI.</p>
            </div>
            <Link
              to="/compras"
              className="inline-flex min-h-10 items-center justify-center rounded-[var(--radius-md)] border border-[var(--color-border)] px-4 text-sm font-semibold transition hover:bg-[var(--color-bg)]"
            >
              Ver compras
            </Link>
          </div>
          <DataTable rows={mockPurchaseSuggestions.slice(0, 3)} columns={columns} />
        </div>
      </section>
    </div>
  )
}

import { useEffect, useMemo, useState } from 'react'
import { BarChart3, CalendarDays, CalendarRange, CircleDollarSign, Download, FileText, PackageMinus, ShoppingBag, Tags, TrendingUp } from 'lucide-react'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { DonutMetricChart } from '../../../shared/charts/DonutMetricChart'
import { LineTrendChart } from '../../../shared/charts/LineTrendChart'
import { ActionButton } from '../../../shared/components/ActionButton'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { reportsApi } from '../../../infra/api/reportsApi'
import type { Variant } from '../../catalog/domain/types'
import type { MetricDatum, ReportFilters, ReportSummary } from '../domain/types'

const columns: Column<Variant>[] = [
  {
    key: 'producto',
    header: 'Producto',
    sortable: true,
    sortValue: (row) => row.nombreProducto,
    render: (row) => (
      <span className="grid gap-0.5">
        <span className="font-semibold">{row.nombreProducto}</span>
        <span className="text-xs font-semibold text-[var(--color-muted)]">{row.sku}</span>
      </span>
    ),
  },
  {
    key: 'categoria',
    header: 'Categoria',
    sortable: true,
    sortValue: (row) => row.categoria,
    render: (row) => row.categoria,
  },
  {
    key: 'stock',
    header: 'Stock',
    sortable: true,
    sortType: 'number',
    sortValue: (row) => row.stockActual,
    render: (row) => row.stockActual,
  },
  {
    key: 'accion',
    header: 'Accion sugerida',
    sortable: true,
    sortValue: (row) => (row.stockActual <= row.stockMinimo ? 'Reponer' : 'Mantener'),
    render: (row) => <StatusBadge status={row.stockActual <= row.stockMinimo ? 'Reponer' : 'Mantener'} />,
  },
]

const emptySummary: ReportSummary = {
  ventasMes: 0,
  crecimientoPorcentaje: 0,
  skusConAlerta: 0,
  reportesActivos: 0,
  unidadesVendidas: 0,
  ticketPromedio: 0,
  categoriaPrincipal: 'Sin datos',
  productoMasVendido: 'Sin datos',
}

const reportOptions = [
  { value: 'resumen', label: 'Resumen' },
  { value: 'rotacion', label: 'Rotacion' },
] as const

type DownloadReport = (typeof reportOptions)[number]['value']
type RangeMode = 'manual' | 'guided'
type GuidedRange = 'week' | 'month'

function formatCurrency(value: number) {
  return `S/ ${value.toLocaleString('es-PE', { maximumFractionDigits: 2 })}`
}

function formatCompactCurrency(value: number) {
  return `S/ ${value.toLocaleString('es-PE', { maximumFractionDigits: 0 })}`
}

function formatUnits(value: number) {
  return value.toLocaleString('es-PE', { maximumFractionDigits: 0 })
}

function formatDateInput(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatMonthInput(date: Date) {
  return formatDateInput(date).slice(0, 7)
}

function toIsoStart(date: string) {
  return `${date}T00:00:00`
}

function toIsoEnd(date: string) {
  return `${date}T23:59:59`
}

function addDays(date: Date, days: number) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function weekBounds(baseDate: string) {
  const date = new Date(`${baseDate}T00:00:00`)
  const day = date.getDay()
  const diffToMonday = day === 0 ? -6 : 1 - day
  const start = addDays(date, diffToMonday)
  const end = addDays(start, 6)
  return { start: formatDateInput(start), end: formatDateInput(end) }
}

function monthBounds(month: string) {
  const [year, monthIndex] = month.split('-').map(Number)
  const start = new Date(year, monthIndex - 1, 1)
  const end = new Date(year, monthIndex, 0)
  return { start: formatDateInput(start), end: formatDateInput(end) }
}

function rangeLabel(start: string, end: string) {
  const startLabel = new Date(`${start}T00:00:00`).toLocaleDateString('es-PE')
  const endLabel = new Date(`${end}T00:00:00`).toLocaleDateString('es-PE')
  return `${startLabel} - ${endLabel}`
}

function saveBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}

export function ReportsPage() {
  const [downloadType, setDownloadType] = useState<DownloadReport>('resumen')
  const today = useMemo(() => new Date(), [])
  const [rangeMode, setRangeMode] = useState<RangeMode>('guided')
  const [guidedRange, setGuidedRange] = useState<GuidedRange>('month')
  const [manualStart, setManualStart] = useState(formatDateInput(addDays(today, -29)))
  const [manualEnd, setManualEnd] = useState(formatDateInput(today))
  const [weekDate, setWeekDate] = useState(formatDateInput(today))
  const [monthDate, setMonthDate] = useState(formatMonthInput(today))
  const [summary, setSummary] = useState<ReportSummary>(emptySummary)
  const [trend, setTrend] = useState<MetricDatum[]>([])
  const [category, setCategory] = useState<MetricDatum[]>([])
  const [rotation, setRotation] = useState<MetricDatum[]>([])
  const [alerts, setAlerts] = useState<Variant[]>([])
  const [, setStatus] = useState('Cargando reportes desde backend.')
  const [downloading, setDownloading] = useState(false)

  const activeRange = useMemo(() => {
    if (rangeMode === 'manual') return { start: manualStart, end: manualEnd }
    return guidedRange === 'week' ? weekBounds(weekDate) : monthBounds(monthDate)
  }, [guidedRange, manualEnd, manualStart, monthDate, rangeMode, weekDate])

  const hasInvalidRange = activeRange.start > activeRange.end
  const reportFilters: ReportFilters = useMemo(() => ({
    desde: toIsoStart(activeRange.start),
    hasta: toIsoEnd(activeRange.end),
  }), [activeRange.end, activeRange.start])

  useEffect(() => {
    if (hasInvalidRange) {
      Promise.resolve().then(() => {
        setSummary(emptySummary)
        setTrend([])
        setCategory([])
        setRotation([])
        setAlerts([])
        setStatus('La fecha de inicio no puede ser posterior a la fecha final.')
      })
      return
    }

    Promise.all([
      reportsApi.getSummary(reportFilters),
      reportsApi.getSalesTrend(reportFilters),
      reportsApi.getSalesByCategory(reportFilters),
      reportsApi.getRotation(reportFilters),
      reportsApi.getStockAlerts(),
    ])
      .then(([summaryData, trendResponse, categoryResponse, rotationData, alertData]) => {
        setSummary(summaryData)
        setTrend(trendResponse)
        setCategory(categoryResponse)
        setRotation(rotationData)
        setAlerts(alertData)
        setStatus('Datos cargados.')
      })
      .catch(() => {
        setSummary(emptySummary)
        setTrend([])
        setCategory([])
        setRotation([])
        setAlerts([])
        setStatus('Backend no disponible. No se muestran datos mock.')
      })
  }, [hasInvalidRange, reportFilters])

  async function handleDownload(format: 'csv' | 'pdf') {
    if (hasInvalidRange) {
      setStatus('Corrige el rango de fechas antes de descargar.')
      return
    }
    setDownloading(true)
    try {
      const file = await reportsApi.downloadReport(downloadType, format, reportFilters)
      saveBlob(file.blob, file.filename)
      setStatus(`Reporte ${format.toUpperCase()} generado.`)
    } catch {
      setStatus(`No se pudo descargar el reporte ${format.toUpperCase()}.`)
    } finally {
      setDownloading(false)
    }
  }

  return (
    <div className="page-grid">
      <section className="flex min-w-0 justify-end">
        <div className="grid w-full min-w-0 gap-2 rounded-[var(--radius-lg)] border border-[var(--color-border)] bg-[var(--color-surface)] p-3 shadow-sm xl:w-auto xl:min-w-[720px]">
          <div className="grid min-w-0 grid-cols-1 items-end gap-2 sm:grid-cols-2 lg:flex lg:flex-wrap">
            <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
              Modo
              <select value={rangeMode} onChange={(event) => setRangeMode(event.target.value as RangeMode)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]">
                <option value="manual">Rango manual</option>
                <option value="guided">Semana o mes</option>
              </select>
            </label>

            {rangeMode === 'manual' ? (
              <>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
                  Desde
                  <input type="date" value={manualStart} onChange={(event) => setManualStart(event.target.value)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]" />
                </label>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
                  Hasta
                  <input type="date" value={manualEnd} onChange={(event) => setManualEnd(event.target.value)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]" />
                </label>
              </>
            ) : (
              <>
                <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
                  Corte
                  <select value={guidedRange} onChange={(event) => setGuidedRange(event.target.value as GuidedRange)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]">
                    <option value="week">Semana</option>
                    <option value="month">Mes</option>
                  </select>
                </label>
                {guidedRange === 'week' ? (
                  <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
                    Fecha base
                    <input type="date" value={weekDate} onChange={(event) => setWeekDate(event.target.value)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]" />
                  </label>
                ) : (
                  <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
                    Mes
                    <input type="month" value={monthDate} onChange={(event) => setMonthDate(event.target.value)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]" />
                  </label>
                )}
              </>
            )}

            <label className="grid gap-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
              Reporte
              <select value={downloadType} onChange={(event) => setDownloadType(event.target.value as DownloadReport)} className="min-h-10 rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]">
                {reportOptions.map((option) => (
                  <option key={option.value} value={option.value}>{option.label}</option>
                ))}
              </select>
            </label>

            <div className="grid grid-cols-2 gap-2 sm:col-span-2 lg:flex">
              <ActionButton type="button" variant="secondary" disabled={downloading || hasInvalidRange} onClick={() => handleDownload('csv')}>
                <Download size={16} />
                CSV
              </ActionButton>
              <ActionButton type="button" disabled={downloading || hasInvalidRange} onClick={() => handleDownload('pdf')}>
                <FileText size={16} />
                PDF
              </ActionButton>
            </div>
          </div>

          <div className="flex flex-wrap gap-2 text-xs font-semibold text-[var(--color-muted)]">
            <span className="inline-flex items-center gap-1 rounded-[var(--radius-md)] bg-[var(--color-bg)] px-2 py-1"><CalendarRange size={14} /> {rangeLabel(activeRange.start, activeRange.end)}</span>
            <span className="inline-flex items-center gap-1 rounded-[var(--radius-md)] bg-[var(--color-bg)] px-2 py-1"><CalendarDays size={14} /> {guidedRange === 'week' ? 'Analisis semanal' : 'Analisis mensual'}</span>
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-4">
        <KpiCard label="Ventas periodo" value={formatCurrency(summary.ventasMes)} icon={CircleDollarSign} />
        <KpiCard label="Crecimiento" value={`${summary.crecimientoPorcentaje.toFixed(2)}%`} icon={TrendingUp} tone="success" />
        <KpiCard label="Unidades vendidas" value={formatUnits(summary.unidadesVendidas)} icon={ShoppingBag} tone="primary" />
        <KpiCard label="Ticket promedio" value={formatCurrency(summary.ticketPromedio)} icon={BarChart3} tone="success" />
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <KpiCard label="Categoria principal" value={summary.categoriaPrincipal || 'Sin datos'} icon={Tags} tone="primary" />
        <KpiCard label="Producto mas vendido" value={summary.productoMasVendido || 'Sin datos'} icon={BarChart3} />
        <KpiCard label="SKUs con alerta" value={String(summary.skusConAlerta)} icon={PackageMinus} tone="warning" />
      </section>

      <section className="grid gap-4 xl:grid-cols-3">
        <ChartCard title="Rendimiento de ventas" subtitle="Ingresos del periodo">
          <LineTrendChart data={trend} valueFormatter={formatCompactCurrency} />
        </ChartCard>
        <ChartCard title="Ventas por categoria" subtitle="Participacion del periodo">
          <DonutMetricChart data={category} valueFormatter={formatCompactCurrency} />
        </ChartCard>
        <ChartCard title="Rotacion de inventario" subtitle="Unidades vendidas">
          <BarMetricChart data={rotation} valueFormatter={formatUnits} />
        </ChartCard>
      </section>

      <DataTable rows={alerts} columns={columns} emptyText="Sin alertas de stock" maxHeight="420px" />
    </div>
  )
}

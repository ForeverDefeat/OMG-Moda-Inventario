import type { Variant } from '../../features/catalog/domain/types'
import type { MetricDatum, ReportFilters, ReportSummary } from '../../features/reports/domain/types'
import { apiRequest, downloadRequest } from './httpClient'

type ReportPeriod = 'today' | '7d' | '30d' | '120d'
type ReportDownload = 'resumen' | 'rotacion'

function query(filters: { [key: string]: string | number | undefined }) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== '') params.set(key, String(value))
  })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const reportsApi = {
  getSummary(filters: ReportFilters = {}) {
    return apiRequest<ReportSummary>(`/reportes/resumen${query({ ...filters })}`)
  },

  getSalesTrend(filters: ReportFilters = { periodo: '30d' }) {
    return apiRequest<MetricDatum[]>(`/reportes/ventas-tendencia${query({ ...filters })}`)
  },

  getSalesByCategory(filters: ReportFilters = {}) {
    return apiRequest<MetricDatum[]>(`/reportes/ventas-categoria${query({ ...filters })}`)
  },

  getRotation(filters: { categoria?: string; meses?: number; periodo?: ReportPeriod; desde?: string; hasta?: string } = {}) {
    return apiRequest<MetricDatum[]>(`/reportes/rotacion${query(filters)}`)
  },

  getStockAlerts() {
    return apiRequest<Variant[]>('/reportes/stock-alertas')
  },

  downloadReport(tipo: ReportDownload, formato: 'csv' | 'pdf', filters: ReportFilters = { periodo: '30d' }) {
    return downloadRequest(`/reportes/export/${tipo}.${formato}${query({ ...filters })}`)
  },
}

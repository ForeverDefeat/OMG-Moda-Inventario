import type { PurchaseSuggestion } from '../../purchase-orders/domain/types'

export interface MetricDatum {
  [key: string]: string | number
  name: string
  value: number
}

export interface DashboardResponse {
  kpis: {
    valorStock: number
    skusActivos: number
    alertasStock: number
    ventasPeriodo: number
    comprasSugeridas: number
  }
  ventasTendencia: MetricDatum[]
  ventasCategoria: MetricDatum[]
  rotacion: MetricDatum[]
  sugerencias: PurchaseSuggestion[]
}

export interface DashboardFilters {
  periodo?: 'today' | '7d'
  canal?: 'all' | 'pos' | 'online'
  categoria?: string
}

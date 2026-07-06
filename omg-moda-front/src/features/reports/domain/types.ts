export interface MetricDatum {
  [key: string]: string | number
  name: string
  value: number
}

export interface ReportSummary {
  ventasMes: number
  crecimientoPorcentaje: number
  skusConAlerta: number
  reportesActivos: number
  unidadesVendidas: number
  ticketPromedio: number
  categoriaPrincipal: string
  productoMasVendido: string
}

export interface ReportFilters {
  desde?: string
  hasta?: string
  periodo?: 'today' | '7d' | '30d' | '120d'
}

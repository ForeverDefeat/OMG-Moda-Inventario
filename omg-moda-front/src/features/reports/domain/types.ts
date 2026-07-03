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
}

export interface ReportFilters {
  desde?: string
  hasta?: string
}

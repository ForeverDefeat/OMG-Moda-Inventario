export interface PurchaseSuggestion {
  id: number
  producto: string
  proveedor: string
  cantidadSugerida: number
  costoEstimado: number
  prioridad: 'Alta' | 'Media' | 'Baja'
  motivo: string
}

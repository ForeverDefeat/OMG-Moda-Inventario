export type SaleStatus = 'PENDIENTE' | 'COMPLETADA' | 'ANULADA'

export interface CreateSaleRequest {
  metodoPago: string
  items: Array<{
    idVariante: number
    cantidad: number
    precioUnitario: number
  }>
}

export interface SaleResponse {
  idVenta: number
  idUsuario: number
  estado: SaleStatus
  metodoPago: string
  fecha: string
  detalles: Array<{
    idDetalle: number
    idVariante: number
    cantidad: number
    precioUnitario: number
    subtotal: number
  }>
  total: number
}

export interface SaleFilters {
  estado?: SaleStatus
  desde?: string
  hasta?: string
}

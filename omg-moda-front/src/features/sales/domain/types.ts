export type SaleStatus = 'PENDING_PAYMENT' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED' | 'PENDIENTE' | 'COMPLETADA' | 'ANULADA'
export type PaymentStatus = 'PENDING' | 'CONFIRMED' | 'MANUALLY_CONFIRMED' | 'EXPIRED' | 'CANCELLED' | 'FAILED'
export type PaymentMethod = 'EFECTIVO' | 'TARJETA' | 'YAPE' | 'PLIN' | 'CARD'

export interface CreateSaleRequest {
  metodoPago: PaymentMethod
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
  payment?: PaymentIntent
  trazabilidad?: PaymentAudit[]
}

export interface PaymentIntent {
  idPayment: number
  idVenta: number
  method: PaymentMethod
  provider: string
  providerReference: string
  amountDue: number
  amountReceived: number
  changeAmount: number
  currency: 'PEN'
  status: PaymentStatus
  paymentReference: string
  expiresAt: string
  createdAt: string
  confirmedAt?: string
}

export interface PaymentAudit {
  eventType: string
  previousStatus?: string
  newStatus?: string
  userId?: number
  userRole?: string
  reference?: string
  observation?: string
  createdAt: string
}

export interface SaleFilters {
  estado?: SaleStatus
  desde?: string
  hasta?: string
}

export interface PaymentListItem {
  idPayment: number
  idVenta: number
  idUsuario: number
  vendedorNombre: string
  vendedorCorreo: string
  method: PaymentMethod
  providerReference: string
  amountDue: number
  amountReceived: number
  currency: 'PEN'
  status: PaymentStatus
  estadoVenta: SaleStatus
  createdAt: string
  expiresAt: string
  confirmedAt?: string
}

export interface PaymentDetail {
  payment: PaymentIntent
  venta: SaleResponse
  vendedorNombre: string
  vendedorCorreo: string
  productos: Array<{
    idDetalle: number
    idVariante: number
    sku: string
    producto: string
    categoria: string
    color: string
    talla: string
    cantidad: number
    precioUnitario: number
    subtotal: number
  }>
  reservas: Array<{
    idReserva: number
    idVariante: number
    cantidad: number
    estado: 'ACTIVE' | 'CONSUMED' | 'RELEASED' | 'EXPIRED'
    createdAt: string
    expiresAt: string
    releasedAt?: string
  }>
}

export interface PaymentFilters {
  status?: PaymentStatus
  method?: PaymentMethod
  desde?: string
  hasta?: string
  search?: string
}

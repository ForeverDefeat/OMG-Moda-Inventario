export type StockStatus = 'NORMAL' | 'LOW' | 'OUT' | 'BAJO_STOCK' | 'SIN_STOCK'

export interface Variant {
  idVariante: number
  idProducto: number
  sku: string
  nombreProducto: string
  categoria: string
  marca: string
  talla: string
  color: string
  material?: string | null
  precioCosto: number
  precioVenta: number
  stockActual: number
  stockReservado?: number
  stockDisponible?: number
  stockMinimo: number
  stockStatus: StockStatus
  imageUrl?: string
}

export interface CreateProductRequest {
  nombre: string
  categoria: string
  marca: string
  imageUrl?: string
  variantes: Array<{
    sku?: string
    talla: string
    color: string
    material?: string
    precioCosto: number
    precioVenta: number
  }>
}

export interface UpdateProductRequest {
  nombre: string
  imageUrl?: string
}

export interface VariantFilters {
  talla?: string
  color?: string
  categoria?: string
  sku?: string
}

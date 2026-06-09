export type MovementType = 'ENTRADA' | 'SALIDA' | 'AJUSTE'

export interface RegisterEntryRequest {
  idVariante: number
  cantidad: number
  motivo?: string
}

export interface RegisterAdjustmentRequest {
  idVariante: number
  cantidad: number
  motivo: string
}

export interface MovementResponse {
  idMovimiento: number
  idVariante: number
  idUsuario: number
  tipo: MovementType
  cantidad: number
  motivo?: string
  fecha: string
  stockResultante: number
}

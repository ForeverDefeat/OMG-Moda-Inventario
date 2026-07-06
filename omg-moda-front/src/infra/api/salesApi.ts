import type { CreateSaleRequest, PaymentIntent, SaleFilters, SaleResponse } from '../../features/sales/domain/types'
import { apiRequest } from './httpClient'

function query(filters: SaleFilters) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const salesApi = {
  createSale(payload: CreateSaleRequest, idempotencyKey?: string) {
    return apiRequest<SaleResponse>('/ventas', {
      method: 'POST',
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : undefined,
      body: JSON.stringify(payload),
    })
  },

  listSales(filters: SaleFilters = {}) {
    return apiRequest<SaleResponse[]>(`/ventas${query(filters)}`)
  },

  getSale(id: number) {
    return apiRequest<SaleResponse>(`/ventas/${id}`)
  },

  getPayment(id: number) {
    return apiRequest<PaymentIntent>(`/pagos/${id}`)
  },

  confirmCash(id: number, payload: { amountReceived: number; reference?: string; observation?: string }) {
    return apiRequest<SaleResponse>(`/pagos/${id}/confirmar-efectivo`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  confirmManual(id: number, payload: { amountReceived: number; currency: 'PEN'; reference: string; observation: string }) {
    return apiRequest<SaleResponse>(`/pagos/${id}/confirmar-manual`, {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  cancelPayment(id: number) {
    return apiRequest<SaleResponse>(`/pagos/${id}/cancelar`, { method: 'POST' })
  },
}

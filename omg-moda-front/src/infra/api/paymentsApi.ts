import type { PaymentDetail, PaymentFilters, PaymentIntent, PaymentListItem, SaleResponse } from '../../features/sales/domain/types'
import { apiRequest } from './httpClient'

function query(filters: PaymentFilters) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const paymentsApi = {
  list(filters: PaymentFilters = {}) {
    return apiRequest<PaymentListItem[]>(`/pagos${query(filters)}`)
  },

  get(id: number) {
    return apiRequest<PaymentIntent>(`/pagos/${id}`)
  },

  detail(id: number) {
    return apiRequest<PaymentDetail>(`/pagos/${id}/detalle`)
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

  cancel(id: number) {
    return apiRequest<SaleResponse>(`/pagos/${id}/cancelar`, { method: 'POST' })
  },
}

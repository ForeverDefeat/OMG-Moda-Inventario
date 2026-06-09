import type { CreateSaleRequest, SaleFilters, SaleResponse } from '../../features/sales/domain/types'
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
  createSale(payload: CreateSaleRequest) {
    return apiRequest<SaleResponse>('/ventas', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  listSales(filters: SaleFilters = {}) {
    return apiRequest<SaleResponse[]>(`/ventas${query(filters)}`)
  },
}

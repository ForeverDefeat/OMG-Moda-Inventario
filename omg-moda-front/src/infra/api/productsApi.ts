import type { CreateProductRequest, Variant, VariantFilters } from '../../features/catalog/domain/types'
import { apiRequest } from './httpClient'

function query(filters: VariantFilters) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const productsApi = {
  listVariants(filters: VariantFilters = {}) {
    return apiRequest<Variant[]>(`/productos/variantes${query(filters)}`)
  },

  createProduct(payload: CreateProductRequest) {
    return apiRequest<Variant[]>('/productos', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
}

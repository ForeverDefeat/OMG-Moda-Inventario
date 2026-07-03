import type { PurchaseSuggestion } from '../../features/purchase-orders/domain/types'
import { apiRequest } from './httpClient'

export const purchaseApi = {
  listSuggestions() {
    return apiRequest<PurchaseSuggestion[]>('/compras/sugerencias')
  },
}

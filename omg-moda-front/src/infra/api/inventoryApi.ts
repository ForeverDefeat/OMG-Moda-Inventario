import type { MovementResponse, RegisterAdjustmentRequest, RegisterEntryRequest } from '../../features/inventory/domain/types'
import { apiRequest } from './httpClient'

export const inventoryApi = {
  registerEntry(payload: RegisterEntryRequest) {
    return apiRequest<MovementResponse>('/movimientos/entrada', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },

  registerAdjustment(payload: RegisterAdjustmentRequest) {
    return apiRequest<MovementResponse>('/movimientos/ajuste', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
}

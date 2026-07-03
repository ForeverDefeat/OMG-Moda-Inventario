import type { DashboardFilters, DashboardResponse } from '../../features/dashboard/domain/types'
import { apiRequest } from './httpClient'

function query(filters: DashboardFilters) {
  const params = new URLSearchParams()
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value)
  })
  const value = params.toString()
  return value ? `?${value}` : ''
}

export const dashboardApi = {
  getDashboard(filters: DashboardFilters = {}) {
    return apiRequest<DashboardResponse>(`/dashboard${query(filters)}`)
  },
}

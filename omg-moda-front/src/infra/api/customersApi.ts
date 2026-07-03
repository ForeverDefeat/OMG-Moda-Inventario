import type { CreateCustomerRequest, Customer } from '../../features/customers/domain/types'
import { apiRequest } from './httpClient'

export const customersApi = {
  listCustomers() {
    return apiRequest<Customer[]>('/clientes')
  },

  createCustomer(payload: CreateCustomerRequest) {
    return apiRequest<Customer>('/clientes', {
      method: 'POST',
      body: JSON.stringify(payload),
    })
  },
}

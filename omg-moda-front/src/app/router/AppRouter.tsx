import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from '../layout/AppShell'
import { ProtectedRoute } from './ProtectedRoute'
import { LoginPage } from '../../features/auth/ui/LoginPage'
import { CatalogPage } from '../../features/catalog/ui/CatalogPage'
import { CustomersPage } from '../../features/customers/ui/CustomersPage'
import { DashboardPage } from '../../features/dashboard/ui/DashboardPage'
import { InventoryPage } from '../../features/inventory/ui/InventoryPage'
import { PaymentsPage } from '../../features/payments/ui/PaymentsPage'
import { PurchaseOrdersPage } from '../../features/purchase-orders/ui/PurchaseOrdersPage'
import { ReportsPage } from '../../features/reports/ui/ReportsPage'
import { SalesPosPage } from '../../features/sales/ui/SalesPosPage'
import { UsersPage } from '../../features/users/ui/UsersPage'

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/catalogo" element={<CatalogPage />} />
          <Route path="/stock" element={<InventoryPage />} />
          <Route path="/ventas" element={<SalesPosPage />} />
          <Route path="/pagos" element={<PaymentsPage />} />
          <Route path="/clientes" element={<CustomersPage />} />
          <Route path="/compras" element={<PurchaseOrdersPage />} />
          <Route path="/reportes" element={<ReportsPage />} />
          <Route path="/usuarios" element={<UsersPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

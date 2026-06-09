import { Mail, Phone, ShoppingBag, UserPlus, Users } from 'lucide-react'
import type { Customer } from '../domain/types'
import { mockCustomers } from '../../../infra/mock/mockData'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'

const columns: Column<Customer>[] = [
  { key: 'cliente', header: 'Cliente', render: (row) => <span className="font-semibold">{row.nombre}</span> },
  { key: 'correo', header: 'Correo', render: (row) => row.correo },
  { key: 'telefono', header: 'Telefono', render: (row) => row.telefono },
  { key: 'segmento', header: 'Segmento', render: (row) => <StatusBadge status={row.segmento} /> },
  { key: 'valor', header: 'Valor', render: (row) => `S/ ${row.totalCompras.toFixed(2)}` },
]

export function CustomersPage() {
  const total = mockCustomers.reduce((sum, customer) => sum + customer.totalCompras, 0)

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Clientes</h1>
          <p className="text-sm text-[var(--color-muted)]">Gestiona perfiles, historiales de compra y segmentos.</p>
        </div>
        <ActionButton><UserPlus size={17} /> Anadir cliente</ActionButton>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard label="Total de clientes" value={String(mockCustomers.length)} icon={Users} />
        <KpiCard label="Valor del cliente" value={`S/ ${total.toLocaleString('es-PE')}`} icon={Mail} tone="success" />
        <KpiCard label="Contactos activos" value="92%" icon={Phone} tone="primary" />
        <KpiCard label="Compras recurrentes" value="68%" icon={ShoppingBag} tone="warning" />
      </section>
      <section className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
        <div>
          <h2 className="text-lg font-bold">Todos los clientes</h2>
          <p className="text-sm text-[var(--color-muted)]">Modulo mock preparado para reemplazo por API.</p>
        </div>
      </section>
      <DataTable rows={mockCustomers} columns={columns} />
    </div>
  )
}

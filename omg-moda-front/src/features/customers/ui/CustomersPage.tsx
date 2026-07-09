import { useEffect, useState, type FormEvent } from 'react'
import { Mail, Phone, ShoppingBag, UserPlus, Users } from 'lucide-react'
import type { CreateCustomerRequest, Customer } from '../domain/types'
import { customersApi } from '../../../infra/api/customersApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { StatusBadge } from '../../../shared/components/Badge'
import { Modal } from '../../../shared/components/Modal'
import { useAuth } from '../../auth/application/useAuth'

const columns: Column<Customer>[] = [
  { key: 'cliente', header: 'Cliente', render: (row) => <span className="font-semibold">{row.nombre}</span>, sortable: true, sortValue: (row) => row.nombre },
  { key: 'correo', header: 'Correo', render: (row) => row.correo, sortable: true, sortValue: (row) => row.correo },
  { key: 'telefono', header: 'Telefono', render: (row) => row.telefono, sortable: true, sortValue: (row) => row.telefono },
  { key: 'segmento', header: 'Segmento', render: (row) => <StatusBadge status={row.segmento} />, sortable: true, sortValue: (row) => row.segmento },
  { key: 'valor', header: 'Valor', render: (row) => `S/ ${row.totalCompras.toFixed(2)}`, sortable: true, sortValue: (row) => row.totalCompras, sortType: 'number' },
]

export function CustomersPage() {
  const { session } = useAuth()
  const [customers, setCustomers] = useState<Customer[]>([])
  const [modalOpen, setModalOpen] = useState(false)
  const canManageCustomers = session?.rol === 'ADMIN'
  const total = customers.reduce((sum, customer) => sum + customer.totalCompras, 0)
  const recurrent = customers.length
    ? Math.round((customers.filter((customer) => customer.segmento !== 'Nuevo').length / customers.length) * 100)
    : 0

  useEffect(() => {
    customersApi.listCustomers()
      .then((data) => {
        setCustomers(data)
      })
      .catch(() => {
        setCustomers([])
      })
  }, [])

  async function submitCustomer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const payload: CreateCustomerRequest = {
      nombre: String(form.get('nombre') ?? ''),
      correo: String(form.get('correo') ?? ''),
      telefono: String(form.get('telefono') ?? ''),
      segmento: String(form.get('segmento') ?? 'Nuevo') as Customer['segmento'],
      totalCompras: Number(form.get('totalCompras') ?? 0),
      ultimaCompra: String(form.get('ultimaCompra') ?? '') || new Date().toISOString().slice(0, 10),
    }

    try {
      const created = await customersApi.createCustomer(payload)
      setCustomers((current) => [...current, created].sort((a, b) => a.nombre.localeCompare(b.nombre)))
      setModalOpen(false)
      event.currentTarget.reset()
    } catch {
      return
    }
  }

  return (
    <div className="page-grid">
      {canManageCustomers && (
        <section className="flex justify-end">
          <ActionButton onClick={() => setModalOpen(true)}><UserPlus size={17} /> Anadir cliente</ActionButton>
        </section>
      )}

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard label="Total de clientes" value={String(customers.length)} icon={Users} />
        <KpiCard label="Valor del cliente" value={`S/ ${total.toLocaleString('es-PE', { maximumFractionDigits: 2 })}`} icon={Mail} tone="success" />
        <KpiCard label="Contactos activos" value={customers.length ? '100%' : '0%'} icon={Phone} tone="primary" />
        <KpiCard label="Compras recurrentes" value={`${recurrent}%`} icon={ShoppingBag} tone="warning" />
      </section>

      <section className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
        <div>
          <h2 className="text-lg font-bold">Todos los clientes</h2>
          <p className="text-sm text-[var(--color-muted)]">Informacion cargada desde la API de clientes.</p>
        </div>
      </section>
      <DataTable rows={customers} columns={columns} emptyText="Sin clientes registrados" maxHeight="480px" />

      {canManageCustomers && (
      <Modal open={modalOpen} title="Nuevo cliente" onClose={() => setModalOpen(false)}>
        <form className="grid gap-4" onSubmit={submitCustomer}>
          <label className="grid gap-1 text-sm font-semibold">
            Nombre
            <input name="nombre" required maxLength={100} className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 outline-none" />
          </label>
          <label className="grid gap-1 text-sm font-semibold">
            Correo
            <input name="correo" required type="email" maxLength={100} className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 outline-none" />
          </label>
          <label className="grid gap-1 text-sm font-semibold">
            Telefono
            <input name="telefono" required maxLength={20} className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 outline-none" />
          </label>
          <div className="grid gap-4 sm:grid-cols-3">
            <label className="grid gap-1 text-sm font-semibold">
              Segmento
              <select name="segmento" defaultValue="Nuevo" className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 outline-none">
                <option value="Nuevo">Nuevo</option>
                <option value="Frecuente">Frecuente</option>
                <option value="VIP">VIP</option>
              </select>
            </label>
            <label className="grid gap-1 text-sm font-semibold">
              Total compras
              <input name="totalCompras" type="number" min="0" step="0.01" defaultValue="0" className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 outline-none" />
            </label>
            <label className="grid gap-1 text-sm font-semibold">
              Ultima compra
              <input name="ultimaCompra" type="date" className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 outline-none" />
            </label>
          </div>
          <div className="flex justify-end gap-2">
            <ActionButton type="button" variant="secondary" onClick={() => setModalOpen(false)}>Cancelar</ActionButton>
            <ActionButton type="submit">Guardar cliente</ActionButton>
          </div>
        </form>
      </Modal>
      )}
    </div>
  )
}

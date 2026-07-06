import { useEffect, useMemo, useState } from 'react'
import { Badge, StatusBadge } from '../../../shared/components/Badge'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { Drawer } from '../../../shared/components/Drawer'
import { KpiCard } from '../../../shared/components/KpiCard'
import { paymentsApi } from '../../../infra/api/paymentsApi'
import { useAuth } from '../../auth/application/useAuth'
import type { PaymentDetail, PaymentFilters, PaymentListItem, PaymentMethod, PaymentStatus } from '../../sales/domain/types'
import { Banknote, Clock3, CreditCard, RefreshCcw, ShieldCheck } from 'lucide-react'

const paymentStatuses: PaymentStatus[] = ['PENDING', 'MANUALLY_CONFIRMED', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'FAILED']
const paymentMethods: PaymentMethod[] = ['EFECTIVO', 'YAPE', 'PLIN', 'CARD']

const currency = new Intl.NumberFormat('es-PE', {
  style: 'currency',
  currency: 'PEN',
})

function formatMoney(value: number) {
  return currency.format(value ?? 0)
}

function formatDate(value?: string) {
  if (!value) return 'No disponible'
  return new Date(value).toLocaleString('es-PE')
}

function isPending(payment?: PaymentListItem | PaymentDetail['payment']) {
  return payment?.status === 'PENDING'
}

function isDigital(method?: PaymentMethod) {
  return method === 'YAPE' || method === 'PLIN' || method === 'CARD' || method === 'TARJETA'
}

export function PaymentsPage() {
  const { session } = useAuth()
  const isAdmin = session?.rol === 'ADMIN'
  const [filters, setFilters] = useState<PaymentFilters>({ status: 'PENDING' })
  const [payments, setPayments] = useState<PaymentListItem[]>([])
  const [selected, setSelected] = useState<PaymentDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [detailLoading, setDetailLoading] = useState(false)
  const [message, setMessage] = useState('Cargando bandeja de pagos.')
  const [cashReceived, setCashReceived] = useState('')
  const [manualReference, setManualReference] = useState('')
  const [manualObservation, setManualObservation] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadPayments()
  }, [])

  const pendingCount = payments.filter((payment) => payment.status === 'PENDING').length
  const totalPending = payments
    .filter((payment) => payment.status === 'PENDING')
    .reduce((sum, payment) => sum + payment.amountDue, 0)
  const expiredCount = payments.filter((payment) => payment.status === 'EXPIRED').length

  const columns: Column<PaymentListItem>[] = useMemo(() => [
    { key: 'idVenta', header: 'Venta', render: (row) => `#${row.idVenta}`, sortable: true, sortType: 'number', sortValue: (row) => row.idVenta },
    {
      key: 'estado',
      header: 'Estado',
      render: (row) => <StatusBadge status={row.status === 'PENDING' ? 'Pendiente' : row.status} />,
      sortable: true,
      sortValue: (row) => row.status,
    },
    { key: 'method', header: 'Metodo', render: (row) => <Badge tone={row.method === 'EFECTIVO' ? 'neutral' : 'primary'}>{row.method}</Badge>, sortable: true, sortValue: (row) => row.method },
    { key: 'total', header: 'Monto', render: (row) => formatMoney(row.amountDue), sortable: true, sortType: 'number', sortValue: (row) => row.amountDue },
    { key: 'vendedor', header: 'Vendedor', render: (row) => row.vendedorNombre, sortable: true, sortValue: (row) => row.vendedorNombre },
    { key: 'fecha', header: 'Creado', render: (row) => formatDate(row.createdAt), sortable: true, sortValue: (row) => new Date(row.createdAt).getTime(), sortType: 'number' },
    {
      key: 'acciones',
      header: 'Acciones',
      render: (row) => (
        <ActionButton variant="secondary" onClick={() => openDetail(row.idPayment)}>
          Ver detalle
        </ActionButton>
      ),
    },
  ], [])

  async function loadPayments(nextFilters = filters) {
    setLoading(true)
    try {
      const data = await paymentsApi.list(nextFilters)
      setPayments(data)
      setMessage(data.length ? 'Pagos cargados. Verifica antes de entregar productos.' : 'No hay pagos con los filtros actuales.')
    } catch (error) {
      setPayments([])
      setMessage(error instanceof Error ? error.message : 'No se pudieron cargar los pagos.')
    } finally {
      setLoading(false)
    }
  }

  function updateFilter<K extends keyof PaymentFilters>(key: K, value: PaymentFilters[K] | '') {
    setFilters((current) => ({
      ...current,
      [key]: value || undefined,
    }))
  }

  async function applyFilters() {
    await loadPayments(filters)
  }

  async function openDetail(idPayment: number) {
    setDetailLoading(true)
    try {
      const detail = await paymentsApi.detail(idPayment)
      setSelected(detail)
      setCashReceived(detail.payment.amountDue.toFixed(2))
      setManualReference('')
      setManualObservation('')
      setMessage('Detalle actualizado.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo cargar el detalle del pago.')
    } finally {
      setDetailLoading(false)
    }
  }

  async function refreshSelected() {
    if (!selected) return
    await openDetail(selected.payment.idPayment)
    await loadPayments(filters)
  }

  async function cancelPayment() {
    if (!selected || !isPending(selected.payment)) return
    setSaving(true)
    try {
      await paymentsApi.cancel(selected.payment.idPayment)
      setSelected(null)
      await loadPayments(filters)
      setMessage('Pago cancelado. La reserva fue liberada.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo cancelar el pago.')
    } finally {
      setSaving(false)
    }
  }

  async function confirmCash() {
    if (!selected || selected.payment.method !== 'EFECTIVO') return
    setSaving(true)
    try {
      await paymentsApi.confirmCash(selected.payment.idPayment, {
        amountReceived: Number(cashReceived),
        reference: 'CAJA',
        observation: 'Confirmado desde bandeja de pagos',
      })
      setSelected(null)
      await loadPayments(filters)
      setMessage('Pago en efectivo confirmado correctamente.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo confirmar el efectivo.')
    } finally {
      setSaving(false)
    }
  }

  async function confirmManual() {
    if (!selected || !isAdmin || !isDigital(selected.payment.method)) return
    setSaving(true)
    try {
      await paymentsApi.confirmManual(selected.payment.idPayment, {
        amountReceived: selected.payment.amountDue,
        currency: 'PEN',
        reference: manualReference,
        observation: manualObservation,
      })
      setSelected(null)
      await loadPayments(filters)
      setMessage('Pago digital confirmado manualmente.')
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo confirmar manualmente el pago.')
    } finally {
      setSaving(false)
    }
  }

  const actionPanel = selected && isPending(selected.payment) ? (
    <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-4">
      <div className="mb-3 flex items-center gap-2">
        <CreditCard size={17} />
        <h3 className="font-bold">Acciones de pago</h3>
      </div>
      {selected.payment.method === 'EFECTIVO' && (
        <div className="grid gap-3">
          <label className="block">
            <span className="text-sm font-semibold">Monto recibido</span>
            <input
              type="number"
              min="0"
              step="0.01"
              value={cashReceived}
              onChange={(event) => setCashReceived(event.target.value)}
              className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
            />
          </label>
          <ActionButton onClick={confirmCash} disabled={saving || Number(cashReceived) < selected.payment.amountDue}>
            Confirmar efectivo
          </ActionButton>
        </div>
      )}
      {isDigital(selected.payment.method) && isAdmin && (
        <div className="grid gap-3">
          <p className="text-xs text-[var(--color-muted)]">Confirma solo despues de validar el comprobante real fuera del sistema.</p>
          <input
            value={manualReference}
            onChange={(event) => setManualReference(event.target.value)}
            placeholder="Referencia de operacion"
            className="w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
          />
          <textarea
            value={manualObservation}
            onChange={(event) => setManualObservation(event.target.value)}
            placeholder="Observacion de confirmacion"
            className="min-h-24 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
          />
          <ActionButton onClick={confirmManual} disabled={saving || !manualReference.trim() || !manualObservation.trim()}>
            Confirmar manualmente
          </ActionButton>
        </div>
      )}
      {isDigital(selected.payment.method) && !isAdmin && (
        <p className="text-sm text-[var(--color-muted)]">Solo ADMIN puede confirmar manualmente pagos digitales.</p>
      )}
      <div className="mt-3 grid grid-cols-2 gap-2">
        <ActionButton variant="secondary" onClick={refreshSelected} disabled={saving}>Verificar estado</ActionButton>
        <ActionButton variant="danger" onClick={cancelPayment} disabled={saving}>Cancelar</ActionButton>
      </div>
    </div>
  ) : null

  return (
    <div className="grid gap-6">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Pagos pendientes</h1>
          <p className="text-sm text-[var(--color-muted)]">
            {isAdmin ? 'Bandeja central para revisar cobros, validar comprobantes y liberar reservas.' : 'Consulta tus cobros pendientes antes de entregar productos.'}
          </p>
        </div>
        <ActionButton variant="secondary" onClick={() => loadPayments(filters)} disabled={loading}>
          <RefreshCcw size={16} /> Actualizar
        </ActionButton>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <KpiCard label="Pagos pendientes" value={String(pendingCount)} icon={Clock3} tone="warning" helper="No entregues productos sin confirmacion" />
        <KpiCard label="Monto pendiente" value={formatMoney(totalPending)} icon={Banknote} tone="primary" />
        <KpiCard label="Expirados en vista" value={String(expiredCount)} icon={ShieldCheck} tone={expiredCount ? 'danger' : 'success'} />
      </section>

      <section className="dashboard-card p-4">
        <div className="grid gap-3 md:grid-cols-[1fr_160px_160px_180px_180px_auto] md:items-end">
          <label className="block">
            <span className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Buscar</span>
            <input
              value={filters.search ?? ''}
              onChange={(event) => updateFilter('search', event.target.value)}
              placeholder="Venta, referencia o vendedor"
              className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
            />
          </label>
          <label className="block">
            <span className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Estado</span>
            <select
              value={filters.status ?? ''}
              onChange={(event) => updateFilter('status', event.target.value as PaymentStatus | '')}
              className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm"
            >
              <option value="">Todos</option>
              {paymentStatuses.map((status) => <option key={status}>{status}</option>)}
            </select>
          </label>
          <label className="block">
            <span className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Metodo</span>
            <select
              value={filters.method ?? ''}
              onChange={(event) => updateFilter('method', event.target.value as PaymentMethod | '')}
              className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm"
            >
              <option value="">Todos</option>
              {paymentMethods.map((method) => <option key={method}>{method}</option>)}
            </select>
          </label>
          <label className="block">
            <span className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Desde</span>
            <input
              type="datetime-local"
              value={filters.desde ?? ''}
              onChange={(event) => updateFilter('desde', event.target.value)}
              className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
            />
          </label>
          <label className="block">
            <span className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Hasta</span>
            <input
              type="datetime-local"
              value={filters.hasta ?? ''}
              onChange={(event) => updateFilter('hasta', event.target.value)}
              className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
            />
          </label>
          <ActionButton onClick={applyFilters} disabled={loading}>Filtrar</ActionButton>
        </div>
        <p className="mt-3 text-sm font-semibold text-[var(--color-muted)]">{message}</p>
      </section>

      <DataTable
        rows={payments}
        columns={columns}
        emptyText={loading ? 'Cargando pagos...' : 'No hay pagos para mostrar'}
        maxHeight="520px"
      />

      <Drawer title={selected ? `Pago #${selected.payment.idPayment}` : 'Detalle de pago'} isOpen={Boolean(selected) || detailLoading} onClose={() => setSelected(null)} size="lg">
        {detailLoading && !selected && <p className="text-sm text-[var(--color-muted)]">Cargando detalle...</p>}
        {selected && (
          <div className="grid gap-4">
            <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Venta #{selected.venta.idVenta}</p>
                  <p className="mt-1 text-2xl font-bold">{formatMoney(selected.payment.amountDue)}</p>
                  <p className="text-sm text-[var(--color-muted)]">{selected.vendedorNombre}</p>
                </div>
                <StatusBadge status={selected.payment.status === 'PENDING' ? 'Pendiente' : selected.payment.status} />
              </div>
              <div className="mt-3 grid gap-2 text-sm">
                <InfoRow label="Metodo" value={selected.payment.method} />
                <InfoRow label="Referencia" value={selected.payment.providerReference} />
                <InfoRow label="Creado" value={formatDate(selected.payment.createdAt)} />
                <InfoRow label="Expira" value={formatDate(selected.payment.expiresAt)} />
              </div>
            </div>

            {actionPanel}

            <div className="rounded-xl border border-[var(--color-border)] p-4">
              <h3 className="font-bold">Productos</h3>
              <div className="mt-3 grid gap-2">
                {selected.productos.map((item) => (
                  <div key={item.idDetalle} className="rounded-lg bg-[var(--color-bg)] p-3 text-sm">
                    <div className="flex justify-between gap-3">
                      <div className="min-w-0">
                        <p className="truncate font-semibold">{item.producto}</p>
                        <p className="text-xs font-semibold text-[var(--color-muted)]">{item.sku} / {item.color} / {item.talla}</p>
                      </div>
                      <strong>{formatMoney(item.subtotal)}</strong>
                    </div>
                    <p className="mt-1 text-xs text-[var(--color-muted)]">{item.cantidad} und. x {formatMoney(item.precioUnitario)}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="rounded-xl border border-[var(--color-border)] p-4">
              <h3 className="font-bold">Trazabilidad</h3>
              <div className="mt-3 grid gap-2">
                {selected.venta.trazabilidad?.map((event, index) => (
                  <div key={`${event.eventType}-${index}`} className="flex gap-3 text-sm">
                    <span className="mt-1 size-2 shrink-0 rounded-full bg-[var(--color-primary)]" />
                    <div>
                      <p className="font-semibold">{event.eventType}</p>
                      <p className="text-xs text-[var(--color-muted)]">{formatDate(event.createdAt)} {event.userRole ? `/ ${event.userRole}` : ''}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </Drawer>
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-3">
      <span className="text-[var(--color-muted)]">{label}</span>
      <strong className="break-all text-right">{value}</strong>
    </div>
  )
}

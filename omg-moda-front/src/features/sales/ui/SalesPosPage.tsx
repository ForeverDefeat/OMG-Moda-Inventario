import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Minus, Plus, Receipt, ShoppingCart, Trash2 } from 'lucide-react'
import { useAuth } from '../../auth/application/useAuth'
import { groupVariantsByProduct, productGroupMatchesSearch } from '../../catalog/domain/productGroups'
import type { Variant } from '../../catalog/domain/types'
import type { PaymentListItem, PaymentMethod, SaleResponse } from '../domain/types'
import { productsApi } from '../../../infra/api/productsApi'
import { salesApi } from '../../../infra/api/salesApi'
import { paymentsApi } from '../../../infra/api/paymentsApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { Badge } from '../../../shared/components/Badge'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { IconButton } from '../../../shared/components/IconButton'
import { KpiCard } from '../../../shared/components/KpiCard'
import { ProductVariantCard } from '../../../shared/components/ProductVariantCard'
import { SearchInput } from '../../../shared/components/SearchInput'
import { cn } from '../../../shared/utils/cn'

interface CartItem {
  variant: Variant
  quantity: number
}

type GridColumns = 3 | 5

const salesColumns: Column<SaleResponse>[] = [
  { key: 'id', header: 'Venta', render: (row) => `#${row.idVenta}` },
  { key: 'fecha', header: 'Fecha', render: (row) => new Date(row.fecha).toLocaleString('es-PE') },
  { key: 'metodo', header: 'Metodo', render: (row) => row.metodoPago },
  { key: 'total', header: 'Total', render: (row) => `S/ ${row.total.toFixed(2)}` },
]

const rowsPerProductPage = 3
const recentSalesLimit = 5

export function SalesPosPage() {
  const [searchParams] = useSearchParams()
  const urlQuery = searchParams.get('q') ?? ''
  const [variants, setVariants] = useState<Variant[]>([])
  const [queryState, setQueryState] = useState(() => ({ source: urlQuery, value: urlQuery }))
  const [cart, setCart] = useState<CartItem[]>([])
  const [payment, setPayment] = useState<PaymentMethod>('EFECTIVO')
  const [pendingSale, setPendingSale] = useState<SaleResponse | null>(null)
  const [amountReceived, setAmountReceived] = useState('')
  const [manualReference, setManualReference] = useState('')
  const [manualObservation, setManualObservation] = useState('')
  const [message, setMessage] = useState('Cargando POS desde backend.')
  const [sales, setSales] = useState<SaleResponse[]>([])
  const [pendingPayments, setPendingPayments] = useState<PaymentListItem[]>([])
  const [productPage, setProductPage] = useState(1)
  const [gridColumns, setGridColumns] = useState<GridColumns>(5)
  const { session } = useAuth()
  const query = queryState.source === urlQuery ? queryState.value : urlQuery
  const activePayment = pendingSale?.payment
  const isAdmin = session?.rol === 'ADMIN'
  const isDigitalPayment = payment !== 'EFECTIVO'

  useEffect(() => {
    loadVariants()
    salesApi.listSales()
      .then((data) => setSales(data))
      .catch(() => setSales([]))
    loadPendingPayments()
  }, [])

  function loadVariants() {
    return productsApi.listVariants()
      .then((data) => {
        setVariants(data)
        setMessage(data.length ? 'POS listo para vender.' : 'Backend conectado sin productos disponibles.')
      })
      .catch(() => {
        setVariants([])
        setMessage('Backend no disponible. No se muestran productos mock.')
      })
  }

  function loadPendingPayments() {
    return paymentsApi.list({ status: 'PENDING' })
      .then((data) => setPendingPayments(data.slice(0, 3)))
      .catch(() => setPendingPayments([]))
  }

  const productGroups = useMemo(() => groupVariantsByProduct(variants), [variants])
  const filtered = useMemo(() => productGroups.filter((group) =>
    productGroupMatchesSearch(group, query),
  ), [productGroups, query])

  const total = cart.reduce((sum, item) => sum + item.variant.precioVenta * item.quantity, 0)
  const numericAmountReceived = Number(amountReceived)
  const changeAmount = Number.isFinite(numericAmountReceived) ? Math.max(numericAmountReceived - total, 0) : 0
  const ticketAverage = sales.length
    ? sales.reduce((sum, sale) => sum + sale.total, 0) / sales.length
    : 0
  const productPageSize = rowsPerProductPage * gridColumns
  const productTotalPages = Math.max(1, Math.ceil(filtered.length / productPageSize))
  const currentProductPage = Math.min(productPage, productTotalPages)
  const productStartIndex = (currentProductPage - 1) * productPageSize
  const paginatedProducts = filtered.slice(productStartIndex, productStartIndex + productPageSize)
  const productResultStart = filtered.length ? productStartIndex + 1 : 0
  const productResultEnd = Math.min(productStartIndex + productPageSize, filtered.length)

  function updateSearch(value: string) {
    setQueryState({ source: urlQuery, value })
    setProductPage(1)
  }

  function addToCart(variant: Variant) {
    const available = stockDisponible(variant)
    if (available <= 0) {
      setMessage('No hay stock disponible para esta variante.')
      return
    }

    setCart((current) => {
      const existing = current.find((item) => item.variant.idVariante === variant.idVariante)
      if (existing) {
        if (existing.quantity >= available) {
          setMessage('No puedes vender mas unidades que el stock disponible.')
          return current
        }
        return current.map((item) => item.variant.idVariante === variant.idVariante
          ? { ...item, quantity: item.quantity + 1 }
          : item)
      }
      return [...current, { variant, quantity: 1 }]
    })
  }

  function updateQuantity(idVariante: number, quantity: number) {
    if (quantity <= 0) {
      setCart((current) => current.filter((item) => item.variant.idVariante !== idVariante))
      return
    }
    setCart((current) => current.map((item) => {
      if (item.variant.idVariante !== idVariante) return item
      return { ...item, quantity: Math.min(quantity, stockDisponible(item.variant)) }
    }))
  }

  function resetPaymentFlow() {
    setPendingSale(null)
    setAmountReceived('')
    setManualReference('')
    setManualObservation('')
  }

  async function generarCobro() {
    if (!cart.length) return
    const payload = {
      metodoPago: payment,
      items: cart.map((item) => ({
        idVariante: item.variant.idVariante,
        cantidad: item.quantity,
        precioUnitario: item.variant.precioVenta,
      })),
    }
    try {
      const created = await salesApi.createSale(payload, crypto.randomUUID())
      setPendingSale(created)
      setMessage('No entregues los productos hasta que el pago este confirmado.')
      await loadVariants()
      await loadPendingPayments()
    } catch {
      setMessage('No se pudo generar el cobro. El carrito se mantiene sin cambios.')
    }
  }

  async function confirmarEfectivo() {
    if (!activePayment) {
      await generarCobro()
      return
    }
    try {
      const completed = await salesApi.confirmCash(activePayment.idPayment, {
        amountReceived: numericAmountReceived,
        reference: 'CAJA',
        observation: 'Confirmacion de efectivo en POS',
      })
      completarFlujo(completed)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo confirmar el pago en efectivo.')
    }
  }

  async function confirmarManualDigital() {
    if (!activePayment) return
    try {
      const completed = await salesApi.confirmManual(activePayment.idPayment, {
        amountReceived: total,
        currency: 'PEN',
        reference: manualReference,
        observation: manualObservation,
      })
      completarFlujo(completed)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo confirmar manualmente el pago.')
    }
  }

  async function verificarEstado() {
    if (!pendingSale?.idVenta) return
    try {
      const latest = await salesApi.getSale(pendingSale.idVenta)
      setPendingSale(latest.estado === 'COMPLETED' ? null : latest)
      if (latest.estado === 'COMPLETED') completarFlujo(latest)
      else setMessage('Pago pendiente. No entregues los productos hasta que el pago este confirmado.')
      await loadPendingPayments()
    } catch {
      setMessage('No se pudo verificar el estado del pago.')
    }
  }

  async function cancelarCobro() {
    if (!activePayment) return
    try {
      await salesApi.cancelPayment(activePayment.idPayment)
      resetPaymentFlow()
      await loadVariants()
      await loadPendingPayments()
      setMessage('Reserva liberada. La venta fue cancelada o expiro.')
    } catch {
      setMessage('No se pudo cancelar el cobro.')
    }
  }

  async function completarFlujo(completed: SaleResponse) {
    setSales((current) => [completed, ...current.filter((sale) => sale.idVenta !== completed.idVenta)])
    setCart([])
    resetPaymentFlow()
    await loadVariants()
    await loadPendingPayments()
    setMessage('Pago confirmado correctamente.')
  }

  async function verificarCobroPendiente(paymentItem: PaymentListItem) {
    try {
      const latest = await salesApi.getSale(paymentItem.idVenta)
      if (latest.estado === 'COMPLETED') {
        await completarFlujo(latest)
        return
      }
      setPayment(paymentItem.method)
      setPendingSale(latest)
      setMessage('Pago pendiente. El pago solo aparece confirmado cuando ADMIN lo confirme manualmente o cuando un proveedor/webhook lo confirme.')
    } catch {
      setMessage('No se pudo verificar este cobro pendiente.')
    } finally {
      await loadPendingPayments()
    }
  }

  const categorySales = [
    { name: 'Camisas', value: 42 },
    { name: 'Tops', value: 28 },
    { name: 'Vestidos', value: 18 },
    { name: 'Sacos', value: 12 },
  ]

  return (
    <div className="grid gap-6">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Ventas y POS</h1>
          <p className="text-sm text-[var(--color-muted)]">Registra ventas, consulta productos y arma el carrito de tienda.</p>
        </div>
        <div className="flex gap-2">
          <ActionButton variant="secondary">Exportar</ActionButton>
          <ActionButton>Nueva venta</ActionButton>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <KpiCard label="Ventas registradas" value={String(sales.length)} icon={Receipt} />
        <KpiCard label="Ticket promedio" value={`S/ ${ticketAverage.toFixed(2)}`} icon={ShoppingCart} tone="success" />
        <KpiCard label="Carrito actual" value={`S/ ${total.toFixed(2)}`} icon={Receipt} tone="warning" />
      </section>

      <div className="grid gap-6 xl:grid-cols-[1fr_384px]">
      <div className="page-grid">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <SearchInput value={query} onChange={updateSearch} placeholder="Buscar producto o SKU para vender" />
          <div className="flex shrink-0 flex-wrap items-center gap-2">
            {[3, 5].map((columns) => (
              <button
                key={columns}
                type="button"
                aria-pressed={gridColumns === columns}
                onClick={() => {
                  setGridColumns(columns as GridColumns)
                  setProductPage(1)
                }}
                className={cn(
                  'rounded-full px-3 py-1.5 text-sm font-semibold transition',
                  gridColumns === columns
                    ? 'bg-[var(--color-primary-soft)] text-[var(--color-text)] ring-1 ring-inset ring-black/10'
                    : 'border border-[var(--color-border)] text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
                )}
              >
                {columns} columnas
              </button>
            ))}
          </div>
        </div>

        <section className={cn(
          gridColumns === 5
            ? 'grid gap-3 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-5'
            : 'grid gap-4 sm:grid-cols-2 2xl:grid-cols-3',
        )}>
          {paginatedProducts.map((group) => (
            <ProductVariantCard key={group.idProducto} group={group} onAdd={addToCart} compact={gridColumns === 5} />
          ))}
        </section>

        {paginatedProducts.length === 0 && (
          <section className="rounded-2xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface)] p-8 text-center">
            <p className="font-bold text-[var(--color-text)]">Sin productos para vender</p>
            <p className="mt-1 text-sm text-[var(--color-muted)]">Ajusta la busqueda o revisa el catalogo disponible.</p>
          </section>
        )}

        <nav className="flex flex-col gap-3 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 sm:flex-row sm:items-center sm:justify-between" aria-label="Paginacion de productos para venta">
          <p className="text-sm font-semibold text-[var(--color-text)]">
            {productResultStart} - {productResultEnd} de {filtered.length} productos
          </p>
          <div className="flex items-center justify-center gap-2">
            <button
              type="button"
              disabled={currentProductPage === 1}
              onClick={() => setProductPage((current) => Math.max(1, current - 1))}
              className="grid size-9 place-items-center rounded-full text-[var(--color-muted)] transition hover:bg-[var(--color-bg)] disabled:opacity-35"
              aria-label="Pagina anterior"
            >
              <ChevronLeft size={18} />
            </button>
            <span className="min-w-20 text-center text-sm font-bold text-[var(--color-text)]">
              {currentProductPage} / {productTotalPages}
            </span>
            <button
              type="button"
              disabled={currentProductPage === productTotalPages}
              onClick={() => setProductPage((current) => Math.min(productTotalPages, current + 1))}
              className="grid size-9 place-items-center rounded-full text-[var(--color-muted)] transition hover:bg-[var(--color-bg)] disabled:opacity-35"
              aria-label="Pagina siguiente"
            >
              <ChevronRight size={18} />
            </button>
          </div>
        </nav>

        <ChartCard title="Ventas por categoria" subtitle="Referencia visual del diseno POS">
          <BarMetricChart data={categorySales} />
        </ChartCard>
      </div>

      <aside className="h-fit rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 xl:sticky xl:top-24">
        <div className="mb-4">
          <h2 className="text-xl font-bold">Resumen de venta</h2>
          <p className="text-sm text-[var(--color-muted)]">{message}</p>
        </div>
        <div className="space-y-3">
          {cart.map((item) => (
            <div key={item.variant.idVariante} className="rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] p-3">
              <div className="flex justify-between gap-3">
                <div>
                  <p className="font-semibold">{item.variant.nombreProducto}</p>
                  <p className="text-xs font-semibold text-[var(--color-muted)]">{item.variant.sku}</p>
                  <p className="text-xs font-semibold text-[var(--color-muted)]">
                    {item.variant.color} / {item.variant.talla === 'U' ? 'Talla unica' : `Talla ${item.variant.talla}`}
                  </p>
                  <p className="text-sm text-[var(--color-muted)]">S/ {item.variant.precioVenta.toFixed(2)}</p>
                </div>
                <IconButton label="Quitar" icon={Trash2} onClick={() => updateQuantity(item.variant.idVariante, 0)} />
              </div>
              <div className="mt-3 flex items-center gap-2">
                <IconButton label="Restar" icon={Minus} onClick={() => updateQuantity(item.variant.idVariante, item.quantity - 1)} />
                <span className="min-w-10 text-center font-bold">{item.quantity}</span>
                <IconButton label="Sumar" icon={Plus} onClick={() => updateQuantity(item.variant.idVariante, item.quantity + 1)} />
              </div>
            </div>
          ))}
        </div>
        <select
          value={payment}
          onChange={(event) => {
            setPayment(event.target.value as PaymentMethod)
            resetPaymentFlow()
          }}
          disabled={Boolean(pendingSale)}
          className="mt-4 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2"
        >
          <option>EFECTIVO</option>
          <option>TARJETA</option>
          <option>YAPE</option>
          <option>PLIN</option>
        </select>
        <div className="my-5 flex items-center justify-between border-y border-[var(--color-border)] py-4">
          <span className="font-semibold">Total</span>
          <span className="text-2xl font-bold text-[var(--color-primary-strong)]">S/ {total.toFixed(2)}</span>
        </div>
        {payment === 'EFECTIVO' && (
          <div className="space-y-3">
            <label className="block">
              <span className="text-sm font-semibold text-[var(--color-text)]">Monto recibido</span>
              <input
                type="number"
                min="0"
                step="0.01"
                value={amountReceived}
                onChange={(event) => setAmountReceived(event.target.value)}
                className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm outline-none"
              />
            </label>
            <div className="rounded-xl bg-[var(--color-bg)] p-3 text-sm">
              <div className="flex justify-between"><span>Debe</span><strong>S/ {total.toFixed(2)}</strong></div>
              <div className="flex justify-between"><span>Vuelto</span><strong>S/ {changeAmount.toFixed(2)}</strong></div>
            </div>
            <ActionButton
              className="w-full"
              onClick={activePayment ? confirmarEfectivo : generarCobro}
              disabled={!cart.length || !amountReceived || numericAmountReceived < total}
            >
              {activePayment ? 'Confirmar pago en efectivo' : 'Generar cobro'}
            </ActionButton>
          </div>
        )}

        {isDigitalPayment && (
          <div className="space-y-3">
            {!activePayment && (
              <ActionButton className="w-full" onClick={generarCobro} disabled={!cart.length}>Generar cobro</ActionButton>
            )}
            {activePayment && (
              <div className="space-y-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-3">
                <p className="text-sm font-bold text-[var(--color-text)]">Esperando pago</p>
                <p className="text-xs text-[var(--color-muted)]">No entregues los productos hasta que el pago este confirmado.</p>
                <div className="rounded-lg bg-[var(--color-surface)] p-3 text-xs">
                  <p className="font-bold">Referencia interna STUB</p>
                  <p className="break-all">{activePayment.providerReference}</p>
                  <p className="mt-2 text-[var(--color-muted)]">No es un QR ni comprobante de pago real.</p>
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <ActionButton variant="secondary" onClick={verificarEstado}>Verificar estado</ActionButton>
                  <ActionButton variant="danger" onClick={cancelarCobro}>Cancelar</ActionButton>
                </div>
                {isAdmin && (
                  <div className="space-y-2 border-t border-[var(--color-border)] pt-3">
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
                      className="min-h-20 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 text-sm outline-none"
                    />
                    <ActionButton
                      className="w-full"
                      onClick={confirmarManualDigital}
                      disabled={!manualReference.trim() || !manualObservation.trim()}
                    >
                      Confirmar manualmente
                    </ActionButton>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
        {pendingSale && (
          <TraceLine sale={pendingSale} />
        )}
        <div className="mt-5 rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-3">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h3 className="text-sm font-bold">Mis cobros pendientes</h3>
              <p className="text-xs text-[var(--color-muted)]">Verifica antes de entregar productos.</p>
            </div>
            <Link to="/pagos" className="text-xs font-bold text-[var(--color-primary-strong)] hover:underline">
              Abrir
            </Link>
          </div>
          <div className="mt-3 grid gap-2">
            {pendingPayments.map((item) => (
              <div key={item.idPayment} className="rounded-lg bg-[var(--color-surface)] p-3 text-sm">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="font-semibold">Venta #{item.idVenta}</p>
                    <p className="truncate text-xs text-[var(--color-muted)]">{item.providerReference}</p>
                  </div>
                  <Badge tone="warning">{item.method}</Badge>
                </div>
                <div className="mt-2 flex items-center justify-between gap-2">
                  <strong>S/ {item.amountDue.toFixed(2)}</strong>
                  <ActionButton variant="secondary" onClick={() => verificarCobroPendiente(item)}>Verificar</ActionButton>
                </div>
              </div>
            ))}
            {!pendingPayments.length && (
              <p className="rounded-lg bg-[var(--color-surface)] p-3 text-sm text-[var(--color-muted)]">No tienes cobros pendientes.</p>
            )}
          </div>
        </div>
        <div className="mt-5">
          <h3 className="mb-2 text-sm font-bold">Ventas recientes</h3>
          <DataTable rows={sales.slice(0, recentSalesLimit)} columns={salesColumns} />
        </div>
      </aside>
      </div>
    </div>
  )
}

function stockDisponible(variant: Variant) {
  return variant.stockDisponible ?? Math.max(variant.stockActual - (variant.stockReservado ?? 0), 0)
}

function TraceLine({ sale }: { sale: SaleResponse }) {
  const steps = [
    { label: 'Venta creada', done: true },
    { label: 'Stock reservado', done: true },
    { label: 'Pago generado', done: Boolean(sale.payment) },
    { label: 'Pago pendiente', done: sale.payment?.status === 'PENDING' },
    { label: 'Pago confirmado', done: sale.payment?.status === 'CONFIRMED' || sale.payment?.status === 'MANUALLY_CONFIRMED' },
    { label: 'Venta completada', done: sale.estado === 'COMPLETED' },
  ]

  return (
    <div className="mt-4 space-y-2 rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-3">
      {steps.map((step) => (
        <div key={step.label} className="flex items-center gap-2 text-xs font-semibold">
          <span className={`size-2 rounded-full ${step.done ? 'bg-[var(--color-primary)]' : 'bg-[var(--color-border)]'}`} />
          <span className={step.done ? 'text-[var(--color-text)]' : 'text-[var(--color-muted)]'}>{step.label}</span>
        </div>
      ))}
    </div>
  )
}

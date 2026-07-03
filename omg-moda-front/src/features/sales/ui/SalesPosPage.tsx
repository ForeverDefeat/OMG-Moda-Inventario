import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Minus, Plus, Receipt, ShoppingCart, Trash2 } from 'lucide-react'
import type { Variant } from '../../catalog/domain/types'
import type { SaleResponse } from '../domain/types'
import { productsApi } from '../../../infra/api/productsApi'
import { salesApi } from '../../../infra/api/salesApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { BarMetricChart } from '../../../shared/charts/BarMetricChart'
import { ChartCard } from '../../../shared/components/ChartCard'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { IconButton } from '../../../shared/components/IconButton'
import { KpiCard } from '../../../shared/components/KpiCard'
import { ProductPriceCard } from '../../../shared/components/ProductPriceCard'
import { SearchInput } from '../../../shared/components/SearchInput'

interface CartItem {
  variant: Variant
  quantity: number
}

const salesColumns: Column<SaleResponse>[] = [
  { key: 'id', header: 'Venta', render: (row) => `#${row.idVenta}` },
  { key: 'fecha', header: 'Fecha', render: (row) => new Date(row.fecha).toLocaleString('es-PE') },
  { key: 'metodo', header: 'Metodo', render: (row) => row.metodoPago },
  { key: 'total', header: 'Total', render: (row) => `S/ ${row.total.toFixed(2)}` },
]

export function SalesPosPage() {
  const [searchParams] = useSearchParams()
  const urlQuery = searchParams.get('q') ?? ''
  const [variants, setVariants] = useState<Variant[]>([])
  const [queryState, setQueryState] = useState(() => ({ source: urlQuery, value: urlQuery }))
  const [cart, setCart] = useState<CartItem[]>([])
  const [payment, setPayment] = useState('EFECTIVO')
  const [message, setMessage] = useState('Cargando POS desde backend.')
  const [sales, setSales] = useState<SaleResponse[]>([])
  const query = queryState.source === urlQuery ? queryState.value : urlQuery

  useEffect(() => {
    productsApi.listVariants()
      .then((data) => {
        setVariants(data)
        setMessage(data.length ? 'POS listo para vender.' : 'Backend conectado sin productos disponibles.')
      })
      .catch(() => {
        setVariants([])
        setMessage('Backend no disponible. No se muestran productos mock.')
      })

    salesApi.listSales()
      .then((data) => setSales(data))
      .catch(() => setSales([]))
  }, [])

  const filtered = useMemo(() => {
    const value = query.toLowerCase()
    return variants.filter((variant) =>
      [variant.nombreProducto, variant.categoria, variant.color, variant.talla].some((field) => field.toLowerCase().includes(value)),
    )
  }, [variants, query])

  const total = cart.reduce((sum, item) => sum + item.variant.precioVenta * item.quantity, 0)
  const ticketAverage = sales.length
    ? sales.reduce((sum, sale) => sum + sale.total, 0) / sales.length
    : 0

  function addToCart(variant: Variant) {
    if (variant.stockActual <= 0) {
      setMessage('No hay stock disponible para esta variante.')
      return
    }

    setCart((current) => {
      const existing = current.find((item) => item.variant.idVariante === variant.idVariante)
      if (existing) {
        if (existing.quantity >= variant.stockActual) {
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
      return { ...item, quantity: Math.min(quantity, item.variant.stockActual) }
    }))
  }

  async function checkout() {
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
      const created = await salesApi.createSale(payload)
      setSales((current) => [created, ...current])
      setVariants((current) => current.map((variant) => {
        const sold = payload.items.find((item) => item.idVariante === variant.idVariante)
        return sold ? { ...variant, stockActual: variant.stockActual - sold.cantidad } : variant
      }))
      setMessage(`Venta #${created.idVenta} registrada en backend.`)
      setCart([])
    } catch {
      setMessage('No se pudo registrar la venta en backend. El carrito se mantiene sin cambios.')
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
        <SearchInput value={query} onChange={(value) => setQueryState({ source: urlQuery, value })} placeholder="Buscar producto para vender" />

        <section className="grid gap-4 sm:grid-cols-2 2xl:grid-cols-4">
          {filtered.slice(0, 8).map((variant) => (
            <ProductPriceCard key={variant.idVariante} variant={variant} onAdd={addToCart} compact />
          ))}
        </section>

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
        <select value={payment} onChange={(event) => setPayment(event.target.value)} className="mt-4 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2">
          <option>EFECTIVO</option>
          <option>TARJETA</option>
          <option>YAPE</option>
          <option>PLIN</option>
        </select>
        <div className="my-5 flex items-center justify-between border-y border-[var(--color-border)] py-4">
          <span className="font-semibold">Total</span>
          <span className="text-2xl font-bold text-[var(--color-primary-strong)]">S/ {total.toFixed(2)}</span>
        </div>
        <ActionButton className="w-full" onClick={checkout} disabled={!cart.length}>Completar venta</ActionButton>
        <div className="mt-5">
          <h3 className="mb-2 text-sm font-bold">Ventas recientes</h3>
          <DataTable rows={sales.slice(0, 4)} columns={salesColumns} />
        </div>
      </aside>
      </div>
    </div>
  )
}

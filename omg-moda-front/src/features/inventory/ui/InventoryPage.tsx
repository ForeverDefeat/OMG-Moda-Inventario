import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import {
  Building2,
  ClipboardCheck,
  MapPin,
  PackageCheck,
  Plus,
  SlidersHorizontal,
  Truck,
} from 'lucide-react'
import type { RegisterAdjustmentRequest, RegisterEntryRequest } from '../domain/types'
import type { Variant } from '../../catalog/domain/types'
import { inventoryApi } from '../../../infra/api/inventoryApi'
import { productsApi } from '../../../infra/api/productsApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { Badge, StockBadge } from '../../../shared/components/Badge'
import { KpiCard } from '../../../shared/components/KpiCard'
import { Modal } from '../../../shared/components/Modal'
import { SearchInput } from '../../../shared/components/SearchInput'
import { getVariantImage } from '../../../shared/utils/productImages'
import { cn } from '../../../shared/utils/cn'

interface Warehouse {
  id: string
  nombre: string
  tipo: string
  direccion: string
  responsable: string
  capacidad: number
  estado: 'Operativo' | 'Preparacion'
}

interface StockLine {
  pasillo: string
  contenedor: string
  asignado: number
  entrante: number
}

const initialWarehouses: Warehouse[] = [
  {
    id: 'almacen-principal',
    nombre: 'Almacen Principal',
    tipo: 'Almacen',
    direccion: 'Av. Principal 125, Lima',
    responsable: 'Administrador OMG MODA',
    capacidad: 82,
    estado: 'Operativo',
  },
]

const stockLocations: Record<number, StockLine> = {
  1: { pasillo: 'Pasillo 4', contenedor: 'Contenedor B2', asignado: 12, entrante: 50 },
  2: { pasillo: 'Pasillo 2', contenedor: 'Contenedor A1', asignado: 0, entrante: 0 },
  3: { pasillo: 'Pasillo 6', contenedor: 'Contenedor C3', asignado: 8, entrante: 20 },
  4: { pasillo: 'Pasillo 8', contenedor: 'Contenedor D1', asignado: 0, entrante: 0 },
  5: { pasillo: 'Pasillo 7', contenedor: 'Contenedor C4', asignado: 8, entrante: 100 },
  6: { pasillo: 'Pasillo 1', contenedor: 'Contenedor A8', asignado: 10, entrante: 0 },
}

const defaultWarehouseDraft = {
  nombre: '',
  tipo: 'Almacen',
  direccion: '',
  responsable: '',
  capacidad: 60,
}

export function InventoryPage() {
  const [variants, setVariants] = useState<Variant[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>(initialWarehouses)
  const [activeWarehouseId, setActiveWarehouseId] = useState(initialWarehouses[0].id)
  const [warehouseSearch, setWarehouseSearch] = useState('')
  const [inventorySearch, setInventorySearch] = useState('')
  const [entryOpen, setEntryOpen] = useState(false)
  const [adjustOpen, setAdjustOpen] = useState(false)
  const [warehouseOpen, setWarehouseOpen] = useState(false)
  const [message, setMessage] = useState('Cargando inventario desde backend.')

  useEffect(() => {
    productsApi.listVariants()
      .then((data) => {
        setVariants(data)
        setMessage(data.length ? 'Almacen principal listo para operar.' : 'Backend conectado sin inventario registrado.')
      })
      .catch(() => {
        setVariants([])
        setMessage('Backend no disponible. No se muestran datos mock.')
      })
  }, [])

  const activeWarehouse = warehouses.find((warehouse) => warehouse.id === activeWarehouseId) ?? warehouses[0]
  const totalUnits = variants.reduce((total, variant) => total + variant.stockActual, 0)
  const totalValue = variants.reduce((total, variant) => total + variant.stockActual * variant.precioCosto, 0)
  const lowStockCount = variants.filter((variant) => variant.stockActual <= variant.stockMinimo).length

  const visibleWarehouses = useMemo(() => {
    const query = warehouseSearch.trim().toLowerCase()
    if (!query) return warehouses
    return warehouses.filter((warehouse) =>
      [warehouse.nombre, warehouse.tipo, warehouse.direccion].some((value) => value.toLowerCase().includes(query)),
    )
  }, [warehouseSearch, warehouses])

  const visibleVariants = useMemo(() => {
    const query = inventorySearch.trim().toLowerCase()
    if (!query) return variants
    return variants.filter((variant) =>
      [variant.nombreProducto, variant.categoria, variant.talla, variant.color, variant.marca]
        .some((value) => String(value).toLowerCase().includes(query)),
    )
  }, [inventorySearch, variants])

  async function submitEntry(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const payload: RegisterEntryRequest = {
      idVariante: Number(form.get('idVariante')),
      cantidad: Number(form.get('cantidad')),
      motivo: String(form.get('motivo') ?? ''),
    }
    try {
      const movement = await inventoryApi.registerEntry(payload)
      setVariants((current) => current.map((variant) => variant.idVariante === payload.idVariante
        ? { ...variant, stockActual: movement.stockResultante }
        : variant))
      setMessage('Movimiento registrado en backend.')
      setEntryOpen(false)
    } catch {
      setMessage('No se pudo registrar el movimiento en backend. No se aplicaron cambios locales.')
    }
  }

  async function submitAdjustment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const payload: RegisterAdjustmentRequest = {
      idVariante: Number(form.get('idVariante')),
      cantidad: Number(form.get('cantidad')),
      motivo: String(form.get('motivo') ?? ''),
    }
    try {
      const movement = await inventoryApi.registerAdjustment(payload)
      setVariants((current) => current.map((variant) => variant.idVariante === payload.idVariante
        ? { ...variant, stockActual: movement.stockResultante }
        : variant))
      setMessage('Ajuste registrado en backend.')
      setAdjustOpen(false)
    } catch {
      setMessage('No se pudo registrar el ajuste en backend. No se aplicaron cambios locales.')
    }
  }

  function submitWarehouse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const nombre = String(form.get('nombre') ?? '').trim()
    const newWarehouse: Warehouse = {
      id: `almacen-${Date.now()}`,
      nombre,
      tipo: String(form.get('tipo') ?? 'Almacen'),
      direccion: String(form.get('direccion') ?? '').trim(),
      responsable: String(form.get('responsable') ?? '').trim() || 'Administrador OMG MODA',
      capacidad: Number(form.get('capacidad')) || 60,
      estado: 'Preparacion',
    }
    setWarehouses((current) => [...current, newWarehouse])
    setActiveWarehouseId(newWarehouse.id)
    setMessage(`${nombre} agregado como almacen en preparacion.`)
    setWarehouseOpen(false)
    event.currentTarget.reset()
  }

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Gestion de Stock</h1>
          <p className="text-sm text-[var(--color-muted)]">Monitoree un almacen principal y prepare nuevas ubicaciones cuando las necesite.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <ActionButton variant="secondary" onClick={() => setEntryOpen(true)}><Truck size={17} /> Transferir Stock</ActionButton>
          <ActionButton onClick={() => setAdjustOpen(true)}><SlidersHorizontal size={17} /> Ajustar Stock</ActionButton>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard label="Valor Total del Stock" value={`S/ ${totalValue.toLocaleString('es-PE')}`} icon={PackageCheck} />
        <KpiCard label="Unidades Disponibles" value={totalUnits.toLocaleString('es-PE')} icon={Truck} tone="success" />
        <KpiCard label="Almacenes Activos" value={String(warehouses.length)} icon={MapPin} />
        <KpiCard label="Ajustes Pendientes" value={String(lowStockCount)} icon={ClipboardCheck} tone="warning" />
      </section>

      <section className="grid gap-5 xl:grid-cols-[260px_minmax(0,1fr)]">
        <aside className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <div className="mb-4 flex items-center justify-between gap-3">
            <h2 className="text-sm font-bold">Almacenes</h2>
            <button
              type="button"
              onClick={() => setWarehouseOpen(true)}
              className="inline-flex min-h-8 items-center gap-1 rounded-full px-2 text-xs font-bold text-[var(--color-text)] hover:bg-[var(--color-bg)]"
            >
              <Plus size={14} /> Anadir
            </button>
          </div>
          <SearchInput value={warehouseSearch} onChange={setWarehouseSearch} placeholder="Buscar almacenes..." />
          <div className="mt-4 grid gap-3">
            {visibleWarehouses.map((warehouse) => {
              const selected = warehouse.id === activeWarehouse.id
              return (
                <button
                  type="button"
                  key={warehouse.id}
                  onClick={() => setActiveWarehouseId(warehouse.id)}
                  className={cn(
                    'rounded-2xl border p-4 text-left transition hover:border-[var(--color-primary)]',
                    selected ? 'border-[var(--color-text)] bg-[var(--color-bg)]' : 'border-[var(--color-border)] bg-[var(--color-surface)]',
                  )}
                >
                  <span className="block text-sm font-bold">{warehouse.nombre}</span>
                  <span className="mt-1 block text-xs text-[var(--color-muted)]">{warehouse.tipo}</span>
                  <span className="mt-3 flex items-center justify-between gap-2 text-xs font-semibold">
                    <span className={warehouse.estado === 'Operativo' ? 'text-[var(--color-success-foreground)]' : 'text-[var(--color-warning-foreground)]'}>
                      {warehouse.estado}
                    </span>
                    <span>{totalUnits.toLocaleString('es-PE')} articulos</span>
                  </span>
                </button>
              )
            })}
            {!visibleWarehouses.length && (
              <p className="rounded-2xl border border-dashed border-[var(--color-border)] p-4 text-sm text-[var(--color-muted)]">
                No hay almacenes con ese criterio.
              </p>
            )}
          </div>
        </aside>

        <section className="min-w-0 overflow-hidden rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)]">
          <div className="grid gap-4 border-b border-[var(--color-border)] p-5 lg:grid-cols-[1fr_auto]">
            <div className="min-w-0">
              <div className="mb-2 flex flex-wrap items-center gap-2">
                <span className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">{activeWarehouse.tipo}</span>
                <Badge tone={activeWarehouse.estado === 'Operativo' ? 'success' : 'warning'}>{activeWarehouse.estado}</Badge>
              </div>
              <h2 className="truncate text-2xl font-black">{activeWarehouse.nombre}</h2>
              <p className="mt-1 text-sm text-[var(--color-muted)]">{activeWarehouse.direccion || 'Direccion pendiente por registrar.'}</p>
            </div>
            <div className="grid grid-cols-2 gap-4 text-sm sm:grid-cols-3">
              <Metric label="Capacidad" value={`${activeWarehouse.capacidad}%`} />
              <Metric label="Responsable" value={activeWarehouse.responsable} />
              <Metric label="Estado" value={message} muted />
            </div>
          </div>

          <div className="flex flex-col gap-3 border-b border-[var(--color-border)] p-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="font-bold">Inventario</h3>
                <Badge tone="primary">{visibleVariants.length}</Badge>
              </div>
              <p className="text-sm text-[var(--color-muted)]">Mostrando stock del almacen seleccionado.</p>
            </div>
            <div className="flex w-full flex-col gap-2 sm:flex-row lg:w-auto">
              <div className="sm:min-w-72">
                <SearchInput value={inventorySearch} onChange={setInventorySearch} placeholder="Buscar por SKU o articulo..." />
              </div>
              <ActionButton variant="secondary" className="shrink-0"><SlidersHorizontal size={16} /> Filtrar</ActionButton>
            </div>
          </div>

          <InventoryList variants={visibleVariants} onEntry={() => setEntryOpen(true)} onAdjust={() => setAdjustOpen(true)} />
        </section>
      </section>

      <MovementModal open={entryOpen} title="Transferir stock" variants={variants} onClose={() => setEntryOpen(false)} onSubmit={submitEntry} />
      <MovementModal open={adjustOpen} title="Ajustar stock" variants={variants} onClose={() => setAdjustOpen(false)} onSubmit={submitAdjustment} adjustment />
      <WarehouseModal open={warehouseOpen} onClose={() => setWarehouseOpen(false)} onSubmit={submitWarehouse} />
    </div>
  )
}

function Metric({ label, value, muted = false }: { label: string; value: string; muted?: boolean }) {
  return (
    <div className="min-w-0 rounded-xl bg-[var(--color-bg)] px-3 py-2">
      <span className="block text-xs text-[var(--color-muted)]">{label}</span>
      <strong className={cn('mt-1 block truncate text-sm', muted && 'font-medium text-[var(--color-muted)]')}>{value}</strong>
    </div>
  )
}

function InventoryList({ variants, onEntry, onAdjust }: {
  variants: Variant[]
  onEntry: () => void
  onAdjust: () => void
}) {
  return (
    <div className="overflow-hidden">
      <div className="hidden border-b border-[var(--color-border)] bg-[var(--color-bg)] px-4 py-3 text-xs font-bold uppercase tracking-[0.06em] text-[var(--color-muted)] lg:grid lg:grid-cols-[minmax(160px,1.4fr)_minmax(118px,.9fr)_repeat(4,minmax(50px,.45fr))_minmax(126px,.8fr)] lg:gap-3">
        <span>Producto / SKU</span>
        <span>Pasillo / Contenedor</span>
        <span>A Mano</span>
        <span>Asignado</span>
        <span>Disponible</span>
        <span>Entrante</span>
        <span>Acciones</span>
      </div>
      <div className="divide-y divide-[var(--color-border)]">
        {variants.map((variant) => {
          const location = stockLocations[variant.idVariante] ?? { pasillo: 'Pasillo 1', contenedor: 'Contenedor A1', asignado: 0, entrante: 0 }
          const available = Math.max(variant.stockActual - location.asignado, 0)
          return (
            <article
              key={variant.idVariante}
              className="grid gap-3 px-4 py-4 lg:grid-cols-[minmax(160px,1.4fr)_minmax(118px,.9fr)_repeat(4,minmax(50px,.45fr))_minmax(126px,.8fr)] lg:items-center"
            >
              <div className="flex min-w-0 items-center gap-3">
                <img
                  src={getVariantImage(variant)}
                  alt={variant.nombreProducto}
                  className="h-11 w-11 shrink-0 rounded-xl border border-[var(--color-border)] object-cover"
                />
                <div className="min-w-0">
                  <h4 className="truncate text-sm font-bold">{variant.nombreProducto}</h4>
                  <p className="text-xs text-[var(--color-muted)]">CW-{String(variant.idVariante).padStart(5, '0')}</p>
                </div>
              </div>
              <RowValue label="Ubicacion" value={`${location.pasillo}, ${location.contenedor}`} />
              <RowValue label="A Mano" value={String(variant.stockActual)} strong />
              <RowValue label="Asignado" value={String(location.asignado)} />
              <RowValue label="Disponible" value={String(available)} strong danger={available <= variant.stockMinimo} />
              <RowValue label="Entrante" value={location.entrante ? `+${location.entrante}` : '-'} success={location.entrante > 0} />
              <div className="flex flex-wrap gap-2 lg:justify-end">
                <ActionButton variant="secondary" className="min-h-8 px-3 text-xs" onClick={onEntry}><Truck size={14} /> Transferir</ActionButton>
                <ActionButton className="min-h-8 px-3 text-xs" onClick={onAdjust}>Ajustar</ActionButton>
              </div>
            </article>
          )
        })}
        {!variants.length && (
          <p className="p-6 text-sm text-[var(--color-muted)]">No hay articulos para mostrar con ese criterio.</p>
        )}
      </div>
      <div className="flex flex-col gap-3 border-t border-[var(--color-border)] px-4 py-4 text-sm text-[var(--color-muted)] sm:flex-row sm:items-center sm:justify-between">
        <span>Mostrando {variants.length} articulos del almacen seleccionado</span>
        <StockBadge stock={variants.filter((variant) => variant.stockActual > variant.stockMinimo).length} min={1} />
      </div>
    </div>
  )
}

function RowValue({ label, value, strong = false, danger = false, success = false }: {
  label: string
  value: string
  strong?: boolean
  danger?: boolean
  success?: boolean
}) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-xl bg-[var(--color-bg)] px-3 py-2 lg:block lg:bg-transparent lg:p-0">
      <span className="text-xs text-[var(--color-muted)] lg:hidden">{label}</span>
      <span className={cn(
        'text-sm',
        strong && 'font-bold',
        danger && 'text-[var(--color-danger-foreground)]',
        success && 'font-bold text-[var(--color-success-foreground)]',
      )}>
        {value}
      </span>
    </div>
  )
}

function MovementModal({ open, title, variants, onClose, onSubmit, adjustment = false }: {
  open: boolean
  title: string
  variants: Variant[]
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  adjustment?: boolean
}) {
  return (
    <Modal open={open} title={title} onClose={onClose}>
      <form onSubmit={onSubmit} className="grid gap-3">
        <select name="idVariante" className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2">
          {variants.map((variant) => (
            <option key={variant.idVariante} value={variant.idVariante}>{variant.nombreProducto} / {variant.talla} / {variant.color}</option>
          ))}
        </select>
        <input name="cantidad" type="number" min={adjustment ? 0 : 1} required className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder={adjustment ? 'Nuevo stock absoluto' : 'Cantidad a mover'} />
        <textarea name="motivo" required={adjustment} className="min-h-24 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Motivo u observacion" />
        <ActionButton type="submit">{adjustment ? 'Guardar ajuste' : 'Guardar movimiento'}</ActionButton>
      </form>
    </Modal>
  )
}

function WarehouseModal({ open, onClose, onSubmit }: {
  open: boolean
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}) {
  return (
    <Modal open={open} title="Anadir almacen" onClose={onClose}>
      <form onSubmit={onSubmit} className="grid gap-4">
        <label className="grid gap-1 text-sm font-semibold">
          Nombre
          <input name="nombre" required defaultValue={defaultWarehouseDraft.nombre} className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="Almacen secundario" />
        </label>
        <div className="grid gap-3 sm:grid-cols-2">
          <label className="grid gap-1 text-sm font-semibold">
            Tipo
            <select name="tipo" defaultValue={defaultWarehouseDraft.tipo} className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal">
              <option>Almacen</option>
              <option>Tienda</option>
              <option>Procesamiento</option>
            </select>
          </label>
          <label className="grid gap-1 text-sm font-semibold">
            Capacidad
            <input name="capacidad" type="number" min={1} max={100} defaultValue={defaultWarehouseDraft.capacidad} className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" />
          </label>
        </div>
        <label className="grid gap-1 text-sm font-semibold">
          Direccion
          <input name="direccion" defaultValue={defaultWarehouseDraft.direccion} className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="Av. Principal 125, Lima" />
        </label>
        <label className="grid gap-1 text-sm font-semibold">
          Responsable
          <input name="responsable" defaultValue={defaultWarehouseDraft.responsable} className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="Administrador OMG MODA" />
        </label>
        <div className="rounded-xl bg-[var(--color-bg)] p-3 text-sm text-[var(--color-muted)]">
          El almacen se agregara a la pantalla como preparacion. La persistencia en backend puede conectarse cuando exista el modulo de almacenes.
        </div>
        <ActionButton type="submit"><Building2 size={17} /> Agregar almacen</ActionButton>
      </form>
    </Modal>
  )
}

import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import {
  Building2,
  ClipboardCheck,
  Image as ImageIcon,
  Link as LinkIcon,
  MapPin,
  PackageCheck,
  Pencil,
  Plus,
  SlidersHorizontal,
  Truck,
  Upload,
} from 'lucide-react'
import type { RegisterAdjustmentRequest, RegisterEntryRequest } from '../domain/types'
import type { UpdateProductRequest, Variant } from '../../catalog/domain/types'
import { groupVariantsByProduct, type ProductGroup } from '../../catalog/domain/productGroups'
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

type InventorySortKey = 'producto' | 'ubicacion' | 'aMano' | 'disponible' | 'entrante'
type SortDirection = 'asc' | 'desc'
type ImageMode = 'url' | 'upload'

interface InventorySortColumn {
  key: InventorySortKey
  label: string
  type: 'text' | 'number'
}

type InventoryProductGroup = ProductGroup & {
  locationLabel: string
  availableTotal: number
  incomingTotal: number
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

const customReasonValue = '__custom__'
const movementReasons = [
  'Reposicion',
  'Conteo fisico',
  'Correccion por merma',
  'Devolucion',
] as const
const maxImageSizeBytes = 5 * 1024 * 1024
const allowedImageExtensions = ['jpg', 'jpeg', 'png', 'webp']

const inventorySortColumns: InventorySortColumn[] = [
  { key: 'producto', label: 'Producto', type: 'text' },
  { key: 'ubicacion', label: 'Ubicacion', type: 'text' },
  { key: 'aMano', label: 'A Mano', type: 'number' },
  { key: 'disponible', label: 'Disponible', type: 'number' },
  { key: 'entrante', label: 'Entrante', type: 'number' },
]

function getStockLocation(idVariante: number) {
  return stockLocations[idVariante] ?? { pasillo: 'Pasillo 1', contenedor: 'Contenedor A1', asignado: 0, entrante: 0 }
}

function availableUnits(variant: Variant, location = getStockLocation(variant.idVariante)) {
  return Math.max(variant.stockActual - location.asignado, 0)
}

function inventorySortValue(group: InventoryProductGroup, key: InventorySortKey) {
  if (key === 'producto') return group.nombreProducto
  if (key === 'ubicacion') return group.locationLabel
  if (key === 'aMano') return group.stockTotal
  if (key === 'disponible') return group.availableTotal
  return group.incomingTotal
}

function buildInventoryGroups(variants: Variant[]): InventoryProductGroup[] {
  return groupVariantsByProduct(variants).map((group) => {
    const [firstVariant] = group.variants
    const firstLocation = getStockLocation(firstVariant.idVariante)

    return {
      ...group,
      locationLabel: `${firstLocation.pasillo}, ${firstLocation.contenedor}`,
      availableTotal: group.variants.reduce((total, variant) => total + availableUnits(variant), 0),
      incomingTotal: group.variants.reduce((total, variant) => total + getStockLocation(variant.idVariante).entrante, 0),
    }
  })
}

function isValidImageUrl(value: string) {
  if (!value) return true
  if (value.startsWith('/uploads/')) return true

  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

function imageFileError(file: File) {
  const extension = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!allowedImageExtensions.includes(extension)) {
    return 'Formato no permitido. Usa JPG, PNG o WEBP.'
  }
  if (file.size > maxImageSizeBytes) {
    return 'La imagen no puede superar 5 MB.'
  }
  return ''
}

export function InventoryPage() {
  const [variants, setVariants] = useState<Variant[]>([])
  const [warehouses, setWarehouses] = useState<Warehouse[]>(initialWarehouses)
  const [activeWarehouseId, setActiveWarehouseId] = useState(initialWarehouses[0].id)
  const [warehouseSearch, setWarehouseSearch] = useState('')
  const [inventorySearch, setInventorySearch] = useState('')
  const [entryOpen, setEntryOpen] = useState(false)
  const [adjustOpen, setAdjustOpen] = useState(false)
  const [selectedEntryVariantId, setSelectedEntryVariantId] = useState<number | null>(null)
  const [selectedAdjustVariantId, setSelectedAdjustVariantId] = useState<number | null>(null)
  const [selectedProduct, setSelectedProduct] = useState<InventoryProductGroup | null>(null)
  const [warehouseOpen, setWarehouseOpen] = useState(false)
  const [productEditOpen, setProductEditOpen] = useState(false)
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
    return variants.filter((variant) => {
      const location = getStockLocation(variant.idVariante)
      return [variant.sku, variant.nombreProducto, variant.categoria, variant.talla, variant.color, variant.marca, location.pasillo, location.contenedor]
        .some((value) => String(value).toLowerCase().includes(query))
    })
  }, [inventorySearch, variants])

  const visibleInventoryGroups = useMemo(() => buildInventoryGroups(visibleVariants), [visibleVariants])

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
      setSelectedEntryVariantId(null)
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
      setSelectedAdjustVariantId(null)
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

  function openEntryModal(idVariante?: number) {
    setSelectedEntryVariantId(idVariante ?? null)
    setEntryOpen(true)
  }

  function openAdjustmentModal(idVariante?: number) {
    setSelectedAdjustVariantId(idVariante ?? null)
    setAdjustOpen(true)
  }

  function openProductEditModal(group: InventoryProductGroup) {
    setSelectedProduct(group)
    setProductEditOpen(true)
  }

  async function submitProductUpdate(payload: UpdateProductRequest, imageFile: File | null) {
    if (!selectedProduct) return

    try {
      const updatedVariants = await productsApi.updateProduct(selectedProduct.idProducto, payload, imageFile)
      setVariants((current) => [
        ...current.filter((variant) => variant.idProducto !== selectedProduct.idProducto),
        ...updatedVariants,
      ].sort((a, b) => a.idVariante - b.idVariante))
      setMessage('Producto actualizado en backend.')
      setProductEditOpen(false)
      setSelectedProduct(null)
    } catch {
      setMessage('No se pudo actualizar el producto en backend. No se aplicaron cambios locales.')
    }
  }

  return (
    <div className="page-grid">
      <section className="flex justify-end">
        <div className="flex flex-wrap gap-2">
          <ActionButton variant="secondary" onClick={() => openEntryModal()}><Truck size={17} /> Transferir Stock</ActionButton>
          <ActionButton onClick={() => openAdjustmentModal()}><SlidersHorizontal size={17} /> Ajustar Stock</ActionButton>
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
                <Badge tone="primary">{visibleInventoryGroups.length}</Badge>
              </div>
              <p className="text-sm text-[var(--color-muted)]">
                {visibleVariants.length} variantes agrupadas por producto.
              </p>
            </div>
            <div className="flex w-full flex-col gap-2 sm:flex-row lg:w-auto">
              <div className="sm:min-w-72">
                <SearchInput value={inventorySearch} onChange={setInventorySearch} placeholder="Buscar por SKU o articulo..." />
              </div>
              <ActionButton variant="secondary" className="shrink-0"><SlidersHorizontal size={16} /> Filtrar</ActionButton>
            </div>
          </div>

          <InventoryList groups={visibleInventoryGroups} onEntry={openEntryModal} onAdjust={openAdjustmentModal} onEditProduct={openProductEditModal} />
        </section>
      </section>

      <MovementModal open={entryOpen} title="Transferir stock" variants={variants} selectedVariantId={selectedEntryVariantId} onClose={() => {
        setEntryOpen(false)
        setSelectedEntryVariantId(null)
      }} onSubmit={submitEntry} />
      {adjustOpen && (
        <AdjustmentModal key={selectedAdjustVariantId ?? 'manual'} open variants={variants} selectedVariantId={selectedAdjustVariantId} onClose={() => {
          setAdjustOpen(false)
          setSelectedAdjustVariantId(null)
        }} onSubmit={submitAdjustment} />
      )}
      <WarehouseModal open={warehouseOpen} onClose={() => setWarehouseOpen(false)} onSubmit={submitWarehouse} />
      {selectedProduct && (
        <ProductEditModal
          key={selectedProduct.idProducto}
          open={productEditOpen}
          group={selectedProduct}
          onClose={() => {
            setProductEditOpen(false)
            setSelectedProduct(null)
          }}
          onSubmit={submitProductUpdate}
        />
      )}
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

function InventoryList({ groups, onEntry, onAdjust, onEditProduct }: {
  groups: InventoryProductGroup[]
  onEntry: (idVariante: number) => void
  onAdjust: (idVariante: number) => void
  onEditProduct: (group: InventoryProductGroup) => void
}) {
  const [sortKey, setSortKey] = useState<InventorySortKey>('producto')
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc')

  const sortedGroups = useMemo(() => {
    const column = inventorySortColumns.find((item) => item.key === sortKey)
    const direction = sortDirection === 'asc' ? 1 : -1

    return groups
      .map((group, index) => ({ group, index }))
      .sort((left, right) => {
        const leftValue = inventorySortValue(left.group, sortKey)
        const rightValue = inventorySortValue(right.group, sortKey)
        const comparison = column?.type === 'number'
          ? Number(leftValue) - Number(rightValue)
          : String(leftValue).localeCompare(String(rightValue), 'es', { sensitivity: 'base', numeric: true })

        return comparison === 0 ? left.index - right.index : comparison * direction
      })
      .map((item) => item.group)
  }, [groups, sortDirection, sortKey])

  function toggleSort(key: InventorySortKey) {
    setSortKey((currentKey) => {
      if (currentKey === key) {
        setSortDirection((currentDirection) => currentDirection === 'asc' ? 'desc' : 'asc')
        return currentKey
      }

      setSortDirection('asc')
      return key
    })
  }

  function sortIndicator(key: InventorySortKey) {
    if (sortKey !== key) return '↕'
    return sortDirection === 'asc' ? '↑' : '↓'
  }

  return (
    <div className="overflow-hidden">
      <div className="flex flex-wrap items-center gap-2 border-b border-[var(--color-border)] bg-[var(--color-bg)] px-4 py-3">
        <span className="mr-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Ordenar</span>
        {inventorySortColumns.map((column) => (
          <button
            key={column.key}
            type="button"
            onClick={() => toggleSort(column.key)}
            className={cn(
              'inline-flex min-h-8 items-center gap-1 rounded-full border px-3 text-xs font-bold transition',
              sortKey === column.key
                ? 'border-[var(--color-text)] bg-[var(--color-surface)] text-[var(--color-text)]'
                : 'border-[var(--color-border)] bg-transparent text-[var(--color-muted)] hover:text-[var(--color-text)]',
            )}
          >
            <span>{column.label}</span>
            <span className="text-[10px]" aria-hidden="true">{sortIndicator(column.key)}</span>
          </button>
        ))}
      </div>

      <div className="max-h-[560px] overflow-y-auto overflow-x-hidden">
        <div className="grid gap-4 p-4">
          {sortedGroups.map((group) => {
            const primaryVariant = group.variants[0]
            return (
              <article key={group.idProducto} className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
                <div className="grid gap-4 xl:grid-cols-[minmax(260px,.9fr)_1fr]">
                  <div className="flex min-w-0 gap-4">
                    <img
                      src={getVariantImage(primaryVariant)}
                      alt={group.nombreProducto}
                      className="h-20 w-20 shrink-0 rounded-2xl border border-[var(--color-border)] object-cover"
                    />
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <h4 className="truncate text-base font-black">{group.nombreProducto}</h4>
                        <StockBadge stock={group.stockTotal} min={group.stockMinimoGrupo} />
                        <button
                          type="button"
                          onClick={() => onEditProduct(group)}
                          className="inline-flex min-h-8 items-center gap-1 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-xs font-bold text-[var(--color-text)] transition hover:border-[var(--color-text)]"
                        >
                          <Pencil size={13} />
                          Editar
                        </button>
                      </div>
                      <p className="mt-1 text-sm font-semibold text-[var(--color-muted)]">{group.categoria} / {group.marca}</p>
                      <p className="mt-1 text-xs text-[var(--color-muted)]">{group.variants.length} variantes / ubicacion base {group.locationLabel}</p>
                      <div className="mt-4 grid grid-cols-3 gap-2">
                        <Metric label="A mano" value={String(group.stockTotal)} />
                        <Metric label="Disponible" value={String(group.availableTotal)} />
                        <Metric label="Entrante" value={group.incomingTotal ? `+${group.incomingTotal}` : '-'} />
                      </div>
                    </div>
                  </div>

                  <div>
                    <p className="mb-2 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Variantes disponibles</p>
                    <div className="grid gap-3 md:grid-cols-2 2xl:grid-cols-3">
                      {group.variants.map((variant) => {
                        const location = getStockLocation(variant.idVariante)
                        const available = availableUnits(variant, location)
                        const lowStock = variant.stockActual <= variant.stockMinimo

                        return (
                          <div
                            key={variant.idVariante}
                            className={cn(
                              'rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-3 text-xs',
                              variant.stockActual <= 0 && 'opacity-60',
                            )}
                          >
                            <div className="flex flex-wrap items-center gap-2">
                              <span className="font-bold text-[var(--color-text)]">{variant.color}</span>
                              <span className="rounded-full bg-[var(--color-surface)] px-2 py-0.5 font-bold text-[var(--color-text)]">{variant.talla}</span>
                              <Badge tone={lowStock ? 'warning' : 'success'}>{lowStock ? 'Alerta' : 'OK'}</Badge>
                            </div>
                            <p className="mt-2 font-semibold text-[var(--color-muted)]">{variant.sku}</p>
                            <p className="mt-1 text-[var(--color-muted)]">{location.pasillo}, {location.contenedor}</p>
                            <div className="mt-3 grid grid-cols-2 gap-2">
                              <MiniStat label="Stock" value={String(variant.stockActual)} />
                              <MiniStat label="Disp." value={String(available)} danger={available <= variant.stockMinimo} />
                            </div>
                            <div className="mt-3 flex flex-wrap gap-2">
                              <button
                                type="button"
                                onClick={() => onEntry(variant.idVariante)}
                                className="min-h-8 flex-1 rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-2 font-bold text-[var(--color-text)] transition hover:border-[var(--color-text)]"
                              >
                                Transferir
                              </button>
                              <button
                                type="button"
                                onClick={() => onAdjust(variant.idVariante)}
                                className="min-h-8 flex-1 rounded-lg bg-[var(--color-primary)] px-2 font-bold text-white transition hover:bg-[var(--color-primary-strong)]"
                              >
                                Ajustar
                              </button>
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                </div>
              </article>
            )
          })}
          {!sortedGroups.length && (
            <p className="rounded-2xl border border-dashed border-[var(--color-border)] p-6 text-sm text-[var(--color-muted)]">No hay articulos para mostrar con ese criterio.</p>
          )}
        </div>
      </div>
      <div className="flex flex-col gap-3 border-t border-[var(--color-border)] px-4 py-4 text-sm text-[var(--color-muted)] sm:flex-row sm:items-center sm:justify-between">
        <span>
          Mostrando {sortedGroups.length} productos / {sortedGroups.reduce((total, group) => total + group.variants.length, 0)} variantes
        </span>
        <StockBadge stock={sortedGroups.filter((group) => group.hasNormalStock).length} min={1} />
      </div>
    </div>
  )
}

function ProductEditModal({ open, group, onClose, onSubmit }: {
  open: boolean
  group: InventoryProductGroup
  onClose: () => void
  onSubmit: (payload: UpdateProductRequest, imageFile: File | null) => Promise<void>
}) {
  const [name, setName] = useState(group.nombreProducto)
  const [imageMode, setImageMode] = useState<ImageMode>('url')
  const [imageUrl, setImageUrl] = useState(group.imageUrl ?? '')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imagePreviewUrl, setImagePreviewUrl] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => () => {
    if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl)
  }, [imagePreviewUrl])

  function clearUploadedImage() {
    if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl)
    setImageFile(null)
    setImagePreviewUrl('')
  }

  function handleImageModeChange(mode: ImageMode) {
    setImageMode(mode)
    setError('')
    if (mode === 'url') clearUploadedImage()
  }

  function handleImageFileChange(file: File | null) {
    setError('')
    clearUploadedImage()
    if (!file) return

    const nextError = imageFileError(file)
    if (nextError) {
      setError(nextError)
      return
    }

    setImageFile(file)
    setImagePreviewUrl(URL.createObjectURL(file))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const trimmedName = name.trim()
    const trimmedImageUrl = imageMode === 'url' ? imageUrl.trim() : (group.imageUrl ?? '')

    if (!trimmedName) {
      setError('El nombre del producto es obligatorio.')
      return
    }

    if (imageMode === 'url' && !isValidImageUrl(trimmedImageUrl)) {
      setError('Ingresa una URL http(s) valida o deja la imagen vacia.')
      return
    }

    if (imageMode === 'upload' && !imageFile) {
      setError('Selecciona una imagen JPG, PNG o WEBP.')
      return
    }

    setSaving(true)
    try {
      await onSubmit({ nombre: trimmedName, imageUrl: trimmedImageUrl || undefined }, imageMode === 'upload' ? imageFile : null)
    } finally {
      setSaving(false)
    }
  }

  const previewVariant = { categoria: group.categoria, imageUrl: imageMode === 'upload' ? imagePreviewUrl : imageUrl }
  const previewSrc = imageMode === 'upload' && imagePreviewUrl ? imagePreviewUrl : getVariantImage(previewVariant)

  return (
    <Modal open={open} title="Editar producto" onClose={onClose} size="lg">
      <form onSubmit={handleSubmit} className="grid gap-5">
        <section className="grid gap-4 lg:grid-cols-[240px_1fr]">
          <div className="overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)]">
            {previewSrc ? (
              <img src={previewSrc} alt="Vista previa del producto" className="h-52 w-full object-cover" />
            ) : (
              <div className="grid h-52 place-items-center text-[var(--color-muted)]">
                <ImageIcon size={34} />
              </div>
            )}
          </div>
          <div className="grid content-start gap-4">
            <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
              Nombre del producto
              <input
                value={name}
                onChange={(event) => setName(event.target.value)}
                maxLength={100}
                required
                className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
              />
            </label>

            <div className="flex flex-wrap gap-2" role="group" aria-label="Modo de imagen">
              <button
                type="button"
                onClick={() => handleImageModeChange('url')}
                className={cn(
                  'inline-flex min-h-10 items-center gap-2 rounded-[var(--radius-md)] px-3 text-sm font-semibold transition',
                  imageMode === 'url'
                    ? 'bg-[var(--color-primary)] text-white'
                    : 'border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-muted)] hover:text-[var(--color-text)]',
                )}
              >
                <LinkIcon size={16} />
                URL
              </button>
              <button
                type="button"
                onClick={() => handleImageModeChange('upload')}
                className={cn(
                  'inline-flex min-h-10 items-center gap-2 rounded-[var(--radius-md)] px-3 text-sm font-semibold transition',
                  imageMode === 'upload'
                    ? 'bg-[var(--color-primary)] text-white'
                    : 'border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-muted)] hover:text-[var(--color-text)]',
                )}
              >
                <Upload size={16} />
                Subir imagen
              </button>
            </div>

            {imageMode === 'url' ? (
              <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
                URL de imagen
                <input
                  value={imageUrl}
                  onChange={(event) => {
                    setError('')
                    setImageUrl(event.target.value)
                  }}
                  className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
                  placeholder="https://..."
                />
              </label>
            ) : (
              <label className="grid min-h-28 cursor-pointer place-items-center rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-bg)] p-4 text-center text-sm font-semibold text-[var(--color-text)] transition hover:bg-white">
                <Upload size={20} className="mb-2 text-[var(--color-muted)]" />
                {imageFile ? imageFile.name : 'Seleccionar archivo'}
                <span className="mt-1 block text-xs font-normal text-[var(--color-muted)]">JPG, PNG o WEBP hasta 5 MB</span>
                <input
                  type="file"
                  accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp"
                  className="sr-only"
                  onChange={(event) => handleImageFileChange(event.target.files?.[0] ?? null)}
                />
              </label>
            )}
          </div>
        </section>

        {error && <p className="rounded-lg bg-[var(--color-danger-soft)] px-3 py-2 text-sm font-semibold text-[var(--color-danger)]">{error}</p>}

        <div className="flex flex-col-reverse gap-2 border-t border-[var(--color-border)] pt-4 sm:flex-row sm:justify-end">
          <ActionButton type="button" variant="secondary" onClick={onClose}>Cancelar</ActionButton>
          <ActionButton type="submit" disabled={saving}>
            <Pencil size={17} />
            {saving ? 'Guardando...' : 'Guardar cambios'}
          </ActionButton>
        </div>
      </form>
    </Modal>
  )
}

function MiniStat({ label, value, danger = false }: { label: string; value: string; danger?: boolean }) {
  return (
    <div className="rounded-lg bg-[var(--color-surface)] px-2 py-1.5">
      <span className="block text-[10px] font-semibold uppercase tracking-[0.05em] text-[var(--color-muted)]">{label}</span>
      <strong className={cn('mt-0.5 block text-sm', danger && 'text-[var(--color-danger-foreground)]')}>{value}</strong>
    </div>
  )
}

function MovementModal({ open, title, variants, selectedVariantId, onClose, onSubmit }: {
  open: boolean
  title: string
  variants: Variant[]
  selectedVariantId: number | null
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}) {
  return (
    <Modal open={open} title={title} onClose={onClose}>
      <form onSubmit={onSubmit} className="grid gap-3">
        <select name="idVariante" defaultValue={selectedVariantId ?? undefined} className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2">
          {variants.map((variant) => (
            <option key={variant.idVariante} value={variant.idVariante}>{variant.sku} / {variant.nombreProducto} / {variant.talla} / {variant.color}</option>
          ))}
        </select>
        <input name="cantidad" type="number" min={1} required className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Cantidad a mover" />
        <ReasonField />
        <ActionButton type="submit">Guardar movimiento</ActionButton>
      </form>
    </Modal>
  )
}

function AdjustmentModal({ open, variants, selectedVariantId, onClose, onSubmit }: {
  open: boolean
  variants: Variant[]
  selectedVariantId: number | null
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}) {
  const groups = useMemo(() => buildInventoryGroups(variants), [variants])
  const initialVariant = variants.find((variant) => variant.idVariante === selectedVariantId) ?? variants[0]
  const [selectedProductId, setSelectedProductId] = useState(initialVariant?.idProducto ?? 0)
  const [selectedVariant, setSelectedVariant] = useState(initialVariant?.idVariante ?? 0)
  const [newStock, setNewStock] = useState(initialVariant ? String(initialVariant.stockActual) : '')

  const selectedGroup = groups.find((group) => group.idProducto === selectedProductId) ?? groups[0]
  const groupVariants = selectedGroup?.variants ?? []
  const currentVariant = variants.find((variant) => variant.idVariante === selectedVariant) ?? groupVariants[0]
  const location = currentVariant ? getStockLocation(currentVariant.idVariante) : null
  const available = currentVariant && location ? availableUnits(currentVariant, location) : 0
  const numericNewStock = Number(newStock)
  const difference = currentVariant && Number.isFinite(numericNewStock) ? numericNewStock - currentVariant.stockActual : 0

  function handleProductChange(idProducto: number) {
    const nextGroup = groups.find((group) => group.idProducto === idProducto)
    const nextVariant = nextGroup?.variants[0]
    setSelectedProductId(idProducto)
    if (nextVariant) {
      setSelectedVariant(nextVariant.idVariante)
      setNewStock(String(nextVariant.stockActual))
    }
  }

  function handleVariantChange(idVariante: number) {
    const nextVariant = variants.find((variant) => variant.idVariante === idVariante)
    setSelectedVariant(idVariante)
    if (nextVariant) setNewStock(String(nextVariant.stockActual))
  }

  function differenceLabel() {
    if (!currentVariant || !Number.isFinite(numericNewStock)) return 'sin cambios'
    if (difference === 0) return 'sin cambios'
    return difference > 0 ? `+${difference}` : String(difference)
  }

  return (
    <Modal open={open} title="Ajustar stock" onClose={onClose}>
      <form onSubmit={onSubmit} className="grid gap-4">
        <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
          Producto
          <select
            value={selectedProductId}
            onChange={(event) => handleProductChange(Number(event.target.value))}
            className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
          >
            {groups.map((group) => (
              <option key={group.idProducto} value={group.idProducto}>{group.nombreProducto}</option>
            ))}
          </select>
        </label>

        <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
          Variante
          <select
            name="idVariante"
            value={selectedVariant}
            onChange={(event) => handleVariantChange(Number(event.target.value))}
            className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
          >
            {groupVariants.map((variant) => (
              <option key={variant.idVariante} value={variant.idVariante}>
                {variant.sku} / {variant.color} / {variant.talla}
              </option>
            ))}
          </select>
        </label>

        {currentVariant && location && (
          <section className="grid gap-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-4 text-sm">
            <div>
              <p className="font-bold text-[var(--color-text)]">{currentVariant.nombreProducto}</p>
              <p className="text-xs font-semibold text-[var(--color-muted)]">{currentVariant.sku} / {currentVariant.color} / {currentVariant.talla}</p>
            </div>
            <div className="grid gap-2 sm:grid-cols-2">
              <Metric label="Ubicacion" value={`${location.pasillo}, ${location.contenedor}`} />
              <Metric label="Stock actual" value={String(currentVariant.stockActual)} />
              <Metric label="Asignado" value={String(location.asignado)} />
              <Metric label="Disponible" value={String(available)} />
              <Metric label="Entrante" value={location.entrante ? `+${location.entrante}` : '-'} />
              <Metric label="Diferencia" value={differenceLabel()} />
            </div>
          </section>
        )}

        <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
          Nuevo stock absoluto
          <input
            name="cantidad"
            type="number"
            min={0}
            required
            value={newStock}
            onChange={(event) => setNewStock(event.target.value)}
            className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
            placeholder="Nuevo stock absoluto"
          />
        </label>

        <ReasonField required />
        <ActionButton type="submit">Guardar ajuste</ActionButton>
      </form>
    </Modal>
  )
}

function ReasonField({ required = false }: { required?: boolean }) {
  const [selectedReason, setSelectedReason] = useState('')
  const [customReason, setCustomReason] = useState('')
  const isCustom = selectedReason === customReasonValue
  const reason = isCustom ? customReason.trim() : selectedReason

  return (
    <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
      Motivo
      <select
        value={selectedReason}
        onChange={(event) => {
          setSelectedReason(event.target.value)
          if (event.target.value !== customReasonValue) {
            setCustomReason('')
          }
        }}
        required={required && !isCustom}
        className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
      >
        <option value="">{required ? 'Selecciona un motivo' : 'Sin motivo especifico'}</option>
        {movementReasons.map((reasonOption) => (
          <option key={reasonOption} value={reasonOption}>{reasonOption}</option>
        ))}
        <option value={customReasonValue}>Otro...</option>
      </select>
      {isCustom && (
        <textarea
          value={customReason}
          onChange={(event) => setCustomReason(event.target.value)}
          required={required}
          maxLength={150}
          className="min-h-24 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
          placeholder="Describe el motivo"
          autoFocus
        />
      )}
      <input type="hidden" name="motivo" value={reason} />
    </label>
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

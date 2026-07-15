import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Image as ImageIcon, Link as LinkIcon, PackagePlus, Plus, SlidersHorizontal, Trash2, Upload } from 'lucide-react'
import type { CreateProductRequest, Variant } from '../domain/types'
import { groupVariantsByProduct, productGroupMatchesSearch, type ProductGroup } from '../domain/productGroups'
import { productsApi } from '../../../infra/api/productsApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { StockBadge } from '../../../shared/components/Badge'
import { Modal } from '../../../shared/components/Modal'
import { ProductVariantCard } from '../../../shared/components/ProductVariantCard'
import { SearchInput } from '../../../shared/components/SearchInput'
import { getVariantImage } from '../../../shared/utils/productImages'
import { cn } from '../../../shared/utils/cn'

const emptyProduct: CreateProductRequest = {
  nombre: '',
  categoria: '',
  marca: 'OMG MODA',
  imageUrl: '',
  variantes: [{ sku: '', talla: 'M', color: '', material: '', precioCosto: 0, precioVenta: 0 }],
}

const stockFilters = ['Todos', 'En stock', 'Bajo stock', 'Sin stock'] as const
type StockFilter = (typeof stockFilters)[number]
type GridColumns = 3 | 5
type ImageMode = 'url' | 'upload'

const maxImageSizeBytes = 5 * 1024 * 1024
const allowedImageExtensions = ['jpg', 'jpeg', 'png', 'webp']
const customOptionValue = '__custom__'
const baseSizeOptions = ['XS', 'S', 'M', 'L', 'XL', 'XXL']
const baseBrandOptions = ['OMG MODA']
const rowsPerCatalogPage = 5

function uniqueOptions(values: Array<string | null | undefined>, fallback: string[] = []) {
  return Array.from(new Set([...fallback, ...values]
    .map((value) => value?.trim() ?? '')
    .filter(Boolean)))
    .sort((a, b) => a.localeCompare(b, 'es'))
}

function groupMatchesStockFilter(group: ProductGroup, filter: StockFilter) {
  if (filter === 'Sin stock') return group.allOutOfStock
  if (filter === 'Bajo stock') return group.hasLowStock
  if (filter === 'En stock') return group.hasNormalStock
  return true
}

function buildPageNumbers(currentPage: number, totalPages: number) {
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }

  const pages: Array<number | 'ellipsis'> = [1]
  if (currentPage > 3) pages.push('ellipsis')

  const start = Math.max(2, currentPage - 1)
  const end = Math.min(totalPages - 1, currentPage + 1)

  for (let page = start; page <= end; page += 1) {
    pages.push(page)
  }

  if (currentPage < totalPages - 2) pages.push('ellipsis')
  pages.push(totalPages)

  return pages
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

function availableStock(variant: Pick<Variant, 'stockActual' | 'stockReservado' | 'stockDisponible'>) {
  return variant.stockDisponible ?? Math.max(variant.stockActual - (variant.stockReservado ?? 0), 0)
}

function formatPrice(value: number) {
  return `S/ ${value.toFixed(2)}`
}

function normalizeColorName(value: string) {
  return value.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase()
}

function colorSwatch(value: string) {
  const color = normalizeColorName(value)
  if (color.includes('multicolor') || color.includes('floral')) return 'conic-gradient(#111 0 20%, #1f9d55 0 40%, #d97706 0 60%, #e11d48 0 80%, #2563eb 0)'
  if (color.includes('blanco negro')) return 'linear-gradient(135deg, #fafafa 0 48%, #111 50% 100%)'
  if (color.includes('blanco') || color.includes('marfil') || color.includes('hueso') || color.includes('crema')) return '#f7f3e8'
  if (color.includes('negro')) return '#111111'
  if (color.includes('azul marino') || color.includes('azul oscuro') || color.includes('indigo')) return '#1d3557'
  if (color.includes('azul') || color.includes('celeste')) return '#4f9edb'
  if (color.includes('verde oliva')) return '#6f7f44'
  if (color.includes('verde') || color.includes('menta') || color.includes('esmeralda')) return '#2f9b67'
  if (color.includes('rosa')) return '#e9a6b3'
  if (color.includes('rojo') || color.includes('vino') || color.includes('borgona')) return '#9f1d35'
  if (color.includes('terracota')) return '#b85f42'
  if (color.includes('camel') || color.includes('arena') || color.includes('beige') || color.includes('caqui')) return '#c2a46d'
  if (color.includes('mostaza') || color.includes('champagne')) return '#d3a328'
  if (color.includes('lavanda')) return '#a78bfa'
  if (color.includes('chocolate')) return '#5a3825'
  if (color.includes('gris') || color.includes('grafito')) return '#777777'
  return '#b8b8b8'
}

function CreatableSelectField({ label, value, options, placeholder, newLabel, onChange, required = false }: {
  label: string
  value: string
  options: string[]
  placeholder: string
  newLabel: string
  onChange: (value: string) => void
  required?: boolean
}) {
  const [customMode, setCustomMode] = useState(false)
  const trimmedValue = value.trim()
  const valueInOptions = options.includes(trimmedValue)
  const selectValue = customMode || (trimmedValue && !valueInOptions) ? customOptionValue : trimmedValue

  return (
    <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
      {label}
      <select
        value={selectValue}
        onChange={(event) => {
          const nextValue = event.target.value
          if (nextValue === customOptionValue) {
            setCustomMode(true)
            onChange('')
            return
          }
          setCustomMode(false)
          onChange(nextValue)
        }}
        className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
        required={required && !customMode}
      >
        <option value="" disabled>{placeholder}</option>
        {options.map((option) => (
          <option key={option} value={option}>{option}</option>
        ))}
        <option value={customOptionValue}>{newLabel}</option>
      </select>
      {selectValue === customOptionValue && (
        <input
          className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal"
          placeholder={placeholder}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          required={required}
          autoFocus
        />
      )}
    </label>
  )
}

function ProductQuickViewModal({ group, currentIndex, total, onClose, onPrevious, onNext }: {
  group: ProductGroup
  currentIndex: number
  total: number
  onClose: () => void
  onPrevious: () => void
  onNext: () => void
}) {
  const initialVariant = group.variants[0]
  const [selectedVariantId, setSelectedVariantId] = useState(initialVariant.idVariante)
  const selectedVariant = group.variants.find((variant) => variant.idVariante === selectedVariantId) ?? initialVariant
  const colors = Array.from(new Set(group.variants.map((variant) => variant.color)))
    .sort((a, b) => a.localeCompare(b, 'es', { sensitivity: 'base' }))
  const sizes = Array.from(new Set(group.variants.map((variant) => variant.talla)))
    .sort((a, b) => a.localeCompare(b, 'es', { numeric: true, sensitivity: 'base' }))
  const selectedColor = selectedVariant.color
  const stock = availableStock(selectedVariant)
  const priceLabel = group.precioMin === group.precioMax
    ? formatPrice(group.precioMin)
    : `${formatPrice(group.precioMin)} - ${formatPrice(group.precioMax)}`
  const hasProductImage = Boolean(selectedVariant.imageUrl || group.imageUrl)
  const imageSrc = hasProductImage ? getVariantImage(selectedVariant) : ''

  function selectColor(color: string) {
    const nextVariant = group.variants.find((variant) => variant.color === color && variant.talla === selectedVariant.talla)
      ?? group.variants.find((variant) => variant.color === color)
    if (nextVariant) setSelectedVariantId(nextVariant.idVariante)
  }

  return (
    <Modal
      open
      title=""
      onClose={onClose}
      size="lg"
    >
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1.35fr)_340px]">
        <section className="relative overflow-hidden rounded-2xl bg-[#f7f7f7]">
          <div className="grid h-[320px] place-items-center p-4 sm:h-[420px] lg:h-[min(64vh,620px)]">
            {imageSrc ? (
              <img
                src={imageSrc}
                alt={group.nombreProducto}
                className="block h-full w-full object-contain"
              />
            ) : (
              <div className="grid h-full w-full place-items-center rounded-xl border border-dashed border-[var(--color-border)] text-[var(--color-muted)]">
                <div className="text-center">
                  <ImageIcon size={42} className="mx-auto" />
                  <p className="mt-2 text-sm font-semibold">Imagen no disponible</p>
                </div>
              </div>
            )}
          </div>

          <button
            type="button"
            onClick={onPrevious}
            className="absolute left-3 top-1/2 grid size-11 -translate-y-1/2 place-items-center rounded-full bg-white text-[var(--color-text)] shadow-[var(--shadow-card)] transition hover:scale-105 hover:shadow-[var(--shadow-float)]"
            aria-label="Producto anterior"
          >
            <ChevronLeft size={22} />
          </button>
          <button
            type="button"
            onClick={onNext}
            className="absolute right-3 top-1/2 grid size-11 -translate-y-1/2 place-items-center rounded-full bg-white text-[var(--color-text)] shadow-[var(--shadow-card)] transition hover:scale-105 hover:shadow-[var(--shadow-float)]"
            aria-label="Producto siguiente"
          >
            <ChevronRight size={22} />
          </button>
          <div className="absolute bottom-3 right-3 rounded-full bg-white/95 px-3 py-1 text-sm font-bold text-[var(--color-text)] shadow-[var(--shadow-card)]">
            {currentIndex + 1} / {total}
          </div>
        </section>

        <aside className="flex min-w-0 flex-col rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
          <div className="grid gap-3">
            <div className="min-w-0">
              <h3 className="text-xl font-black leading-tight text-[var(--color-text)]">{group.nombreProducto}</h3>
              <p className="mt-1 text-sm font-semibold text-[var(--color-muted)]">{group.categoria} / {group.marca}</p>
            </div>
            <div className="w-fit rounded-xl bg-[var(--color-bg)] px-3 py-2 text-sm font-bold text-[var(--color-text)]">
              {selectedVariant.sku}
            </div>
          </div>

          <div className="mt-5 grid gap-3">
            <InfoBlock label="Precios" value={priceLabel} />
            <div className="rounded-xl bg-[var(--color-bg)] p-3">
              <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Color seleccionado</p>
              <div className="mt-2 flex items-center gap-2">
                <span
                  className="block size-6 rounded-full border border-black/10"
                  style={{ background: colorSwatch(selectedColor) }}
                />
                <strong className="text-sm text-[var(--color-text)]">{selectedColor}</strong>
              </div>
            </div>
            <div className="rounded-xl bg-[var(--color-bg)] p-3">
              <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Colores disponibles</p>
              <div className="mt-2 flex flex-wrap gap-2">
                {colors.map((color) => (
                  <button
                    key={color}
                    type="button"
                    onClick={() => selectColor(color)}
                    className={cn(
                      'grid size-8 place-items-center rounded-full border transition',
                      color === selectedColor ? 'border-[var(--color-text)] ring-2 ring-black/15' : 'border-[var(--color-border)] hover:border-[var(--color-text)]',
                    )}
                    aria-label={`Ver color ${color}`}
                    title={color}
                  >
                    <span className="block size-5 rounded-full border border-black/10" style={{ background: colorSwatch(color) }} />
                  </button>
                ))}
              </div>
            </div>
            <div className="rounded-xl bg-[var(--color-bg)] p-3">
              <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Tallas disponibles</p>
              <div className="mt-2 flex flex-wrap gap-2">
                {sizes.map((size) => (
                  <span key={size} className="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-1.5 text-sm font-bold text-[var(--color-text)]">
                    {size}
                  </span>
                ))}
              </div>
            </div>
            <div className="rounded-xl bg-[var(--color-bg)] p-3">
              <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">Stock</p>
              <div className="mt-2 flex flex-wrap items-center gap-3">
                <StockBadge stock={stock} min={selectedVariant.stockMinimo} />
                <span className="text-sm font-semibold text-[var(--color-muted)]">
                  {stock} disponible / {selectedVariant.stockActual} fisico
                </span>
              </div>
            </div>
          </div>

        </aside>
      </div>
    </Modal>
  )
}

function InfoBlock({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl bg-[var(--color-bg)] p-3">
      <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">{label}</p>
      <p className="mt-2 text-base font-black text-[var(--color-text)]">{value}</p>
    </div>
  )
}

export function CatalogPage() {
  const [searchParams] = useSearchParams()
  const urlQuery = searchParams.get('q') ?? ''
  const [variants, setVariants] = useState<Variant[]>([])
  const [queryState, setQueryState] = useState(() => ({ source: urlQuery, value: urlQuery }))
  const [activeCategory, setActiveCategory] = useState('Todas')
  const [stockFilter, setStockFilter] = useState<StockFilter>('Todos')
  const [gridColumns, setGridColumns] = useState<GridColumns>(3)
  const [page, setPage] = useState(1)
  const [status, setStatus] = useState('Cargando catalogo desde backend.')
  const [modalOpen, setModalOpen] = useState(false)
  const [draft, setDraft] = useState<CreateProductRequest>(emptyProduct)
  const [imageMode, setImageMode] = useState<ImageMode>('url')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imagePreviewUrl, setImagePreviewUrl] = useState('')
  const [imageError, setImageError] = useState('')
  const [quickViewIndex, setQuickViewIndex] = useState<number | null>(null)
  const query = queryState.source === urlQuery ? queryState.value : urlQuery

  useEffect(() => {
    productsApi.listVariants()
      .then((data) => {
        setVariants(data)
        setStatus(data.length ? 'Catalogo conectado al backend.' : 'Backend conectado sin productos registrados.')
      })
      .catch(() => {
        setVariants([])
        setStatus('Backend no disponible. No se muestran datos mock.')
      })
  }, [])

  const productGroups = useMemo(() => groupVariantsByProduct(variants), [variants])
  const filtered = useMemo(() => productGroups.filter((group) =>
    productGroupMatchesSearch(group, query)
    && (activeCategory === 'Todas' || group.categoria === activeCategory)
    && groupMatchesStockFilter(group, stockFilter),
  ), [activeCategory, productGroups, query, stockFilter])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    const imageUrl = imageMode === 'url' ? (draft.imageUrl?.trim() ?? '') : ''

    if (imageMode === 'url' && !isValidImageUrl(imageUrl)) {
      setImageError('Ingresa una URL http(s) valida o deja el campo vacio.')
      return
    }

    if (imageMode === 'upload' && !imageFile) {
      setImageError('Selecciona una imagen JPG, PNG o WEBP.')
      return
    }

    const payload: CreateProductRequest = {
      ...draft,
      nombre: draft.nombre.trim(),
      categoria: draft.categoria.trim(),
      marca: draft.marca.trim(),
      imageUrl: imageUrl || undefined,
      variantes: draft.variantes.map((variant) => ({
        ...variant,
        sku: variant.sku?.trim() || undefined,
        talla: variant.talla.trim(),
        color: variant.color.trim(),
        material: variant.material?.trim() || undefined,
      })),
    }

    try {
      const created = await productsApi.createProduct(payload, imageMode === 'upload' ? imageFile : null)
      setVariants((current) => [...created, ...current])
      setStatus('Producto creado en backend.')
      closeCreateModal()
    } catch {
      setStatus('No se pudo crear el producto en backend. Revisa la conexion o los datos enviados.')
    }
  }

  const lowStock = productGroups.filter((group) => group.hasLowStock || group.allOutOfStock).length
  const categories = uniqueOptions(variants.map((variant) => variant.categoria))
  const brandOptions = uniqueOptions(variants.map((variant) => variant.marca), baseBrandOptions)
  const sizeOptions = uniqueOptions(variants.map((variant) => variant.talla), baseSizeOptions)
  const colorOptions = uniqueOptions(variants.map((variant) => variant.color))
  const pageSize = rowsPerCatalogPage * gridColumns
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const currentPage = Math.min(page, totalPages)
  const startIndex = (currentPage - 1) * pageSize
  const paginated = filtered.slice(startIndex, startIndex + pageSize)
  const resultStart = filtered.length ? startIndex + 1 : 0
  const resultEnd = Math.min(startIndex + pageSize, filtered.length)
  const visibleVariantCount = filtered.reduce((total, group) => total + group.variants.length, 0)
  const pageNumbers = buildPageNumbers(currentPage, totalPages)
  const quickViewProduct = quickViewIndex === null ? null : filtered[quickViewIndex] ?? null

  function updateQuery(value: string) {
    setQueryState({ source: urlQuery, value })
    setPage(1)
  }

  function openCreateModal() {
    setDraft(emptyProduct)
    setImageMode('url')
    setImageFile(null)
    setImagePreviewUrl('')
    setImageError('')
    setModalOpen(true)
  }

  function closeCreateModal({ preservePreview = false } = {}) {
    if (!preservePreview && imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl)
    }
    setModalOpen(false)
    setDraft(emptyProduct)
    setImageMode('url')
    setImageFile(null)
    setImagePreviewUrl('')
    setImageError('')
  }

  function updateVariant(index: number, patch: Partial<CreateProductRequest['variantes'][number]>) {
    setDraft((current) => ({
      ...current,
      variantes: current.variantes.map((variant, variantIndex) =>
        variantIndex === index ? { ...variant, ...patch } : variant,
      ),
    }))
  }

  function addVariant() {
    setDraft((current) => ({
      ...current,
      variantes: [
        ...current.variantes,
        { sku: '', talla: 'M', color: '', material: '', precioCosto: 0, precioVenta: 0 },
      ],
    }))
  }

  function removeVariant(index: number) {
    setDraft((current) => ({
      ...current,
      variantes: current.variantes.length === 1
        ? current.variantes
        : current.variantes.filter((_, variantIndex) => variantIndex !== index),
    }))
  }

  function clearUploadedImage({ revoke = true } = {}) {
    if (revoke && imagePreviewUrl) {
      URL.revokeObjectURL(imagePreviewUrl)
    }
    setImageFile(null)
    setImagePreviewUrl('')
  }

  function handleImageModeChange(mode: ImageMode) {
    setImageMode(mode)
    setImageError('')
    if (mode === 'url') {
      clearUploadedImage()
    } else {
      setDraft((current) => ({ ...current, imageUrl: '' }))
    }
  }

  function handleImageFileChange(file: File | null) {
    setImageError('')
    clearUploadedImage()
    if (!file) return

    const error = imageFileError(file)
    if (error) {
      setImageError(error)
      return
    }

    setImageFile(file)
    setImagePreviewUrl(URL.createObjectURL(file))
  }

  function openQuickView(group: ProductGroup) {
    const index = filtered.findIndex((item) => item.idProducto === group.idProducto)
    if (index >= 0) setQuickViewIndex(index)
  }

  function closeQuickView() {
    setQuickViewIndex(null)
  }

  function showPreviousProduct() {
    setQuickViewIndex((current) => {
      if (current === null || !filtered.length) return current
      return current === 0 ? filtered.length - 1 : current - 1
    })
  }

  function showNextProduct() {
    setQuickViewIndex((current) => {
      if (current === null || !filtered.length) return current
      return current === filtered.length - 1 ? 0 : current + 1
    })
  }

  return (
    <div className="page-grid">
      <section className="flex justify-end">
        <ActionButton type="button" onClick={openCreateModal} className="w-full sm:w-auto">
          <PackagePlus size={17} />
          Anadir producto
        </ActionButton>
      </section>

      <section className="flex min-w-0 flex-col gap-8 lg:flex-row">
        <details open className="responsive-filter-panel min-w-0 shrink-0 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 open:space-y-6 sm:rounded-2xl sm:p-5 lg:w-64 lg:space-y-6">
          <summary className="cursor-pointer list-none font-bold text-[var(--color-text)] marker:hidden">
            <span className="flex items-center justify-between gap-3"><span className="inline-flex items-center gap-2"><SlidersHorizontal size={17} /> Filtros del catalogo</span><span className="text-xs text-[var(--color-muted)]">Mostrar</span></span>
          </summary>
          <div>
            <div className="mb-3 flex items-center gap-2">
              <SlidersHorizontal size={17} />
              <h2 className="font-bold">Filtros</h2>
            </div>
            <SearchInput value={query} onChange={updateQuery} placeholder="Buscar producto" />
            {urlQuery && (
              <p className="mt-2 text-xs font-semibold text-[var(--color-text)]">
                Busqueda global aplicada: {urlQuery}
              </p>
            )}
            <p className="mt-2 text-xs text-[var(--color-muted)]">{status}</p>
          </div>
          <div>
            <p className="mb-2 text-sm font-bold">Categorias</p>
            <div className="flex flex-wrap gap-2 lg:flex-col">
              <button
                type="button"
                onClick={() => {
                  setActiveCategory('Todas')
                  setPage(1)
                }}
                className={cn(
                  'rounded-[var(--radius-md)] px-3 py-2 text-left text-sm font-semibold transition',
                  activeCategory === 'Todas'
                    ? 'bg-[var(--color-primary)] text-white'
                    : 'text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
                )}
              >
                Todas
              </button>
              {categories.map((category) => (
                <button
                  key={category}
                  type="button"
                  onClick={() => {
                    setActiveCategory(category)
                    setPage(1)
                  }}
                  className={cn(
                    'rounded-[var(--radius-md)] px-3 py-2 text-left text-sm font-semibold transition',
                    activeCategory === category
                      ? 'bg-[var(--color-primary-soft)] text-[var(--color-text)] ring-1 ring-inset ring-black/10'
                      : 'text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
                  )}
                >
                  {category}
                </button>
              ))}
            </div>
          </div>
          <div className="border-t border-[var(--color-border)] pt-4">
            <p className="text-sm font-bold">Resumen</p>
            <p className="mt-2 text-sm text-[var(--color-muted)]">{filtered.length} productos visibles</p>
            <p className="text-sm text-[var(--color-muted)]">{visibleVariantCount} variantes disponibles</p>
            <p className="text-sm text-[var(--color-muted)]">{lowStock} productos con alerta</p>
            {(activeCategory !== 'Todas' || stockFilter !== 'Todos' || query) && (
              <button
                type="button"
                onClick={() => {
                  setQueryState({ source: urlQuery, value: '' })
                  setActiveCategory('Todas')
                  setStockFilter('Todos')
                  setPage(1)
                }}
                className="mt-3 text-sm font-semibold text-[var(--color-text)] underline-offset-4 hover:underline"
              >
                Limpiar filtros
              </button>
            )}
          </div>
        </details>

        <div className="min-w-0 flex-1">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-[var(--color-muted)]">
              Mostrando {resultStart} - {resultEnd} de {filtered.length} productos
            </p>
            <div className="flex flex-wrap items-center gap-2">
              {stockFilters.map((chip) => (
                <button
                  key={chip}
                  type="button"
                  onClick={() => {
                    setStockFilter(chip)
                    setPage(1)
                  }}
                  className={cn(
                    'rounded-full px-3 py-1.5 text-sm font-semibold transition',
                    stockFilter === chip
                      ? 'bg-[var(--color-primary)] text-white'
                      : 'border border-[var(--color-border)] text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
                  )}
                >
                  {chip}
                </button>
              ))}
              <span className="mx-1 hidden h-6 w-px bg-[var(--color-border)] sm:block" />
              <span className="hidden sm:contents">{[3, 5].map((columns) => (
                <button
                  key={columns}
                  type="button"
                  aria-pressed={gridColumns === columns}
                  onClick={() => {
                    setGridColumns(columns as GridColumns)
                    setPage(1)
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
              ))}</span>
            </div>
          </div>

          <section className={cn(
            'min-w-0',
            gridColumns === 5
              ? 'grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5'
              : 'grid gap-4 md:grid-cols-2 xl:grid-cols-3',
          )}>
            {paginated.map((group) => (
              <ProductVariantCard key={group.idProducto} group={group} compact={gridColumns === 5} onView={openQuickView} />
            ))}
          </section>

          {paginated.length === 0 && (
            <section className="rounded-2xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface)] p-10 text-center">
              <p className="font-bold text-[var(--color-text)]">Sin productos para mostrar</p>
              <p className="mt-1 text-sm text-[var(--color-muted)]">Ajusta los filtros o registra un producto nuevo.</p>
            </section>
          )}

          <nav className="mt-6 flex flex-col gap-3 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 sm:flex-row sm:items-center sm:justify-between" aria-label="Paginacion de productos">
            <p className="text-sm font-semibold text-[var(--color-text)]">
              {resultStart} - {resultEnd} de {filtered.length} Resultados
            </p>
            <div className="flex items-center justify-center gap-1">
              <button
                type="button"
                disabled={currentPage === 1}
                onClick={() => setPage((current) => Math.max(1, current - 1))}
                className="grid size-9 place-items-center rounded-full text-[var(--color-muted)] transition hover:bg-[var(--color-bg)] disabled:opacity-35"
                aria-label="Pagina anterior"
              >
                <ChevronLeft size={18} />
              </button>
              {pageNumbers.map((pageNumber, index) => (
                pageNumber === 'ellipsis'
                  ? <span key={`ellipsis-${index}`} className="px-3 text-sm font-bold text-[var(--color-muted)]">...</span>
                  : (
                    <button
                      key={pageNumber}
                      type="button"
                      onClick={() => setPage(pageNumber)}
                      aria-current={currentPage === pageNumber ? 'page' : undefined}
                      className={cn(
                        'grid size-9 place-items-center rounded-full text-sm font-bold transition',
                        currentPage === pageNumber
                          ? 'bg-[var(--color-primary)] text-white'
                          : 'text-[var(--color-text)] hover:bg-[var(--color-bg)]',
                      )}
                    >
                      {pageNumber}
                    </button>
                  )
              ))}
              <button
                type="button"
                disabled={currentPage === totalPages}
                onClick={() => setPage((current) => Math.min(totalPages, current + 1))}
                className="grid size-9 place-items-center rounded-full text-[var(--color-muted)] transition hover:bg-[var(--color-bg)] disabled:opacity-35"
                aria-label="Pagina siguiente"
              >
                <ChevronRight size={18} />
              </button>
            </div>
          </nav>
        </div>
      </section>

      <Modal open={modalOpen} title="Anadir producto" onClose={() => closeCreateModal()} size="lg">
        <form onSubmit={handleCreate} className="grid gap-5">
          <section className="grid gap-3 sm:grid-cols-3">
            <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
              Nombre
              <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="Camisa Oxford" value={draft.nombre} onChange={(e) => setDraft({ ...draft, nombre: e.target.value })} required />
            </label>
            <CreatableSelectField
              label="Categoria"
              value={draft.categoria}
              options={categories}
              placeholder="Selecciona categoria"
              newLabel="Nueva categoria..."
              onChange={(categoria) => setDraft({ ...draft, categoria })}
              required
            />
            <CreatableSelectField
              label="Marca"
              value={draft.marca}
              options={brandOptions}
              placeholder="Selecciona marca"
              newLabel="Nueva marca..."
              onChange={(marca) => setDraft({ ...draft, marca })}
              required
            />
          </section>

          <section className="grid gap-4 rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] p-4 lg:grid-cols-[240px_1fr]">
            <div className="overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)]">
              {imageMode === 'url' && draft.imageUrl ? (
                <img src={draft.imageUrl} alt="Vista previa del producto" className="h-48 w-full object-cover" />
              ) : imageMode === 'upload' && imagePreviewUrl ? (
                <img src={imagePreviewUrl} alt="Vista previa del producto" className="h-48 w-full object-cover" />
              ) : (
                <div className="grid h-48 place-items-center text-[var(--color-muted)]">
                  <ImageIcon size={34} />
                </div>
              )}
            </div>
            <div className="grid content-start gap-3">
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
                  <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-2 font-normal" placeholder="https://..." value={draft.imageUrl ?? ''} onChange={(e) => {
                    setImageError('')
                    setDraft({ ...draft, imageUrl: e.target.value })
                  }} />
                </label>
              ) : (
                <label className="grid min-h-28 cursor-pointer place-items-center rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface)] p-4 text-center text-sm font-semibold text-[var(--color-text)] transition hover:bg-white">
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
              {imageError && <p className="rounded-lg bg-[var(--color-danger-soft)] px-3 py-2 text-sm font-semibold text-[var(--color-danger)]">{imageError}</p>}
            </div>
          </section>

          <section className="grid gap-3">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h3 className="font-bold text-[var(--color-text)]">Variantes</h3>
                <p className="text-sm text-[var(--color-muted)]">{draft.variantes.length} SKU por registrar</p>
              </div>
              <ActionButton type="button" variant="secondary" onClick={addVariant}>
                <Plus size={16} />
                Agregar variante
              </ActionButton>
            </div>

            <div className="grid gap-3">
              {draft.variantes.map((variant, index) => (
                <article key={index} className="grid min-w-0 gap-3 rounded-xl border border-[var(--color-border)] p-3">
                  <div className="grid min-w-0 gap-3 md:grid-cols-2 xl:grid-cols-[1fr_0.8fr_1fr_1fr_0.8fr_0.8fr_auto]">
                    <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
                      SKU
                      <input
                        className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal uppercase"
                        placeholder="Auto"
                        value={variant.sku ?? ''}
                        onChange={(e) => updateVariant(index, { sku: e.target.value })}
                      />
                      <span className="text-xs font-normal text-[var(--color-muted)]">Opcional; si se deja vacio se genera.</span>
                    </label>
                    <CreatableSelectField
                      label="Talla"
                      value={variant.talla}
                      options={sizeOptions}
                      placeholder="Selecciona talla"
                      newLabel="Nueva talla..."
                      onChange={(talla) => updateVariant(index, { talla })}
                      required
                    />
                    <CreatableSelectField
                      label="Color"
                      value={variant.color}
                      options={colorOptions}
                      placeholder="Selecciona color"
                      newLabel="Nuevo color..."
                      onChange={(color) => updateVariant(index, { color })}
                      required
                    />
                    <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
                      Material
                      <input className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="Material" value={variant.material ?? ''} onChange={(e) => updateVariant(index, { material: e.target.value })} />
                    </label>
                    <input className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Costo" type="number" min="0.01" step="0.01" value={variant.precioCosto || ''} onChange={(e) => updateVariant(index, { precioCosto: Number(e.target.value) })} required />
                    <input className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Venta" type="number" min="0.01" step="0.01" value={variant.precioVenta || ''} onChange={(e) => updateVariant(index, { precioVenta: Number(e.target.value) })} required />
                    <button
                      type="button"
                      disabled={draft.variantes.length === 1}
                      onClick={() => removeVariant(index)}
                      className="grid min-h-10 min-w-10 place-items-center rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 text-[var(--color-muted)] transition hover:bg-[var(--color-danger-soft)] hover:text-[var(--color-danger)] disabled:opacity-35"
                      aria-label="Eliminar variante"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <div className="flex flex-col-reverse gap-2 border-t border-[var(--color-border)] pt-4 sm:flex-row sm:justify-end">
            <ActionButton type="button" variant="secondary" onClick={() => closeCreateModal()}>
              Cancelar
            </ActionButton>
            <ActionButton type="submit">
              <PackagePlus size={17} />
              Agregar producto
            </ActionButton>
          </div>
        </form>
      </Modal>

      {quickViewProduct && (
        <ProductQuickViewModal
          key={quickViewProduct.idProducto}
          group={quickViewProduct}
          currentIndex={quickViewIndex ?? 0}
          total={filtered.length}
          onClose={closeQuickView}
          onPrevious={showPreviousProduct}
          onNext={showNextProduct}
        />
      )}
    </div>
  )
}

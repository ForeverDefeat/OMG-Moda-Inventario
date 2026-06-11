import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Image as ImageIcon, Link as LinkIcon, PackagePlus, Plus, SlidersHorizontal, Trash2, Upload } from 'lucide-react'
import type { CreateProductRequest, Variant } from '../domain/types'
import { productsApi } from '../../../infra/api/productsApi'
import { mockVariants } from '../../../infra/mock/mockData'
import { ActionButton } from '../../../shared/components/ActionButton'
import { Modal } from '../../../shared/components/Modal'
import { ProductPriceCard } from '../../../shared/components/ProductPriceCard'
import { SearchInput } from '../../../shared/components/SearchInput'
import { cn } from '../../../shared/utils/cn'

const emptyProduct: CreateProductRequest = {
  nombre: '',
  categoria: '',
  marca: 'OMG MODA',
  imageUrl: '',
  variantes: [{ talla: 'M', color: '', material: '', precioCosto: 0, precioVenta: 0 }],
}

const stockFilters = ['Todos', 'En stock', 'Bajo stock', 'Sin stock'] as const
type StockFilter = (typeof stockFilters)[number]
type GridColumns = 3 | 5
type ImageMode = 'url' | 'upload'

const maxImageSizeBytes = 5 * 1024 * 1024
const allowedImageExtensions = ['jpg', 'jpeg', 'png', 'webp']

function matchesStockFilter(variant: Variant, filter: StockFilter) {
  if (filter === 'Sin stock') return variant.stockActual <= 0
  if (filter === 'Bajo stock') return variant.stockActual > 0 && variant.stockActual <= variant.stockMinimo
  if (filter === 'En stock') return variant.stockActual > variant.stockMinimo
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

export function CatalogPage() {
  const [searchParams] = useSearchParams()
  const urlQuery = searchParams.get('q') ?? ''
  const [variants, setVariants] = useState<Variant[]>(mockVariants)
  const [queryState, setQueryState] = useState(() => ({ source: urlQuery, value: urlQuery }))
  const [activeCategory, setActiveCategory] = useState('Todas')
  const [stockFilter, setStockFilter] = useState<StockFilter>('Todos')
  const [gridColumns, setGridColumns] = useState<GridColumns>(3)
  const [page, setPage] = useState(1)
  const [status, setStatus] = useState('Usando datos de referencia hasta conectar backend.')
  const [modalOpen, setModalOpen] = useState(false)
  const [draft, setDraft] = useState<CreateProductRequest>(emptyProduct)
  const [imageMode, setImageMode] = useState<ImageMode>('url')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imagePreviewUrl, setImagePreviewUrl] = useState('')
  const [imageError, setImageError] = useState('')
  const query = queryState.source === urlQuery ? queryState.value : urlQuery

  useEffect(() => {
    productsApi.listVariants()
      .then((data) => {
        setVariants(data.length ? data : mockVariants)
        setStatus('Catalogo conectado al backend.')
      })
      .catch(() => setStatus('Backend no disponible: se muestran datos mock.'))
  }, [])

  const filtered = useMemo(() => {
    const value = query.toLowerCase()
    return variants.filter((variant) =>
      [variant.nombreProducto, variant.categoria, variant.color, variant.talla]
        .some((field) => field.toLowerCase().includes(value))
      && (activeCategory === 'Todas' || variant.categoria === activeCategory)
      && matchesStockFilter(variant, stockFilter),
    )
  }, [activeCategory, stockFilter, variants, query])

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
      imageUrl: imageUrl || undefined,
      variantes: draft.variantes.map((variant) => ({
        ...variant,
        material: variant.material?.trim() || undefined,
      })),
    }

    try {
      const created = await productsApi.createProduct(payload, imageMode === 'upload' ? imageFile : null)
      setVariants((current) => [...created, ...current])
      setStatus('Producto creado en backend.')
      closeCreateModal()
    } catch {
      const idProducto = Date.now()
      const localImageUrl = imageMode === 'upload' ? imagePreviewUrl : payload.imageUrl
      const mockCreated: Variant[] = payload.variantes.map((variant, index) => ({
        idVariante: idProducto + index,
        idProducto,
        nombreProducto: payload.nombre,
        categoria: payload.categoria,
        marca: payload.marca,
        talla: variant.talla,
        color: variant.color,
        material: variant.material,
        precioCosto: variant.precioCosto,
        precioVenta: variant.precioVenta,
        stockActual: 0,
        stockMinimo: 5,
        stockStatus: 'SIN_STOCK',
        imageUrl: localImageUrl,
      }))
      setVariants((current) => [...mockCreated, ...current])
      setStatus('Producto agregado localmente porque el backend no respondio.')
      closeCreateModal({ preservePreview: imageMode === 'upload' && Boolean(imagePreviewUrl) })
    }
  }

  const lowStock = variants.filter((variant) => variant.stockActual <= variant.stockMinimo).length
  const categories = Array.from(new Set(variants.map((variant) => variant.categoria)))
  const pageSize = gridColumns === 5 ? 10 : 6
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const currentPage = Math.min(page, totalPages)
  const startIndex = (currentPage - 1) * pageSize
  const paginated = filtered.slice(startIndex, startIndex + pageSize)
  const resultStart = filtered.length ? startIndex + 1 : 0
  const resultEnd = Math.min(startIndex + pageSize, filtered.length)
  const pageNumbers = buildPageNumbers(currentPage, totalPages)

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
        { talla: 'M', color: '', material: '', precioCosto: 0, precioVenta: 0 },
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

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Catalogo de Productos</h1>
          <p className="text-sm text-[var(--color-muted)]">Gestiona productos, variantes, precios e imagenes de referencia.</p>
        </div>
        <ActionButton type="button" onClick={openCreateModal}>
          <PackagePlus size={17} />
          Anadir producto
        </ActionButton>
      </section>

      <section className="flex flex-col gap-8 lg:flex-row">
        <aside className="flex shrink-0 flex-col gap-6 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 lg:w-64">
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
            <p className="mt-2 text-sm text-[var(--color-muted)]">{filtered.length} variantes visibles</p>
            <p className="text-sm text-[var(--color-muted)]">{lowStock} con stock bajo</p>
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
        </aside>

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
              <span className="mx-1 h-6 w-px bg-[var(--color-border)]" />
              {[3, 5].map((columns) => (
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
              ))}
            </div>
          </div>

          <section className={cn(
            gridColumns === 5
              ? 'grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5'
              : 'grid gap-4 md:grid-cols-2 xl:grid-cols-3',
          )}>
            {paginated.map((variant) => (
              <ProductPriceCard key={variant.idVariante} variant={variant} compact={gridColumns === 5} />
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
            <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
              Categoria
              <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="Camisas" value={draft.categoria} onChange={(e) => setDraft({ ...draft, categoria: e.target.value })} required />
            </label>
            <label className="grid gap-1.5 text-sm font-semibold text-[var(--color-text)]">
              Marca
              <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2 font-normal" placeholder="OMG MODA" value={draft.marca} onChange={(e) => setDraft({ ...draft, marca: e.target.value })} required />
            </label>
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
                  <div className="grid min-w-0 gap-3 md:grid-cols-2 xl:grid-cols-[0.7fr_1fr_1fr_0.8fr_0.8fr_auto]">
                    <input className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Talla" value={variant.talla} onChange={(e) => updateVariant(index, { talla: e.target.value })} required />
                    <input className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Color" value={variant.color} onChange={(e) => updateVariant(index, { color: e.target.value })} required />
                    <input className="min-w-0 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Material" value={variant.material ?? ''} onChange={(e) => updateVariant(index, { material: e.target.value })} />
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
    </div>
  )
}

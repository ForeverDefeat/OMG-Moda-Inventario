import type { FormEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import { PackagePlus, SlidersHorizontal } from 'lucide-react'
import type { CreateProductRequest, Variant } from '../domain/types'
import { productsApi } from '../../../infra/api/productsApi'
import { mockVariants } from '../../../infra/mock/mockData'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { Modal } from '../../../shared/components/Modal'
import { ProductPriceCard } from '../../../shared/components/ProductPriceCard'
import { SearchInput } from '../../../shared/components/SearchInput'
import { StockBadge } from '../../../shared/components/Badge'

const columns: Column<Variant>[] = [
  { key: 'producto', header: 'Producto', render: (row) => <span className="font-semibold">{row.nombreProducto}</span> },
  { key: 'categoria', header: 'Categoria', render: (row) => row.categoria },
  { key: 'variante', header: 'Variante', render: (row) => `${row.talla} / ${row.color}` },
  { key: 'precio', header: 'Precio', render: (row) => `S/ ${row.precioVenta.toFixed(2)}` },
  { key: 'stock', header: 'Stock', render: (row) => <StockBadge stock={row.stockActual} min={row.stockMinimo} /> },
]

const emptyProduct: CreateProductRequest = {
  nombre: '',
  categoria: '',
  marca: 'OMG MODA',
  variantes: [{ talla: 'M', color: '', material: '', precioCosto: 0, precioVenta: 0 }],
}

export function CatalogPage() {
  const [variants, setVariants] = useState<Variant[]>(mockVariants)
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('Usando datos de referencia hasta conectar backend.')
  const [modalOpen, setModalOpen] = useState(false)
  const [draft, setDraft] = useState<CreateProductRequest>(emptyProduct)

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
        .some((field) => field.toLowerCase().includes(value)),
    )
  }, [variants, query])

  async function handleCreate(event: FormEvent) {
    event.preventDefault()
    try {
      const created = await productsApi.createProduct(draft)
      setVariants((current) => [...created, ...current])
      setStatus('Producto creado en backend.')
      setModalOpen(false)
      setDraft(emptyProduct)
    } catch {
      const mockCreated: Variant = {
        idVariante: Date.now(),
        idProducto: Date.now(),
        nombreProducto: draft.nombre,
        categoria: draft.categoria,
        marca: draft.marca,
        talla: draft.variantes[0].talla,
        color: draft.variantes[0].color,
        material: draft.variantes[0].material,
        precioCosto: draft.variantes[0].precioCosto,
        precioVenta: draft.variantes[0].precioVenta,
        stockActual: 0,
        stockMinimo: 5,
        stockStatus: 'SIN_STOCK',
      }
      setVariants((current) => [mockCreated, ...current])
      setStatus('Producto agregado localmente porque el backend no respondio.')
      setModalOpen(false)
    }
  }

  const lowStock = variants.filter((variant) => variant.stockActual <= variant.stockMinimo).length
  const categories = Array.from(new Set(variants.map((variant) => variant.categoria)))

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Catalogo de Productos</h1>
          <p className="text-sm text-[var(--color-muted)]">Gestiona productos, variantes, precios e imagenes de referencia.</p>
        </div>
        <ActionButton onClick={() => setModalOpen(true)}>
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
            <SearchInput value={query} onChange={setQuery} placeholder="Buscar producto" />
            <p className="mt-2 text-xs text-[var(--color-muted)]">{status}</p>
          </div>
          <div>
            <p className="mb-2 text-sm font-bold">Categorias</p>
            <div className="flex flex-wrap gap-2 lg:flex-col">
              {categories.map((category) => (
                <button key={category} type="button" className="text-left text-sm text-[var(--color-muted)] transition hover:text-[var(--color-text)]">
                  {category}
                </button>
              ))}
            </div>
          </div>
          <div className="border-t border-[var(--color-border)] pt-4">
            <p className="text-sm font-bold">Resumen</p>
            <p className="mt-2 text-sm text-[var(--color-muted)]">{filtered.length} variantes visibles</p>
            <p className="text-sm text-[var(--color-muted)]">{lowStock} con stock bajo</p>
          </div>
        </aside>

        <div className="min-w-0 flex-1">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-[var(--color-muted)]">Mostrando {filtered.length} productos</p>
            <div className="flex flex-wrap gap-2">
              {['Todos', 'En stock', 'Bajo stock', 'Sin stock'].map((chip, index) => (
                <span key={chip} className={index === 0 ? 'rounded-full bg-[var(--color-primary)] px-3 py-1.5 text-sm font-medium text-white' : 'rounded-full border border-[var(--color-border)] px-3 py-1.5 text-sm font-medium text-[var(--color-muted)]'}>
                  {chip}
                </span>
              ))}
            </div>
          </div>

          <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {filtered.slice(0, 6).map((variant) => (
              <ProductPriceCard key={variant.idVariante} variant={variant} />
            ))}
          </section>

          <div className="mt-6">
            <DataTable rows={filtered} columns={columns} />
          </div>
        </div>
      </section>

      <Modal open={modalOpen} title="Anadir producto" onClose={() => setModalOpen(false)}>
        <form onSubmit={handleCreate} className="grid gap-3">
          <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Nombre" value={draft.nombre} onChange={(e) => setDraft({ ...draft, nombre: e.target.value })} required />
          <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Categoria" value={draft.categoria} onChange={(e) => setDraft({ ...draft, categoria: e.target.value })} required />
          <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Color" value={draft.variantes[0].color} onChange={(e) => setDraft({ ...draft, variantes: [{ ...draft.variantes[0], color: e.target.value }] })} required />
          <div className="grid gap-3 sm:grid-cols-3">
            <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Talla" value={draft.variantes[0].talla} onChange={(e) => setDraft({ ...draft, variantes: [{ ...draft.variantes[0], talla: e.target.value }] })} required />
            <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Costo" type="number" value={draft.variantes[0].precioCosto || ''} onChange={(e) => setDraft({ ...draft, variantes: [{ ...draft.variantes[0], precioCosto: Number(e.target.value) }] })} required />
            <input className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Venta" type="number" value={draft.variantes[0].precioVenta || ''} onChange={(e) => setDraft({ ...draft, variantes: [{ ...draft.variantes[0], precioVenta: Number(e.target.value) }] })} required />
          </div>
          <ActionButton type="submit">Guardar producto</ActionButton>
        </form>
      </Modal>
    </div>
  )
}

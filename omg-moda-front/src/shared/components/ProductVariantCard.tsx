import { ShoppingCart } from 'lucide-react'
import { useMemo, useState } from 'react'
import type { ProductGroup } from '../../features/catalog/domain/productGroups'
import { findVariantForSelection, selectInitialVariant } from '../../features/catalog/domain/productGroups'
import type { Variant } from '../../features/catalog/domain/types'
import { cn } from '../utils/cn'
import { getVariantImage } from '../utils/productImages'
import { ActionButton } from './ActionButton'
import { StockBadge } from './Badge'

const sizeOrder = ['XXS', 'XS', 'S', 'M', 'L', 'XL', 'XXL', 'U']

function uniqueSorted(values: string[]) {
  return Array.from(new Set(values)).sort((a, b) => {
    const left = sizeOrder.indexOf(a)
    const right = sizeOrder.indexOf(b)
    if (left >= 0 || right >= 0) return (left < 0 ? 99 : left) - (right < 0 ? 99 : right)
    return a.localeCompare(b, 'es', { numeric: true, sensitivity: 'base' })
  })
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

function formatPrice(value: number) {
  return `S/ ${value.toFixed(2)}`
}

export function ProductVariantCard({
  group,
  onAdd,
  onView,
  compact = false,
}: {
  group: ProductGroup
  onAdd?: (variant: Variant) => void
  onView?: (group: ProductGroup) => void
  compact?: boolean
}) {
  const initialVariant = useMemo(() => selectInitialVariant(group), [group])
  const [selectedVariantId, setSelectedVariantId] = useState(initialVariant.idVariante)
  const selectedVariant = group.variants.find((variant) => variant.idVariante === selectedVariantId) ?? initialVariant
  const selectedColor = selectedVariant.color

  const colors = uniqueSorted(group.variants.map((variant) => variant.color))
  const variantsForColor = group.variants.filter((variant) => variant.color === selectedColor)
  const sizes = uniqueSorted(variantsForColor.map((variant) => variant.talla))
  const hasPriceRange = group.precioMin !== group.precioMax

  function selectColor(color: string) {
    const colorVariants = group.variants.filter((variant) => variant.color === color)
    const sameSize = colorVariants.find((variant) => variant.talla === selectedVariant.talla)
    const nextVariant = sameSize
      ?? colorVariants.find((variant) => availableStock(variant) > variant.stockMinimo)
      ?? colorVariants.find((variant) => availableStock(variant) > 0)
      ?? colorVariants[0]

    if (nextVariant) setSelectedVariantId(nextVariant.idVariante)
  }

  function selectSize(talla: string) {
    const nextVariant = findVariantForSelection(group, selectedColor, talla)
    if (nextVariant) setSelectedVariantId(nextVariant.idVariante)
  }

  function openView() {
    if (onView) onView(group)
  }

  return (
    <article
      className={cn(
        'group flex h-full min-w-0 max-w-full flex-col overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] transition-colors hover:border-black/50',
        onView && 'cursor-pointer focus-within:border-black/50',
        compact ? 'shadow-sm' : 'dashboard-card',
      )}
      onClick={openView}
      onKeyDown={(event) => {
        if (!onView) return
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault()
          openView()
        }
      }}
      role={onView ? 'button' : undefined}
      tabIndex={onView ? 0 : undefined}
    >
      <div className={cn('relative shrink-0 overflow-hidden bg-[var(--color-bg)]', compact ? 'h-40 sm:h-44' : 'h-52 sm:h-56')}>
        <img
          src={getVariantImage(selectedVariant)}
          alt={group.nombreProducto}
          className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
        />
        <div className="absolute right-2 top-2 rounded-lg bg-white/90 px-2 py-1 text-sm font-bold text-[var(--color-text)] backdrop-blur">
          {formatPrice(selectedVariant.precioVenta)}
        </div>
        {availableStock(selectedVariant) <= 0 && (
          <div className="absolute inset-0 grid place-items-center bg-white/80">
            <span className="rounded-full bg-[var(--color-primary)] px-3 py-1 text-xs font-bold uppercase tracking-wider text-white">
              Sin stock
            </span>
          </div>
        )}
      </div>

      <div className={cn('flex min-w-0 flex-1 flex-col gap-3 p-4', compact && 'gap-2 p-3')}>
        <div>
          <h3 className="line-clamp-1 font-bold leading-tight text-[var(--color-text)]">{group.nombreProducto}</h3>
          <p className="line-clamp-1 text-xs font-semibold text-[var(--color-muted)]">{selectedVariant.sku}</p>
          <p className="line-clamp-1 text-sm text-[var(--color-muted)]">{group.categoria} / {group.marca}</p>
          {hasPriceRange && (
            <p className="mt-1 text-xs font-semibold text-[var(--color-muted)]">
              Rango {formatPrice(group.precioMin)} - {formatPrice(group.precioMax)}
            </p>
          )}
        </div>

        <div className="grid gap-2">
          <div className="flex flex-wrap items-center gap-1.5" aria-label="Colores disponibles">
            {colors.map((color) => {
              const colorVariants = group.variants.filter((variant) => variant.color === color)
              const colorHasStock = colorVariants.some((variant) => availableStock(variant) > 0)
              const selected = color === selectedColor

              return (
                <button
                  key={color}
                  type="button"
                  title={`${color}${colorHasStock ? '' : ' sin stock'}`}
                  aria-label={`Color ${color}`}
                  aria-pressed={selected}
                  onClick={(event) => {
                    event.stopPropagation()
                    selectColor(color)
                  }}
                  className={cn(
                    'grid size-7 place-items-center rounded-full border transition',
                    selected ? 'border-[var(--color-text)] ring-2 ring-black/15' : 'border-[var(--color-border)] hover:border-[var(--color-text)]',
                    !colorHasStock && 'opacity-35',
                  )}
                >
                  <span
                    className="block size-4 rounded-full border border-black/10"
                    style={{ background: colorSwatch(color) }}
                  />
                </button>
              )
            })}
          </div>

          <div className="flex flex-wrap gap-1.5" aria-label="Tallas disponibles">
            {sizes.map((talla) => {
              const variant = findVariantForSelection(group, selectedColor, talla)
              const selected = variant?.idVariante === selectedVariant.idVariante
              const hasStock = Boolean(variant && availableStock(variant) > 0)

              return (
                <button
                  key={talla}
                  type="button"
                  title={`${talla}${hasStock ? '' : ' sin stock'}`}
                  aria-pressed={selected}
                  onClick={(event) => {
                    event.stopPropagation()
                    selectSize(talla)
                  }}
                  className={cn(
                    'min-h-8 min-w-10 rounded-lg border px-2 text-xs font-bold transition',
                    selected
                      ? 'border-[var(--color-text)] bg-[var(--color-primary)] text-white'
                      : 'border-[var(--color-border)] bg-[var(--color-bg)] text-[var(--color-text)] hover:border-[var(--color-text)]',
                    !hasStock && 'opacity-40',
                  )}
                >
                  {talla}
                </button>
              )
            })}
          </div>
        </div>

        <div className="mt-auto flex min-w-0 flex-wrap items-center justify-between gap-3">
          <span className="min-w-0 break-words text-sm font-semibold text-[var(--color-muted)]">
            {availableStock(selectedVariant)} disponible / {selectedVariant.stockActual} fisico
          </span>
          <StockBadge stock={availableStock(selectedVariant)} min={selectedVariant.stockMinimo} />
        </div>

        {onAdd && (
          <ActionButton className="w-full" onClick={(event) => {
            event.stopPropagation()
            onAdd(selectedVariant)
          }} disabled={availableStock(selectedVariant) <= 0}>
            <ShoppingCart size={17} />
            Agregar
          </ActionButton>
        )}
      </div>
    </article>
  )
}

function availableStock(variant: { stockActual: number; stockReservado?: number; stockDisponible?: number }) {
  return variant.stockDisponible ?? Math.max(variant.stockActual - (variant.stockReservado ?? 0), 0)
}

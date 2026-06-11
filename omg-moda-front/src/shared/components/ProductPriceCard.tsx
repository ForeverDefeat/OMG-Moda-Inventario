import { ShoppingCart } from 'lucide-react'
import type { Variant } from '../../features/catalog/domain/types'
import { cn } from '../utils/cn'
import { getVariantImage } from '../utils/productImages'
import { ActionButton } from './ActionButton'
import { StockBadge } from './Badge'

export function ProductPriceCard({ variant, onAdd, compact = false }: { variant: Variant; onAdd?: (variant: Variant) => void; compact?: boolean }) {
  return (
    <article className={cn('group flex h-full flex-col overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] transition-colors hover:border-black/50', compact ? 'shadow-sm' : 'dashboard-card')}>
      <div className={cn('relative shrink-0 overflow-hidden bg-[var(--color-bg)]', compact ? 'h-40 sm:h-44' : 'h-52 sm:h-56')}>
        <img
          src={getVariantImage(variant)}
          alt={variant.nombreProducto}
          className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
        />
        <div className="absolute right-2 top-2 rounded-lg bg-white/90 px-2 py-1 text-sm font-bold text-[var(--color-text)] backdrop-blur">
          S/ {variant.precioVenta.toFixed(2)}
        </div>
        {variant.stockActual <= 0 && (
          <div className="absolute inset-0 grid place-items-center bg-white/80">
            <span className="rounded-full bg-[var(--color-primary)] px-3 py-1 text-xs font-bold uppercase tracking-wider text-white">
              Sin stock
            </span>
          </div>
        )}
      </div>
      <div className={cn('flex flex-1 flex-col gap-3 p-4', compact && 'gap-2 p-3')}>
        <div>
          <h3 className="line-clamp-1 font-bold leading-tight text-[var(--color-text)]">{variant.nombreProducto}</h3>
          <p className="line-clamp-1 text-sm text-[var(--color-muted)]">{variant.categoria} / {variant.talla} / {variant.color}</p>
        </div>
        <div className="mt-auto flex items-center justify-between gap-3">
          <span className="text-sm font-semibold text-[var(--color-muted)]">{variant.stockActual} en stock</span>
          <StockBadge stock={variant.stockActual} min={variant.stockMinimo} />
        </div>
        {onAdd && (
          <ActionButton className="w-full" onClick={() => onAdd(variant)}>
            <ShoppingCart size={17} />
            Agregar
          </ActionButton>
        )}
      </div>
    </article>
  )
}

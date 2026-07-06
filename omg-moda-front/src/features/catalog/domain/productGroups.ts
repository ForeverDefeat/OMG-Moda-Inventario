import type { Variant } from './types'

export interface ProductGroup {
  idProducto: number
  nombreProducto: string
  categoria: string
  marca: string
  imageUrl?: string
  variants: Variant[]
  stockTotal: number
  stockMinimoGrupo: number
  precioMin: number
  precioMax: number
  hasNormalStock: boolean
  hasLowStock: boolean
  allOutOfStock: boolean
}

export function groupVariantsByProduct(variants: Variant[]): ProductGroup[] {
  const grouped = new Map<number, Variant[]>()

  variants.forEach((variant) => {
    grouped.set(variant.idProducto, [...(grouped.get(variant.idProducto) ?? []), variant])
  })

  return Array.from(grouped.values()).map((items) => {
    const sortedVariants = [...items].sort((a, b) => a.idVariante - b.idVariante)
    const [first] = sortedVariants
    const stockTotal = sortedVariants.reduce((total, variant) => total + availableStock(variant), 0)
    const prices = sortedVariants.map((variant) => variant.precioVenta)

    return {
      idProducto: first.idProducto,
      nombreProducto: first.nombreProducto,
      categoria: first.categoria,
      marca: first.marca,
      imageUrl: first.imageUrl,
      variants: sortedVariants,
      stockTotal,
      stockMinimoGrupo: sortedVariants.reduce((total, variant) => total + variant.stockMinimo, 0),
      precioMin: Math.min(...prices),
      precioMax: Math.max(...prices),
      hasNormalStock: sortedVariants.some((variant) => availableStock(variant) > variant.stockMinimo),
      hasLowStock: sortedVariants.some((variant) => availableStock(variant) > 0 && availableStock(variant) <= variant.stockMinimo),
      allOutOfStock: sortedVariants.every((variant) => availableStock(variant) <= 0),
    }
  })
}

export function selectInitialVariant(group: ProductGroup) {
  return group.variants.find((variant) => availableStock(variant) > variant.stockMinimo)
    ?? group.variants.find((variant) => availableStock(variant) > 0)
    ?? group.variants[0]
}

function availableStock(variant: { stockActual: number; stockReservado?: number; stockDisponible?: number }) {
  return variant.stockDisponible ?? Math.max(variant.stockActual - (variant.stockReservado ?? 0), 0)
}

export function findVariantForSelection(group: ProductGroup, color: string, talla: string) {
  return group.variants.find((variant) => variant.color === color && variant.talla === talla)
}

export function productGroupMatchesSearch(group: ProductGroup, query: string) {
  const value = query.trim().toLowerCase()
  if (!value) return true

  return [
    group.nombreProducto,
    group.categoria,
    group.marca,
    ...group.variants.flatMap((variant) => [variant.sku, variant.color, variant.talla, variant.material ?? '']),
  ].some((field) => field.toLowerCase().includes(value))
}

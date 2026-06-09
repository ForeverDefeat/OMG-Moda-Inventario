import type { Variant } from '../../features/catalog/domain/types'

const categoryImages: Record<string, string> = {
  camisas: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&q=80',
  pantalones: 'https://images.unsplash.com/photo-1542272604-780c8d4bb9f3?w=400&q=80',
  tops: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400&q=80',
  sacos: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=400&q=80',
  vestidos: 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=400&q=80',
  faldas: 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=400&q=80',
}

export function getVariantImage(variant: Pick<Variant, 'categoria' | 'imageUrl'>) {
  const key = variant.categoria.toLowerCase()
  return variant.imageUrl ?? categoryImages[key] ?? 'https://images.unsplash.com/photo-1529374255404-311a2a4f1fd9?w=400&q=80'
}

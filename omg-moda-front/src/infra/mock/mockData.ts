import type { Variant } from '../../features/catalog/domain/types'
import type { Customer } from '../../features/customers/domain/types'
import type { PurchaseSuggestion } from '../../features/purchase-orders/domain/types'
import type { SaleResponse } from '../../features/sales/domain/types'

export const mockVariants: Variant[] = [
  {
    idVariante: 1,
    idProducto: 1,
    nombreProducto: 'Camisa Oxford',
    categoria: 'Camisas',
    marca: 'OMG MODA',
    talla: 'M',
    color: 'Azul',
    material: 'Algodon',
    precioCosto: 45,
    precioVenta: 89.9,
    stockActual: 45,
    stockMinimo: 8,
    stockStatus: 'NORMAL',
    imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&q=80',
  },
  {
    idVariante: 2,
    idProducto: 2,
    nombreProducto: 'Blazer Ejecutivo',
    categoria: 'Sacos',
    marca: 'OMG MODA',
    talla: 'L',
    color: 'Negro',
    material: 'Lino',
    precioCosto: 120,
    precioVenta: 249.9,
    stockActual: 7,
    stockMinimo: 10,
    stockStatus: 'BAJO_STOCK',
    imageUrl: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=400&q=80',
  },
  {
    idVariante: 3,
    idProducto: 3,
    nombreProducto: 'Pantalon Palazzo',
    categoria: 'Pantalones',
    marca: 'OMG MODA',
    talla: 'S',
    color: 'Beige',
    material: 'Viscosa',
    precioCosto: 58,
    precioVenta: 119.9,
    stockActual: 22,
    stockMinimo: 6,
    stockStatus: 'NORMAL',
    imageUrl: 'https://images.unsplash.com/photo-1542272604-780c8d4bb9f3?w=400&q=80',
  },
  {
    idVariante: 4,
    idProducto: 4,
    nombreProducto: 'Vestido Midi',
    categoria: 'Vestidos',
    marca: 'OMG MODA',
    talla: 'M',
    color: 'Terracota',
    material: 'Rayon',
    precioCosto: 82,
    precioVenta: 169.9,
    stockActual: 0,
    stockMinimo: 5,
    stockStatus: 'SIN_STOCK',
    imageUrl: 'https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=400&q=80',
  },
  {
    idVariante: 5,
    idProducto: 5,
    nombreProducto: 'Top Satinado',
    categoria: 'Tops',
    marca: 'OMG MODA',
    talla: 'S',
    color: 'Marfil',
    material: 'Saten',
    precioCosto: 34,
    precioVenta: 79.9,
    stockActual: 34,
    stockMinimo: 10,
    stockStatus: 'NORMAL',
    imageUrl: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400&q=80',
  },
  {
    idVariante: 6,
    idProducto: 6,
    nombreProducto: 'Falda Plisada',
    categoria: 'Faldas',
    marca: 'OMG MODA',
    talla: 'M',
    color: 'Verde',
    material: 'Poliester',
    precioCosto: 40,
    precioVenta: 99.9,
    stockActual: 12,
    stockMinimo: 8,
    stockStatus: 'NORMAL',
    imageUrl: 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=400&q=80',
  },
]

export const mockSales: SaleResponse[] = [
  {
    idVenta: 101,
    idUsuario: 2,
    estado: 'COMPLETADA',
    metodoPago: 'EFECTIVO',
    fecha: '2026-06-03T10:30:00',
    detalles: [{ idDetalle: 1, idVariante: 1, cantidad: 2, precioUnitario: 89.9, subtotal: 179.8 }],
    total: 179.8,
  },
  {
    idVenta: 102,
    idUsuario: 2,
    estado: 'COMPLETADA',
    metodoPago: 'YAPE',
    fecha: '2026-06-03T12:20:00',
    detalles: [{ idDetalle: 2, idVariante: 5, cantidad: 1, precioUnitario: 79.9, subtotal: 79.9 }],
    total: 79.9,
  },
]

export const mockCustomers: Customer[] = [
  { id: 1, nombre: 'Mariela Torres', correo: 'mariela@email.com', telefono: '999 120 554', segmento: 'VIP', totalCompras: 1860, ultimaCompra: '2026-06-01' },
  { id: 2, nombre: 'Andrea Rojas', correo: 'andrea@email.com', telefono: '988 771 042', segmento: 'Frecuente', totalCompras: 920, ultimaCompra: '2026-05-30' },
  { id: 3, nombre: 'Lucia Benavides', correo: 'lucia@email.com', telefono: '977 113 898', segmento: 'Nuevo', totalCompras: 169.9, ultimaCompra: '2026-06-03' },
  { id: 4, nombre: 'Carolina Vega', correo: 'carolina@email.com', telefono: '955 803 210', segmento: 'VIP', totalCompras: 2420, ultimaCompra: '2026-05-29' },
]

export const mockPurchaseSuggestions: PurchaseSuggestion[] = [
  { id: 1, producto: 'Blazer Ejecutivo Negro L', proveedor: 'Textiles Sur', cantidadSugerida: 24, costoEstimado: 2880, prioridad: 'Alta', motivo: 'Stock bajo y rotacion alta' },
  { id: 2, producto: 'Vestido Midi Terracota M', proveedor: 'Moda Lima', cantidadSugerida: 18, costoEstimado: 1476, prioridad: 'Alta', motivo: 'Sin stock en POS' },
  { id: 3, producto: 'Top Satinado Marfil S', proveedor: 'Atelier Norte', cantidadSugerida: 16, costoEstimado: 544, prioridad: 'Media', motivo: 'Campania fin de semana' },
  { id: 4, producto: 'Falda Plisada Verde M', proveedor: 'Import Moda', cantidadSugerida: 10, costoEstimado: 400, prioridad: 'Baja', motivo: 'Reposicion preventiva' },
]

export const trendData = [
  { name: 'Lun', value: 820 },
  { name: 'Mar', value: 1240 },
  { name: 'Mie', value: 980 },
  { name: 'Jue', value: 1480 },
  { name: 'Vie', value: 1920 },
  { name: 'Sab', value: 2410 },
]

export const categoryData = [
  { name: 'Camisas', value: 34 },
  { name: 'Vestidos', value: 24 },
  { name: 'Sacos', value: 18 },
  { name: 'Tops', value: 15 },
]

export const reportBars = [
  { name: 'Ene', value: 38 },
  { name: 'Feb', value: 44 },
  { name: 'Mar', value: 51 },
  { name: 'Abr', value: 47 },
  { name: 'May', value: 63 },
  { name: 'Jun', value: 69 },
]

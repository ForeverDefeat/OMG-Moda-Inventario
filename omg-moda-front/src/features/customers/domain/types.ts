export interface Customer {
  id: number
  nombre: string
  correo: string
  telefono: string
  segmento: 'VIP' | 'Frecuente' | 'Nuevo'
  totalCompras: number
  ultimaCompra: string
}

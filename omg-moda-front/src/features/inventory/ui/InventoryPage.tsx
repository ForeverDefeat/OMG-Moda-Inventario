import type { FormEvent } from 'react'
import { useState } from 'react'
import { ClipboardCheck, PackageCheck, PackagePlus, RotateCcw } from 'lucide-react'
import type { RegisterAdjustmentRequest, RegisterEntryRequest } from '../domain/types'
import type { Variant } from '../../catalog/domain/types'
import { inventoryApi } from '../../../infra/api/inventoryApi'
import { mockVariants } from '../../../infra/mock/mockData'
import { ActionButton } from '../../../shared/components/ActionButton'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { KpiCard } from '../../../shared/components/KpiCard'
import { Modal } from '../../../shared/components/Modal'
import { StockBadge } from '../../../shared/components/Badge'

const columns: Column<Variant>[] = [
  { key: 'producto', header: 'Producto / SKU', render: (row) => <span className="font-semibold">{row.nombreProducto}</span> },
  { key: 'categoria', header: 'Categoria', render: (row) => row.categoria },
  { key: 'stock', header: 'Stock', render: (row) => row.stockActual },
  { key: 'estado', header: 'Estado', render: (row) => <StockBadge stock={row.stockActual} min={row.stockMinimo} /> },
  { key: 'costo', header: 'Valor costo', render: (row) => `S/ ${(row.precioCosto * row.stockActual).toFixed(2)}` },
]

export function InventoryPage() {
  const [variants, setVariants] = useState(mockVariants)
  const [entryOpen, setEntryOpen] = useState(false)
  const [adjustOpen, setAdjustOpen] = useState(false)
  const [message, setMessage] = useState('Listo para registrar movimientos.')

  const totalUnits = variants.reduce((total, variant) => total + variant.stockActual, 0)
  const totalValue = variants.reduce((total, variant) => total + variant.stockActual * variant.precioCosto, 0)

  async function submitEntry(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const payload: RegisterEntryRequest = {
      idVariante: Number(form.get('idVariante')),
      cantidad: Number(form.get('cantidad')),
      motivo: String(form.get('motivo') ?? ''),
    }
    try {
      await inventoryApi.registerEntry(payload)
      setMessage('Entrada registrada en backend.')
    } catch {
      setMessage('Entrada aplicada localmente porque el backend no respondio.')
    }
    setVariants((current) => current.map((variant) => variant.idVariante === payload.idVariante
      ? { ...variant, stockActual: variant.stockActual + payload.cantidad }
      : variant))
    setEntryOpen(false)
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
      await inventoryApi.registerAdjustment(payload)
      setMessage('Ajuste registrado en backend.')
    } catch {
      setMessage('Ajuste aplicado localmente porque el backend no respondio.')
    }
    setVariants((current) => current.map((variant) => variant.idVariante === payload.idVariante
      ? { ...variant, stockActual: payload.cantidad }
      : variant))
    setAdjustOpen(false)
  }

  return (
    <div className="page-grid">
      <section className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="panel-title">Gestion de Stock</h1>
          <p className="text-sm text-[var(--color-muted)]">Controla entradas, ajustes y disponibilidad operativa.</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <ActionButton variant="secondary" onClick={() => setEntryOpen(true)}><PackagePlus size={17} /> Transferir stock</ActionButton>
          <ActionButton onClick={() => setAdjustOpen(true)}><RotateCcw size={17} /> Ajustar stock</ActionButton>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <KpiCard label="Valor total de inventario" value={`S/ ${totalValue.toLocaleString('es-PE')}`} icon={PackageCheck} />
        <KpiCard label="Unidades disponibles" value={String(totalUnits)} icon={ClipboardCheck} tone="success" />
        <KpiCard label="Productos en alerta" value={String(variants.filter((variant) => variant.stockActual <= variant.stockMinimo).length)} icon={RotateCcw} tone="warning" />
        <KpiCard label="Movimientos hoy" value="12" icon={PackagePlus} tone="primary" />
      </section>

      <section className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5">
        <div>
          <h2 className="text-lg font-bold">Inventario operativo</h2>
          <p className="text-sm text-[var(--color-muted)]">{message}</p>
        </div>
      </section>

      <DataTable rows={variants} columns={columns} />

      <MovementModal open={entryOpen} title="Registrar entrada" variants={variants} onClose={() => setEntryOpen(false)} onSubmit={submitEntry} />
      <MovementModal open={adjustOpen} title="Registrar ajuste" variants={variants} onClose={() => setAdjustOpen(false)} onSubmit={submitAdjustment} adjustment />
    </div>
  )
}

function MovementModal({ open, title, variants, onClose, onSubmit, adjustment = false }: {
  open: boolean
  title: string
  variants: Variant[]
  onClose: () => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  adjustment?: boolean
}) {
  return (
    <Modal open={open} title={title} onClose={onClose}>
      <form onSubmit={onSubmit} className="grid gap-3">
        <select name="idVariante" className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2">
          {variants.map((variant) => (
            <option key={variant.idVariante} value={variant.idVariante}>{variant.nombreProducto} / {variant.talla} / {variant.color}</option>
          ))}
        </select>
        <input name="cantidad" type="number" min={adjustment ? 0 : 1} required className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder={adjustment ? 'Nuevo stock absoluto' : 'Cantidad a ingresar'} />
        <textarea name="motivo" required={adjustment} className="min-h-24 rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-2" placeholder="Motivo u observacion" />
        <ActionButton type="submit">{adjustment ? 'Guardar ajuste' : 'Guardar entrada'}</ActionButton>
      </form>
    </Modal>
  )
}

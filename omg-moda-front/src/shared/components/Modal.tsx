import type { ReactNode } from 'react'
import { X } from 'lucide-react'
import { IconButton } from './IconButton'

export function Modal({ open, title, children, onClose }: {
  open: boolean
  title: string
  children: ReactNode
  onClose: () => void
}) {
  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/35 p-4">
      <section className="w-full max-w-xl rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-[var(--shadow-float)]">
        <header className="flex items-center justify-between border-b border-[var(--color-border)] px-5 py-4">
          <h2 className="text-lg font-bold text-[var(--color-text)]">{title}</h2>
          <IconButton label="Cerrar" icon={X} onClick={onClose} />
        </header>
        <div className="p-5">{children}</div>
      </section>
    </div>
  )
}

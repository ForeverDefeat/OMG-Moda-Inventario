import type { ReactNode } from 'react'
import { X } from 'lucide-react'
import { cn } from '../utils/cn'
import { IconButton } from './IconButton'

interface DrawerProps {
  title: string
  isOpen: boolean
  onClose: () => void
  children: ReactNode
  side?: 'left' | 'right'
}

export function Drawer({ title, isOpen, onClose, children, side = 'right' }: DrawerProps) {
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50">
      <button
        aria-label="Cerrar panel"
        className="absolute inset-0 bg-black/35"
        onClick={onClose}
        type="button"
      />
      <aside
        className={cn(
          'absolute top-0 h-full w-[min(420px,92vw)] overflow-y-auto bg-[var(--color-surface)] p-5 shadow-[var(--shadow-float)]',
          side === 'right' ? 'right-0' : 'left-0',
        )}
      >
        <div className="mb-5 flex items-center justify-between gap-3">
          <h2 className="text-lg font-bold text-[var(--color-text)]">{title}</h2>
          <IconButton label="Cerrar" icon={X} onClick={onClose} />
        </div>
        {children}
      </aside>
    </div>
  )
}

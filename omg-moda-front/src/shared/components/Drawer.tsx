import { useEffect, type ReactNode } from 'react'
import { X } from 'lucide-react'
import { cn } from '../utils/cn'
import { IconButton } from './IconButton'

interface DrawerProps {
  title: string
  isOpen: boolean
  onClose: () => void
  children: ReactNode
  side?: 'left' | 'right'
  size?: 'md' | 'lg'
}

export function Drawer({ title, isOpen, onClose, children, side = 'right', size = 'md' }: DrawerProps) {
  useEffect(() => {
    if (!isOpen) return
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isOpen, onClose])

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
        role="dialog"
        aria-modal="true"
        aria-labelledby="drawer-title"
        className={cn(
          'absolute top-0 flex h-full w-full flex-col overflow-hidden bg-[var(--color-surface)] shadow-[var(--shadow-float)] sm:w-auto',
          size === 'lg' ? 'sm:w-[min(560px,94vw)]' : 'sm:w-[min(420px,92vw)]',
          side === 'right' ? 'right-0' : 'left-0',
        )}
      >
        <div className="flex items-center justify-between gap-3 border-b border-[var(--color-border)] px-4 pb-3 pt-[max(12px,env(safe-area-inset-top))] sm:px-5 sm:py-4">
          <h2 id="drawer-title" className="text-lg font-bold text-[var(--color-text)]">{title}</h2>
          <IconButton label="Cerrar" icon={X} onClick={onClose} />
        </div>
        <div className="min-h-0 flex-1 overflow-y-auto px-4 pb-[max(20px,env(safe-area-inset-bottom))] pt-4 sm:px-5">{children}</div>
      </aside>
    </div>
  )
}

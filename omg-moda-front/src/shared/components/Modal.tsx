import { useEffect, type MouseEvent, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { X } from 'lucide-react'
import { IconButton } from './IconButton'
import { cn } from '../utils/cn'

export function Modal({ open, title, subtitle, children, onClose, size = 'md', closeOnOverlayClick = true }: {
  open: boolean
  title: string
  subtitle?: string
  children: ReactNode
  onClose: () => void
  size?: 'md' | 'lg'
  closeOnOverlayClick?: boolean
}) {
  useEffect(() => {
    if (!open) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [open])

  useEffect(() => {
    if (!open) return

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [onClose, open])

  if (!open) return null

  function handleOverlayClick(event: MouseEvent<HTMLDivElement>) {
    if (closeOnOverlayClick && event.target === event.currentTarget) {
      onClose()
    }
  }

  return createPortal(
    <div className="fixed inset-0 z-50 grid place-items-center overflow-y-auto bg-black/45 p-0 sm:p-4" onMouseDown={handleOverlayClick}>
      <section className={cn(
        'flex h-[100dvh] w-full flex-col overflow-hidden bg-[var(--color-surface)] shadow-[var(--shadow-float)] sm:h-auto sm:max-h-[calc(100vh-2rem)] sm:rounded-2xl sm:border sm:border-[var(--color-border)]',
        size === 'lg' ? 'max-w-4xl' : 'max-w-xl',
      )} role="dialog" aria-modal="true" aria-labelledby="modal-title">
        <header className={cn('flex shrink-0 items-center justify-between px-4 pb-3 pt-[max(12px,env(safe-area-inset-top))] sm:px-5 sm:py-4', title && 'border-b border-[var(--color-border)]')}>
          {title ? (
            <div className="min-w-0">
              <h2 id="modal-title" className="text-lg font-bold text-[var(--color-text)]">{title}</h2>
              {subtitle && <p className="mt-0.5 text-sm text-[var(--color-muted)]">{subtitle}</p>}
            </div>
          ) : <span id="modal-title" className="sr-only">Modal</span>}
          <IconButton label="Cerrar" icon={X} onClick={onClose} />
        </header>
        <div className="min-h-0 flex-1 overflow-x-hidden overflow-y-auto p-4 pb-[max(20px,env(safe-area-inset-bottom))] sm:max-h-[calc(100vh-7rem)] sm:p-5">{children}</div>
      </section>
    </div>,
    document.body,
  )
}

import type { LucideIcon } from 'lucide-react'
import { cn } from '../utils/cn'

interface IconButtonProps {
  label: string
  icon: LucideIcon
  onClick?: () => void
  className?: string
  type?: 'button' | 'submit'
}

export function IconButton({ label, icon: Icon, onClick, className, type = 'button' }: IconButtonProps) {
  return (
    <button
      type={type}
      aria-label={label}
      title={label}
      onClick={onClick}
      className={cn(
        'grid size-10 place-items-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-muted)] transition hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
        className,
      )}
    >
      <Icon size={18} />
    </button>
  )
}

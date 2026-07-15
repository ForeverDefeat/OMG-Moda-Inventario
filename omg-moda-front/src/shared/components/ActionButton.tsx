import type { ButtonHTMLAttributes, ReactNode } from 'react'
import { cn } from '../utils/cn'

interface ActionButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
}

const variants = {
  primary: 'bg-[var(--color-primary)] text-[var(--color-primary-foreground)] hover:bg-[var(--color-primary-strong)]',
  secondary: 'border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-text)] hover:bg-[var(--color-bg)]',
  danger: 'bg-[var(--color-danger)] text-white hover:bg-[#b3261e]',
  ghost: 'text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)]',
}

export function ActionButton({ children, className, variant = 'primary', ...props }: ActionButtonProps) {
  return (
    <button
      {...props}
      className={cn(
        'inline-flex min-h-11 items-center justify-center gap-2 rounded-[var(--radius-md)] px-4 text-sm font-semibold transition sm:min-h-10',
        variants[variant],
        className,
      )}
    >
      {children}
    </button>
  )
}

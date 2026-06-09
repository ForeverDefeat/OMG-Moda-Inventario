import type { ReactNode } from 'react'
import { cn } from '../utils/cn'

type Tone = 'success' | 'warning' | 'danger' | 'neutral' | 'primary'

const tones: Record<Tone, string> = {
  success: 'bg-[var(--color-success-soft)] text-[var(--color-success-foreground)]',
  warning: 'bg-[var(--color-warning-soft)] text-[var(--color-warning-foreground)]',
  danger: 'bg-[var(--color-danger-soft)] text-[var(--color-danger-foreground)]',
  neutral: 'bg-[var(--color-bg)] text-[var(--color-muted)]',
  primary: 'bg-[var(--color-primary)] text-white',
}

export function Badge({ children, tone = 'neutral' }: { children: ReactNode; tone?: Tone }) {
  return (
    <span className={cn('inline-flex items-center rounded-full px-2.5 py-1 text-xs font-bold', tones[tone])}>
      {children}
    </span>
  )
}

export function StockBadge({ stock, min }: { stock: number; min: number }) {
  if (stock <= 0) return <Badge tone="danger">Sin stock</Badge>
  if (stock <= min) return <Badge tone="warning">Stock bajo</Badge>
  return <Badge tone="success">En stock</Badge>
}

export function StatusBadge({ status }: { status: string }) {
  const normalized = status.toLowerCase()
  if (normalized.includes('alta') || normalized.includes('anulada') || normalized.includes('sin')) {
    return <Badge tone="danger">{status}</Badge>
  }
  if (normalized.includes('media') || normalized.includes('pendiente') || normalized.includes('bajo')) {
    return <Badge tone="warning">{status}</Badge>
  }
  if (normalized.includes('vip') || normalized.includes('completada') || normalized.includes('frecuente')) {
    return <Badge tone="success">{status}</Badge>
  }
  return <Badge tone="primary">{status}</Badge>
}

export function RoleBadge({ role }: { role: string }) {
  return <Badge tone={role.toUpperCase() === 'ADMIN' ? 'primary' : 'neutral'}>{role}</Badge>
}

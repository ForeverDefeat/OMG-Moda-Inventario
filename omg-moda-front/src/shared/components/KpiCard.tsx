import type { LucideIcon } from 'lucide-react'
import { ArrowUpRight } from 'lucide-react'
import { cn } from '../utils/cn'

interface KpiCardProps {
  label: string
  value: string
  helper?: string
  icon: LucideIcon
  tone?: 'primary' | 'success' | 'warning' | 'danger'
}

const tones = {
  primary: 'bg-[var(--color-primary)] text-white',
  success: 'bg-[var(--color-success-soft)] text-[var(--color-success)]',
  warning: 'bg-[var(--color-warning-soft)] text-[var(--color-warning)]',
  danger: 'bg-[var(--color-danger-soft)] text-[var(--color-danger)]',
}

export function KpiCard({ label, value, helper, icon: Icon, tone = 'primary' }: KpiCardProps) {
  return (
    <article className="dashboard-card flex min-h-[132px] flex-col gap-3 p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-sm font-medium text-[var(--color-muted)]">{label}</p>
          <p className="mt-2 truncate text-2xl font-bold tracking-tight text-[var(--color-text)]">{value}</p>
        </div>
        <div className={cn('grid size-10 shrink-0 place-items-center rounded-lg', tones[tone])}>
          <Icon size={20} />
        </div>
      </div>
      {helper && (
        <p className="mt-auto inline-flex items-center gap-1 text-xs font-semibold text-[var(--color-muted)]">
          <ArrowUpRight size={14} />
          {helper}
        </p>
      )}
    </article>
  )
}

import type { ReactNode } from 'react'

export function ChartCard({ title, subtitle, actions, children }: {
  title: string
  subtitle?: string
  actions?: ReactNode
  children: ReactNode
}) {
  return (
    <section className="dashboard-card p-5">
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-lg font-bold text-[var(--color-text)]">{title}</h2>
          {subtitle && <p className="text-sm text-[var(--color-muted)]">{subtitle}</p>}
        </div>
        {actions && <div className="shrink-0">{actions}</div>}
      </div>
      <div className="h-[260px] min-h-0">{children}</div>
    </section>
  )
}

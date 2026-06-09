import type { ReactNode } from 'react'

export function ChartCard({ title, subtitle, children }: { title: string; subtitle?: string; children: ReactNode }) {
  return (
    <section className="dashboard-card p-5">
      <div className="mb-4">
        <h2 className="text-lg font-bold text-[var(--color-text)]">{title}</h2>
        {subtitle && <p className="text-sm text-[var(--color-muted)]">{subtitle}</p>}
      </div>
      <div className="h-[260px] min-h-0">{children}</div>
    </section>
  )
}

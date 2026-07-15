import type { ReactNode } from 'react'

export function FilterBar({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col gap-3 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-3 sm:rounded-2xl sm:p-4 md:flex-row md:items-center md:justify-between">
      {children}
    </div>
  )
}

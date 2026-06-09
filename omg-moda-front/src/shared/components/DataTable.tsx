import type { ReactNode } from 'react'
import { cn } from '../utils/cn'

export interface Column<T> {
  key: string
  header: string
  render: (row: T) => ReactNode
  className?: string
}

export function DataTable<T>({ columns, rows, emptyText = 'Sin datos' }: {
  columns: Column<T>[]
  rows: T[]
  emptyText?: string
}) {
  return (
    <div className="overflow-hidden rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)]">
      <div className="overflow-x-auto">
        <table className="min-w-[720px]">
          <thead className="bg-[var(--color-bg)] text-left text-xs uppercase tracking-[0.08em] text-[var(--color-muted)]">
            <tr>
              {columns.map((column) => (
                <th key={column.key} className={cn('px-4 py-3 font-semibold', column.className)}>
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--color-border)] bg-[var(--color-surface)] text-sm">
            {rows.map((row, index) => (
              <tr key={index} className="transition hover:bg-[var(--color-bg)]">
                {columns.map((column) => (
                  <td key={column.key} className={cn('px-4 py-3 align-middle', column.className)}>
                    {column.render(row)}
                  </td>
                ))}
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td colSpan={columns.length} className="px-4 py-8 text-center text-[var(--color-muted)]">
                  {emptyText}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

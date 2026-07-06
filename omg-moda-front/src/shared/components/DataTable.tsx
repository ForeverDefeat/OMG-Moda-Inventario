import { useMemo, useState, type ReactNode } from 'react'
import { cn } from '../utils/cn'

export interface Column<T> {
  key: string
  header: string
  render: (row: T) => ReactNode
  className?: string
  sortable?: boolean
  sortValue?: (row: T) => string | number
  sortType?: 'text' | 'number'
}

type SortDirection = 'asc' | 'desc'

interface SortState {
  key: string
  direction: SortDirection
}

function defaultSortValue<T>(row: T, key: string) {
  if (typeof row === 'object' && row !== null && key in row) {
    return String((row as Record<string, unknown>)[key] ?? '')
  }
  return ''
}

function compareValues(left: string | number, right: string | number, sortType: 'text' | 'number') {
  if (sortType === 'number') {
    return Number(left) - Number(right)
  }

  return String(left).localeCompare(String(right), 'es', {
    sensitivity: 'base',
    numeric: true,
  })
}

export function DataTable<T>({ columns, rows, emptyText = 'Sin datos', maxHeight }: {
  columns: Column<T>[]
  rows: T[]
  emptyText?: string
  maxHeight?: string
}) {
  const [sortState, setSortState] = useState<SortState | null>(null)

  const sortedRows = useMemo(() => {
    if (!sortState) return rows

    const column = columns.find((item) => item.key === sortState.key)
    if (!column?.sortable) return rows

    const direction = sortState.direction === 'asc' ? 1 : -1
    const sortType = column.sortType ?? 'text'

    return rows
      .map((row, index) => ({ row, index }))
      .sort((left, right) => {
        const leftValue = column.sortValue?.(left.row) ?? defaultSortValue(left.row, column.key)
        const rightValue = column.sortValue?.(right.row) ?? defaultSortValue(right.row, column.key)
        const comparison = compareValues(leftValue, rightValue, sortType)
        return comparison === 0 ? left.index - right.index : comparison * direction
      })
      .map((item) => item.row)
  }, [columns, rows, sortState])

  function toggleSort(column: Column<T>) {
    if (!column.sortable) return

    setSortState((current) => {
      if (current?.key === column.key) {
        return {
          key: column.key,
          direction: current.direction === 'asc' ? 'desc' : 'asc',
        }
      }

      return { key: column.key, direction: 'asc' }
    })
  }

  function ariaSort(column: Column<T>) {
    if (!column.sortable || sortState?.key !== column.key) return 'none'
    return sortState.direction === 'asc' ? 'ascending' : 'descending'
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)]">
      <div className={cn(maxHeight ? 'overflow-auto' : 'overflow-x-auto')} style={maxHeight ? { maxHeight } : undefined}>
        <table className="min-w-[720px]">
          <thead className="bg-[var(--color-bg)] text-left text-xs uppercase tracking-[0.08em] text-[var(--color-muted)]">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  aria-sort={ariaSort(column)}
                  className={cn(
                    'px-4 py-3 font-semibold',
                    maxHeight && 'sticky top-0 z-10 bg-[var(--color-bg)]',
                    column.className,
                  )}
                >
                  {column.sortable ? (
                    <button
                      type="button"
                      onClick={() => toggleSort(column)}
                      className="flex w-full items-center gap-2 text-left text-xs font-semibold uppercase tracking-[0.08em] text-[var(--color-muted)] transition hover:text-[var(--color-text)]"
                    >
                      <span>{column.header}</span>
                      <span className="text-[10px]" aria-hidden="true">
                        {sortState?.key === column.key ? (sortState.direction === 'asc' ? '↑' : '↓') : '↕'}
                      </span>
                    </button>
                  ) : (
                    column.header
                  )}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--color-border)] bg-[var(--color-surface)] text-sm">
            {sortedRows.map((row, index) => (
              <tr key={index} className="transition hover:bg-[var(--color-bg)]">
                {columns.map((column) => (
                  <td key={column.key} className={cn('px-4 py-3 align-middle', column.className)}>
                    {column.render(row)}
                  </td>
                ))}
              </tr>
            ))}
            {sortedRows.length === 0 && (
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

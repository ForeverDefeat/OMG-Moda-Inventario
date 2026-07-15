import { useMemo, useState, type ReactNode } from 'react'
import { ArrowDown, ArrowUp } from 'lucide-react'
import { cn } from '../utils/cn'

export interface Column<T> {
  key: string
  header: string
  render: (row: T) => ReactNode
  className?: string
  sortable?: boolean
  sortValue?: (row: T) => string | number
  sortType?: 'text' | 'number'
  mobile?: {
    label?: string
    role?: 'title' | 'field' | 'actions'
    hidden?: boolean
  }
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

  const visibleMobileColumns = columns.filter((column) => !column.mobile?.hidden)
  const titleColumn = visibleMobileColumns.find((column) => column.mobile?.role === 'title')
    ?? visibleMobileColumns.find((column) => column.mobile?.role !== 'actions')
  const fieldColumns = visibleMobileColumns.filter((column) => {
    if (column === titleColumn) return false
    const inferredActions = /^acciones?$/i.test(column.header.trim())
    return column.mobile?.role !== 'actions' && !inferredActions
  })
  const actionColumns = visibleMobileColumns.filter((column) => {
    const inferredActions = /^acciones?$/i.test(column.header.trim())
    return column.mobile?.role === 'actions' || inferredActions
  })
  const sortableColumns = columns.filter((column) => column.sortable)

  return (
    <div className="min-w-0">
      <div className="grid gap-3 md:hidden">
        {sortableColumns.length > 0 && rows.length > 1 && (
          <div className="flex items-end gap-2 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-3">
            <label className="min-w-0 flex-1 text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">
              Ordenar por
              <select
                value={sortState?.key ?? ''}
                onChange={(event) => setSortState(event.target.value ? { key: event.target.value, direction: 'asc' } : null)}
                className="mt-1 min-h-11 w-full rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] px-3 text-sm font-semibold normal-case tracking-normal text-[var(--color-text)]"
              >
                <option value="">Orden original</option>
                {sortableColumns.map((column) => <option key={column.key} value={column.key}>{column.mobile?.label ?? column.header}</option>)}
              </select>
            </label>
            <button
              type="button"
              disabled={!sortState}
              onClick={() => setSortState((current) => current ? { ...current, direction: current.direction === 'asc' ? 'desc' : 'asc' } : current)}
              className="grid size-11 shrink-0 place-items-center rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-muted)]"
              aria-label={sortState?.direction === 'desc' ? 'Orden descendente; cambiar a ascendente' : 'Orden ascendente; cambiar a descendente'}
            >
              {sortState?.direction === 'desc' ? <ArrowDown size={18} /> : <ArrowUp size={18} />}
            </button>
          </div>
        )}

        {sortedRows.map((row, index) => (
          <article key={index} className="overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-[var(--shadow-card)]">
            {titleColumn && (
              <div className="border-b border-[var(--color-border)] bg-[var(--color-bg)] px-4 py-3 text-sm font-bold text-[var(--color-text)]">
                {titleColumn.render(row)}
              </div>
            )}
            {fieldColumns.length > 0 && (
              <dl className="grid gap-3 px-4 py-3">
                {fieldColumns.map((column) => (
                  <div key={column.key} className="grid grid-cols-[minmax(90px,.8fr)_minmax(0,1.2fr)] items-start gap-3 text-sm">
                    <dt className="text-xs font-bold uppercase tracking-[0.06em] text-[var(--color-muted)]">{column.mobile?.label ?? column.header}</dt>
                    <dd className="min-w-0 text-right font-medium text-[var(--color-text)] [overflow-wrap:anywhere]">{column.render(row)}</dd>
                  </div>
                ))}
              </dl>
            )}
            {actionColumns.map((column) => (
              <div key={column.key} className="border-t border-[var(--color-border)] px-4 py-3 [&>*]:w-full">
                {column.render(row)}
              </div>
            ))}
          </article>
        ))}

        {sortedRows.length === 0 && (
          <div className="rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-8 text-center text-sm text-[var(--color-muted)]">
            {emptyText}
          </div>
        )}
      </div>

      <div className="hidden overflow-hidden rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] md:block">
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
    </div>
  )
}

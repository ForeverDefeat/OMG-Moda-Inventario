import { cn } from '../utils/cn'

export function Tabs<T extends string>({ value, options, onChange }: {
  value: T
  options: Array<{ label: string; value: T }>
  onChange: (value: T) => void
}) {
  return (
    <div className="inline-flex rounded-[var(--radius-md)] border border-[var(--color-border)] bg-white p-1">
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          onClick={() => onChange(option.value)}
          className={cn(
            'rounded-[var(--radius-sm)] px-3 py-1.5 text-sm font-semibold transition',
            option.value === value
              ? 'bg-[var(--color-primary)] text-white'
              : 'text-[var(--color-muted)] hover:bg-[var(--color-bg-soft)]',
          )}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}

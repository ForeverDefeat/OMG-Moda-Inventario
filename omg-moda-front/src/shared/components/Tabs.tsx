import { cn } from '../utils/cn'

export function Tabs<T extends string>({ value, options, onChange }: {
  value: T
  options: Array<{ label: string; value: T }>
  onChange: (value: T) => void
}) {
  return (
    <div className="flex w-full overflow-x-auto rounded-[var(--radius-md)] border border-[var(--color-border)] bg-white p-1 sm:inline-flex sm:w-auto">
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          onClick={() => onChange(option.value)}
          className={cn(
            'min-h-10 flex-1 whitespace-nowrap rounded-[var(--radius-sm)] px-3 py-1.5 text-sm font-semibold transition sm:flex-none',
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

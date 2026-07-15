import { Search } from 'lucide-react'

export function SearchInput({ value, onChange, placeholder }: {
  value: string
  onChange: (value: string) => void
  placeholder: string
}) {
  return (
    <label className="flex min-h-11 w-full min-w-0 items-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-3 text-sm sm:min-h-10">
      <Search size={16} className="text-[var(--color-subtle)]" />
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className="ml-2 min-w-0 flex-1 bg-transparent outline-none placeholder:text-[var(--color-subtle)]"
      />
    </label>
  )
}

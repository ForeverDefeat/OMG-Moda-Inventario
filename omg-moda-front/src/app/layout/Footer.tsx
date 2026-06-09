export function Footer() {
  return (
    <footer className="border-t border-[var(--color-border)] bg-[var(--color-surface)] px-6 py-8 text-xs text-[var(--color-muted)]">
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <span className="font-bold text-[var(--color-text)]">ClothWise</span>
        <span>Helping retailers manage stock smarter.</span>
      </div>
    </footer>
  )
}

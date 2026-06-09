const colors = [
  'var(--color-chart-1)',
  'var(--color-chart-2)',
  'var(--color-chart-3)',
  'var(--color-chart-4)',
]

export function DonutMetricChart({ data }: { data: Array<{ name: string; value: number }> }) {
  const total = data.reduce((sum, item) => sum + item.value, 0) || 1
  const size = 240
  const center = size / 2
  const radius = 78
  const circumference = 2 * Math.PI * radius
  const segments = data.reduce<Array<{ item: { name: string; value: number }; dash: number; offset: number; color: string }>>((current, item, index) => {
    const previous = current.at(-1)
    const offset = previous ? previous.offset + previous.dash : 0
    const dash = (item.value / total) * circumference
    return [...current, { item, dash, offset, color: colors[index % colors.length] }]
  }, [])

  return (
    <div className="flex h-full items-center justify-center gap-6">
      <svg viewBox={`0 0 ${size} ${size}`} className="h-full max-h-[220px] w-auto" role="img" aria-label="Grafica de dona">
        <circle cx={center} cy={center} r={radius} fill="none" stroke="var(--color-border)" strokeWidth="26" />
        {segments.map((segment) => (
            <circle
              key={segment.item.name}
              cx={center}
              cy={center}
              r={radius}
              fill="none"
              stroke={segment.color}
              strokeDasharray={`${segment.dash} ${circumference - segment.dash}`}
              strokeDashoffset={-segment.offset}
              strokeLinecap="round"
              strokeWidth="26"
              transform={`rotate(-90 ${center} ${center})`}
            />
        ))}
        <text x={center} y={center - 4} textAnchor="middle" fill="var(--color-text)" fontSize="28" fontWeight="700">
          {total}
        </text>
        <text x={center} y={center + 20} textAnchor="middle" fill="var(--color-muted)" fontSize="12">
          total
        </text>
      </svg>
      <div className="hidden min-w-32 space-y-2 text-sm md:block">
        {data.map((item, index) => (
          <div key={item.name} className="flex items-center justify-between gap-3">
            <span className="flex items-center gap-2 text-[var(--color-muted)]">
              <span className="size-2 rounded-full" style={{ background: colors[index % colors.length] }} />
              {item.name}
            </span>
            <strong>{item.value}</strong>
          </div>
        ))}
      </div>
    </div>
  )
}

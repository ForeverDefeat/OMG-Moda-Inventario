const colors = [
  'var(--color-chart-1)',
  'var(--color-chart-2)',
  'var(--color-chart-3)',
  'var(--color-chart-4)',
]

interface DonutMetricChartProps {
  data: Array<{ name: string; value: number }>
  valueFormatter?: (value: number) => string
}

function defaultFormatter(value: number) {
  return value.toLocaleString('es-PE', { maximumFractionDigits: value >= 100 ? 0 : 1 })
}

function compactLegendData(data: Array<{ name: string; value: number }>, valueFormatter: (value: number) => string) {
  if (data.length <= 5) return data

  const visible = data.slice(0, 5)
  const hidden = data.slice(5)
  const othersValue = hidden.reduce((sum, item) => sum + item.value, 0)

  return [
    ...visible,
    {
      name: 'Otros',
      value: othersValue,
      title: hidden.map((item) => `${item.name}: ${valueFormatter(item.value)}`).join(', '),
    },
  ]
}

export function DonutMetricChart({ data, valueFormatter = defaultFormatter }: DonutMetricChartProps) {
  const rawTotal = data.reduce((sum, item) => sum + item.value, 0)
  const total = rawTotal || 1
  const size = 240
  const center = size / 2
  const radius = 78
  const circumference = 2 * Math.PI * radius
  const compactData = compactLegendData(data, valueFormatter)
  const segments = compactData.reduce<Array<{ item: { name: string; value: number; title?: string }; dash: number; offset: number; color: string }>>((current, item, index) => {
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
            >
              <title>{segment.item.title ?? `${segment.item.name}: ${valueFormatter(segment.item.value)}`}</title>
            </circle>
        ))}
        <text x={center} y={center - 4} textAnchor="middle" fill="var(--color-text)" fontSize="20" fontWeight="700">
          {valueFormatter(rawTotal)}
        </text>
        <text x={center} y={center + 20} textAnchor="middle" fill="var(--color-muted)" fontSize="12">
          total
        </text>
      </svg>
      <div className="hidden min-w-32 space-y-2 text-sm md:block">
        {compactData.map((item, index) => (
          <div key={item.name} className="flex items-center justify-between gap-3" title={'title' in item ? item.title : undefined}>
            <span className="flex items-center gap-2 text-[var(--color-muted)]">
              <span className="size-2 rounded-full" style={{ background: colors[index % colors.length] }} />
              {item.name}
            </span>
            <strong>{valueFormatter(item.value)}</strong>
          </div>
        ))}
      </div>
    </div>
  )
}

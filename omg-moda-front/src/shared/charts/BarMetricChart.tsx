interface BarMetricChartProps {
  data: Array<Record<string, string | number>>
  valueFormatter?: (value: number) => string
}

const colors = [
  'var(--color-chart-2)',
  'var(--color-chart-3)',
  'var(--color-chart-4)',
  'var(--color-chart-1)',
]

export function BarMetricChart({ data, valueFormatter = (value) => String(value) }: BarMetricChartProps) {
  const width = 640
  const height = 240
  const padding = 42
  const values = data.map((item) => Number(item.value))
  const max = Math.max(...values, 1)
  const gap = 18
  const barWidth = (width - padding * 2 - gap * Math.max(data.length - 1, 0)) / Math.max(data.length, 1)
  const ticks = [0, 0.25, 0.5, 0.75, 1].map((ratio) => {
    const value = max - max * ratio
    const y = padding + ratio * (height - padding * 2)
    return { value, y }
  })

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="h-full w-full" role="img" aria-label="Grafica de barras">
      {ticks.map((tick) => (
        <line
          key={tick.y}
          x1={padding}
          x2={width - padding}
          y1={tick.y}
          y2={tick.y}
          stroke="var(--color-border)"
          strokeWidth="1"
        />
      ))}
      {ticks.map((tick) => (
        <text key={`label-${tick.y}`} x={padding - 10} y={tick.y + 4} textAnchor="end" fill="var(--color-muted)" fontSize="11">
          {valueFormatter(Math.round(tick.value))}
        </text>
      ))}
      {data.map((item, index) => {
        const value = Number(item.value)
        const barHeight = ((height - padding * 2) * value) / max
        const x = padding + index * (barWidth + gap)
        const y = height - padding - barHeight
        const label = String(item.name)

        return (
          <g key={label}>
            <rect x={x} y={y} width={barWidth} height={barHeight} rx="8" fill={colors[index % colors.length]}>
              <title>{`${label}: ${valueFormatter(value)}`}</title>
            </rect>
            <text x={x + barWidth / 2} y={Math.max(y - 8, 16)} textAnchor="middle" fill="var(--color-text)" fontSize="12" fontWeight="700">
              {valueFormatter(value)}
            </text>
            <text x={x + barWidth / 2} y={height - 8} textAnchor="middle" fill="var(--color-muted)" fontSize="13">
              {label}
            </text>
          </g>
        )
      })}
    </svg>
  )
}

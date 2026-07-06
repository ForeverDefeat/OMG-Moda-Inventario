interface LineTrendChartProps {
  data: Array<Record<string, string | number>>
  valueFormatter?: (value: number) => string
  showPointLabels?: boolean
}

function shouldShowXAxisLabel(index: number, total: number) {
  if (total <= 8) return true
  if (index === 0 || index === total - 1) return true
  if (total <= 16) return index % 2 === 0
  const middle = Math.floor((total - 1) / 2)
  return index === middle || index % Math.ceil(total / 6) === 0
}

export function LineTrendChart({
  data,
  valueFormatter = (value) => String(value),
  showPointLabels = false,
}: LineTrendChartProps) {
  const values = data.map((item) => Number(item.value))
  const max = Math.max(...values, 1)
  const min = Math.min(...values, 0)
  const range = Math.max(max - min, 1)
  const width = 640
  const height = 240
  const padding = 42
  const step = data.length > 1 ? (width - padding * 2) / (data.length - 1) : 0
  const plotPoint = (value: number, index: number) => {
    const x = padding + index * step
    const y = height - padding - ((value - min) / range) * (height - padding * 2)
    return { x, y, value }
  }
  const points = values.map(plotPoint)
  const polylinePoints = points.map((point) => `${point.x},${point.y}`).join(' ')
  const areaPoints = points.length
    ? `${padding},${height - padding} ${polylinePoints} ${width - padding},${height - padding}`
    : ''
  const ticks = [0, 0.25, 0.5, 0.75, 1].map((ratio) => {
    const value = max - range * ratio
    const y = padding + ratio * (height - padding * 2)
    return { value, y }
  })

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="h-full w-full" role="img" aria-label="Grafica de tendencia">
      <defs>
        <linearGradient id="line-chart-fill" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stopColor="var(--color-chart-3)" stopOpacity="0.22" />
          <stop offset="100%" stopColor="var(--color-chart-3)" stopOpacity="0" />
        </linearGradient>
      </defs>
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
      {areaPoints && <polygon points={areaPoints} fill="url(#line-chart-fill)" />}
      <polyline points={polylinePoints} fill="none" stroke="var(--color-chart-1)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" />
      {points.map((point, index) => {
        const label = String(data[index].name)
        const showLabel = shouldShowXAxisLabel(index, data.length)

        return (
          <g key={label}>
            <circle cx={point.x} cy={point.y} r="5" fill="var(--color-surface)" stroke="var(--color-chart-1)" strokeWidth="3">
              <title>{`${label}: ${valueFormatter(point.value)}`}</title>
            </circle>
            {showPointLabels && (
              <text x={point.x} y={Math.max(point.y - 12, 16)} textAnchor="middle" fill="var(--color-text)" fontSize="12" fontWeight="700">
                {valueFormatter(point.value)}
              </text>
            )}
            {showLabel && (
              <text x={point.x} y={height - 12} textAnchor="middle" fill="var(--color-muted)" fontSize="12">
                {label}
              </text>
            )}
          </g>
        )
      })}
    </svg>
  )
}

export function BarMetricChart({ data }: { data: Array<Record<string, string | number>> }) {
  const width = 640
  const height = 240
  const padding = 34
  const values = data.map((item) => Number(item.value))
  const max = Math.max(...values, 1)
  const gap = 18
  const barWidth = (width - padding * 2 - gap * Math.max(data.length - 1, 0)) / Math.max(data.length, 1)

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="h-full w-full" role="img" aria-label="Grafica de barras">
      {[0, 1, 2, 3].map((line) => (
        <line
          key={line}
          x1={padding}
          x2={width - padding}
          y1={padding + line * 52}
          y2={padding + line * 52}
          stroke="var(--color-border)"
          strokeWidth="1"
        />
      ))}
      {data.map((item, index) => {
        const value = Number(item.value)
        const barHeight = ((height - padding * 2) * value) / max
        const x = padding + index * (barWidth + gap)
        const y = height - padding - barHeight

        return (
          <g key={String(item.name)}>
            <rect x={x} y={y} width={barWidth} height={barHeight} rx="8" fill="var(--color-chart-2)" />
            <text x={x + barWidth / 2} y={height - 8} textAnchor="middle" fill="var(--color-muted)" fontSize="13">
              {String(item.name)}
            </text>
          </g>
        )
      })}
    </svg>
  )
}

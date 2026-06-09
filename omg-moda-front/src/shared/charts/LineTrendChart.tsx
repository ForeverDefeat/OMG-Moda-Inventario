export function LineTrendChart({ data }: { data: Array<Record<string, string | number>> }) {
  const values = data.map((item) => Number(item.value))
  const max = Math.max(...values, 1)
  const min = Math.min(...values, 0)
  const range = Math.max(max - min, 1)
  const width = 640
  const height = 240
  const padding = 34
  const step = data.length > 1 ? (width - padding * 2) / (data.length - 1) : 0
  const points = values.map((value, index) => {
    const x = padding + index * step
    const y = height - padding - ((value - min) / range) * (height - padding * 2)
    return `${x},${y}`
  }).join(' ')

  return (
    <svg viewBox={`0 0 ${width} ${height}`} className="h-full w-full" role="img" aria-label="Grafica de tendencia">
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
      <polyline points={points} fill="none" stroke="var(--color-chart-1)" strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" />
      {data.map((item, index) => (
        <text key={String(item.name)} x={padding + index * step} y={height - 8} textAnchor="middle" fill="var(--color-muted)" fontSize="13">
          {String(item.name)}
        </text>
      ))}
    </svg>
  )
}

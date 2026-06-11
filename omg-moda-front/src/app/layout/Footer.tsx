import { Link } from 'react-router-dom'
import { Mail, MapPin, Server, Shirt, ShieldCheck } from 'lucide-react'

const footerModules = [
  { label: 'Catalogo', href: '/catalogo' },
  { label: 'Stock', href: '/stock' },
  { label: 'POS', href: '/ventas' },
  { label: 'Reportes', href: '/reportes' },
]

export function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="border-t border-[var(--color-border)] bg-[var(--color-surface)] px-6 py-6 text-xs text-[var(--color-muted)]">
      <div className="grid gap-6 lg:grid-cols-[1.2fr_1fr_1fr] lg:items-start">
        <div className="flex gap-3">
          <div className="grid size-10 shrink-0 place-items-center rounded-[var(--radius-lg)] bg-[var(--color-primary)] text-white">
            <Shirt size={18} />
          </div>
          <div>
            <p className="font-bold text-[var(--color-text)]">OMG MODA / ClothWise</p>
            <p className="mt-1 max-w-md leading-relaxed">
              Sistema de gestion de inventario, ventas POS y reposicion para operaciones de moda retail.
            </p>
            <p className="mt-3 font-semibold text-[var(--color-text)]">© {currentYear} OMG MODA. Uso interno administrativo.</p>
          </div>
        </div>

        <div className="grid gap-2">
          <p className="font-bold text-[var(--color-text)]">Datos de operacion</p>
          <span className="inline-flex items-center gap-2"><MapPin size={14} /> Lima, Peru</span>
          <span className="inline-flex items-center gap-2"><Mail size={14} /> soporte@omgmoda.pe</span>
          <span className="inline-flex items-center gap-2"><ShieldCheck size={14} /> Acceso protegido con JWT</span>
          <span className="inline-flex items-center gap-2"><Server size={14} /> API local: localhost:8080</span>
        </div>

        <div className="grid gap-3">
          <p className="font-bold text-[var(--color-text)]">Modulos principales</p>
          <div className="flex flex-wrap gap-2">
            {footerModules.map((module) => (
              <Link
                key={module.href}
                to={module.href}
                className="rounded-[var(--radius-md)] border border-[var(--color-border)] px-3 py-1.5 font-semibold text-[var(--color-text)] transition hover:bg-[var(--color-bg)]"
              >
                {module.label}
              </Link>
            ))}
          </div>
          <p className="leading-relaxed">Frontend v0.0.0 · MySQL inventario_omg · Spring Boot 4</p>
        </div>
      </div>
    </footer>
  )
}

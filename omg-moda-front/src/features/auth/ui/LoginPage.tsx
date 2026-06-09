import type { FormEvent } from 'react'
import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ArrowRight, EyeOff, Lock, Mail, Shirt } from 'lucide-react'
import { useAuth } from '../application/useAuth'

const heroImage = 'https://storage.googleapis.com/banani-generated-images/generated-images/97e3d0ab-7b3f-40ed-911d-2536b670d001.jpg'

export function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [correo, setCorreo] = useState('admin@omgmoda.com')
  const [contrasenia, setContrasenia] = useState('admin123')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login({ correo, contrasenia })
      const from = (location.state as { from?: string } | null)?.from ?? '/dashboard'
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo iniciar sesion')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="grid min-h-screen place-items-center bg-[var(--color-bg)] p-5 max-lg:p-0">
      <section className="flex min-h-[calc(100vh-40px)] w-full overflow-hidden rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] shadow-[var(--shadow-card)] max-lg:min-h-screen max-lg:rounded-none max-lg:border-0">
        <div className="relative hidden w-1/2 flex-col justify-between overflow-hidden bg-[var(--color-bg)] p-12 text-white lg:flex">
          <img
            src={heroImage}
            alt="Almacen moderno de ropa"
            className="absolute inset-0 h-full w-full object-cover"
          />
          <div className="absolute inset-0 bg-black/40" />
          <div className="relative z-10 flex h-full flex-col justify-between">
            <div className="flex items-center gap-3">
              <div className="flex size-10 items-center justify-center rounded-xl border border-white/20 bg-white/20 backdrop-blur">
                <Shirt size={20} />
              </div>
              <span className="text-xl font-bold tracking-tight">ClothWise</span>
            </div>

            <div className="max-w-md">
              <h2 className="mb-4 text-3xl font-bold leading-tight">
                Gestion de inventario inteligente para marcas modernas.
              </h2>
              <p className="text-sm leading-relaxed text-white/80">
                Optimiza tu cadena de suministro, rastrea el stock en tiempo real y toma decisiones basadas en datos.
              </p>
            </div>
          </div>
        </div>

        <div className="flex w-full items-center justify-center p-8 lg:w-1/2">
          <form onSubmit={handleSubmit} className="flex w-full max-w-sm flex-col">
            <div className="mb-10">
              <div className="mb-6 flex items-center gap-3 lg:hidden">
                <div className="flex size-10 items-center justify-center rounded-xl bg-[var(--color-primary)] text-white">
                  <Shirt size={20} />
                </div>
                <span className="text-xl font-bold tracking-tight">ClothWise</span>
              </div>
              <h1 className="mb-2 text-3xl font-bold tracking-tight text-[var(--color-text)]">
                Bienvenido de nuevo
              </h1>
              <p className="text-[var(--color-muted)]">
                Ingresa tus credenciales para acceder a tu cuenta.
              </p>
            </div>

            <div className="flex flex-col gap-5">
              <label className="flex flex-col gap-2">
                <span className="text-sm font-semibold text-[var(--color-text)]">Correo Electronico</span>
                <span className="flex items-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3 text-sm transition focus-within:border-[var(--color-primary)]">
                  <Mail size={16} className="mr-3 text-[var(--color-muted)]" />
                  <input
                    value={correo}
                    onChange={(event) => setCorreo(event.target.value)}
                    type="email"
                    className="w-full bg-transparent text-[var(--color-text)] outline-none placeholder:text-[var(--color-muted)]"
                    placeholder="ejemplo@clothwise.com"
                  />
                </span>
              </label>

              <label className="flex flex-col gap-2">
                <span className="flex items-center justify-between">
                  <span className="text-sm font-semibold text-[var(--color-text)]">Contrasenia</span>
                  <button type="button" className="text-xs font-medium text-[var(--color-primary)]">
                    Olvidaste tu contrasenia?
                  </button>
                </span>
                <span className="flex items-center rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3 text-sm transition focus-within:border-[var(--color-primary)]">
                  <Lock size={16} className="mr-3 text-[var(--color-muted)]" />
                  <input
                    value={contrasenia}
                    onChange={(event) => setContrasenia(event.target.value)}
                    type="password"
                    className="w-full bg-transparent text-[var(--color-text)] outline-none placeholder:text-[var(--color-muted)]"
                    placeholder="••••••••"
                  />
                  <EyeOff size={16} className="ml-3 text-[var(--color-muted)]" />
                </span>
              </label>

              <label className="mt-1 flex items-center gap-2">
                <input type="checkbox" defaultChecked className="size-4 rounded-sm border-[var(--color-border)] accent-[var(--color-primary)]" />
                <span className="text-sm text-[var(--color-muted)]">Mantener sesion iniciada</span>
              </label>
            </div>

            {error && (
              <p className="mt-5 rounded-xl bg-[var(--color-danger-soft)] px-4 py-3 text-sm font-medium text-[var(--color-danger-foreground)]">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="mt-8 flex w-full items-center justify-center gap-2 rounded-xl bg-[var(--color-primary)] py-3 text-sm font-bold text-[var(--color-primary-foreground)] shadow-sm transition hover:opacity-90 disabled:opacity-60"
            >
              {loading ? 'Validando...' : 'Iniciar Sesion'}
              <ArrowRight size={16} />
            </button>

            <div className="mt-8 flex flex-col items-center gap-4 border-t border-[var(--color-border)] pt-8">
              <p className="text-sm text-[var(--color-muted)]">
                No tienes una cuenta?{' '}
                <button type="button" className="font-bold text-[var(--color-primary)]">
                  Solicitar acceso
                </button>
              </p>
            </div>
          </form>
        </div>
      </section>
    </main>
  )
}

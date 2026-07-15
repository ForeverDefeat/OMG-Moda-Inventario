import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { KeyRound, Power, RefreshCcw, ShieldCheck, UserPlus, Users } from 'lucide-react'
import { useAuth } from '../../auth/application/useAuth'
import { usersApi } from '../../../infra/api/usersApi'
import { ActionButton } from '../../../shared/components/ActionButton'
import { Badge, RoleBadge } from '../../../shared/components/Badge'
import { DataTable, type Column } from '../../../shared/components/DataTable'
import { Modal } from '../../../shared/components/Modal'
import type { AppUser, CreateUserRequest, UserRole } from '../domain/types'

const emptyCreateForm: CreateUserRequest = {
  nombre: '',
  correo: '',
  contrasenia: '',
  rol: 'VENDEDOR',
}

type ModalMode = 'create' | 'role' | 'password' | null

export function UsersPage() {
  const { session } = useAuth()
  const [users, setUsers] = useState<AppUser[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [modalMode, setModalMode] = useState<ModalMode>(null)
  const [selectedUser, setSelectedUser] = useState<AppUser | null>(null)
  const [createForm, setCreateForm] = useState<CreateUserRequest>(emptyCreateForm)
  const [roleForm, setRoleForm] = useState<UserRole>('VENDEDOR')
  const [newPassword, setNewPassword] = useState('')

  const isAdmin = session?.rol === 'ADMIN'

  useEffect(() => {
    if (!isAdmin) return
    let active = true

    usersApi.list()
      .then((data) => {
        if (active) setUsers(data)
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof Error ? err.message : 'No se pudieron cargar los usuarios.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [isAdmin])

  const summary = useMemo(() => {
    const active = users.filter((user) => user.activo).length
    const admins = users.filter((user) => user.rol === 'ADMIN' && user.activo).length
    return { total: users.length, active, inactive: users.length - active, admins }
  }, [users])

  const columns: Column<AppUser>[] = [
    {
      key: 'nombre',
      header: 'Nombre',
      sortable: true,
      render: (user) => (
        <div>
          <p className="font-semibold text-[var(--color-text)]">{user.nombre}</p>
          <p className="text-xs text-[var(--color-muted)]">{user.correo}</p>
        </div>
      ),
    },
    {
      key: 'rol',
      header: 'Rol',
      sortable: true,
      render: (user) => <RoleBadge role={user.rol} />,
    },
    {
      key: 'activo',
      header: 'Estado',
      sortable: true,
      sortValue: (user) => (user.activo ? 1 : 0),
      render: (user) => (
        <Badge tone={user.activo ? 'success' : 'danger'}>{user.activo ? 'Activo' : 'Inactivo'}</Badge>
      ),
    },
    {
      key: 'acciones',
      header: 'Acciones',
      className: 'min-w-[360px]',
      render: (user) => (
        <div className="flex flex-wrap gap-2">
          <ActionButton variant="secondary" onClick={() => openRoleModal(user)}>
            <ShieldCheck size={15} />
            Rol
          </ActionButton>
          <ActionButton variant="secondary" onClick={() => openPasswordModal(user)}>
            <KeyRound size={15} />
            Clave
          </ActionButton>
          <ActionButton
            variant={user.activo ? 'danger' : 'secondary'}
            onClick={() => toggleUserStatus(user)}
            disabled={saving}
          >
            {user.activo ? <Power size={15} /> : <RefreshCcw size={15} />}
            {user.activo ? 'Desactivar' : 'Reactivar'}
          </ActionButton>
        </div>
      ),
    },
  ]

  function openCreateModal() {
    setCreateForm(emptyCreateForm)
    setError(null)
    setSuccess(null)
    setModalMode('create')
  }

  function openRoleModal(user: AppUser) {
    setSelectedUser(user)
    setRoleForm(user.rol)
    setError(null)
    setSuccess(null)
    setModalMode('role')
  }

  function openPasswordModal(user: AppUser) {
    setSelectedUser(user)
    setNewPassword('')
    setError(null)
    setSuccess(null)
    setModalMode('password')
  }

  function closeModal() {
    if (saving) return
    setModalMode(null)
    setSelectedUser(null)
  }

  function updateUser(nextUser: AppUser) {
    setUsers((current) => current.map((user) => (user.id === nextUser.id ? nextUser : user)))
  }

  function validateCreateForm() {
    if (!createForm.nombre.trim()) return 'Ingresa el nombre del usuario.'
    if (!createForm.correo.trim() || !createForm.correo.includes('@')) return 'Ingresa un correo valido.'
    if (createForm.contrasenia.length < 6) return 'La contrasenia debe tener al menos 6 caracteres.'
    return null
  }

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const validationError = validateCreateForm()
    if (validationError) {
      setError(validationError)
      return
    }

    setSaving(true)
    setError(null)
    try {
      const created = await usersApi.create({
        ...createForm,
        nombre: createForm.nombre.trim(),
        correo: createForm.correo.trim().toLowerCase(),
      })
      setUsers((current) => [...current, created].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es')))
      setSuccess('Usuario creado correctamente.')
      setModalMode(null)
      setSelectedUser(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo crear el usuario.')
    } finally {
      setSaving(false)
    }
  }

  async function handleRoleChange(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedUser) return
    setSaving(true)
    setError(null)
    try {
      updateUser(await usersApi.updateRole(selectedUser.id, { rol: roleForm }))
      setSuccess('Rol actualizado correctamente.')
      setModalMode(null)
      setSelectedUser(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo actualizar el rol.')
    } finally {
      setSaving(false)
    }
  }

  async function handlePasswordReset(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedUser) return
    if (newPassword.length < 6) {
      setError('La nueva contrasenia debe tener al menos 6 caracteres.')
      return
    }

    setSaving(true)
    setError(null)
    try {
      updateUser(await usersApi.resetPassword(selectedUser.id, { nuevaContrasenia: newPassword }))
      setSuccess('Contrasenia actualizada correctamente.')
      setModalMode(null)
      setSelectedUser(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo resetear la contrasenia.')
    } finally {
      setSaving(false)
    }
  }

  async function toggleUserStatus(user: AppUser) {
    setSaving(true)
    setError(null)
    setSuccess(null)
    try {
      const updated = user.activo ? await usersApi.deactivate(user.id) : await usersApi.reactivate(user.id)
      updateUser(updated)
      setSuccess(user.activo ? 'Usuario desactivado.' : 'Usuario reactivado.')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo actualizar el estado.')
    } finally {
      setSaving(false)
    }
  }

  if (!isAdmin) {
    return (
      <section className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-6">
        <h1 className="text-xl font-bold text-[var(--color-text)]">Acceso restringido</h1>
        <p className="mt-2 text-sm text-[var(--color-muted)]">Solo administradores pueden gestionar usuarios y credenciales.</p>
      </section>
    )
  }

  return (
    <div className="space-y-6">
      <section className="flex flex-col justify-between gap-4 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 shadow-[var(--shadow-card)] md:flex-row md:items-center">
        <div>
          <div className="mb-2 inline-flex items-center gap-2 rounded-full bg-[var(--color-bg)] px-3 py-1 text-xs font-bold text-[var(--color-muted)]">
            <Users size={14} />
            Administracion
          </div>
          <h1 className="text-2xl font-bold text-[var(--color-text)]">Usuarios y credenciales</h1>
          <p className="mt-1 max-w-2xl text-sm text-[var(--color-muted)]">
            Crea accesos internos, cambia roles, resetea claves y bloquea usuarios sin perder historial operativo.
          </p>
        </div>
        <ActionButton onClick={openCreateModal} className="w-full md:w-auto">
          <UserPlus size={17} />
          Anadir usuario
        </ActionButton>
      </section>

      <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Usuarios" value={summary.total} />
        <MetricCard label="Activos" value={summary.active} tone="success" />
        <MetricCard label="Inactivos" value={summary.inactive} tone="danger" />
        <MetricCard label="Admins activos" value={summary.admins} tone="primary" />
      </section>

      {(error || success) && (
        <div className={`rounded-xl border px-4 py-3 text-sm font-medium ${
          error
            ? 'border-[var(--color-danger-soft)] bg-[var(--color-danger-soft)] text-[var(--color-danger-foreground)]'
            : 'border-[var(--color-success-soft)] bg-[var(--color-success-soft)] text-[var(--color-success-foreground)]'
        }`}>
          {error ?? success}
        </div>
      )}

      <DataTable columns={columns} rows={users} emptyText={loading ? 'Cargando usuarios...' : 'No hay usuarios registrados'} />

      <Modal open={modalMode === 'create'} title="Anadir usuario" onClose={closeModal}>
        <form className="space-y-4" onSubmit={handleCreate}>
          <TextField label="Nombre" value={createForm.nombre} onChange={(value) => setCreateForm((current) => ({ ...current, nombre: value }))} />
          <TextField label="Correo electronico" type="email" value={createForm.correo} onChange={(value) => setCreateForm((current) => ({ ...current, correo: value }))} />
          <TextField label="Contrasenia inicial" type="password" value={createForm.contrasenia} onChange={(value) => setCreateForm((current) => ({ ...current, contrasenia: value }))} />
          <RoleSelect value={createForm.rol} onChange={(rol) => setCreateForm((current) => ({ ...current, rol }))} />
          <ModalActions saving={saving} onCancel={closeModal} submitLabel="Crear usuario" />
        </form>
      </Modal>

      <Modal open={modalMode === 'role'} title={`Cambiar rol${selectedUser ? ` de ${selectedUser.nombre}` : ''}`} onClose={closeModal}>
        <form className="space-y-4" onSubmit={handleRoleChange}>
          <RoleSelect value={roleForm} onChange={setRoleForm} />
          <ModalActions saving={saving} onCancel={closeModal} submitLabel="Guardar rol" />
        </form>
      </Modal>

      <Modal open={modalMode === 'password'} title={`Resetear contrasenia${selectedUser ? ` de ${selectedUser.nombre}` : ''}`} onClose={closeModal}>
        <form className="space-y-4" onSubmit={handlePasswordReset}>
          <TextField label="Nueva contrasenia" type="password" value={newPassword} onChange={setNewPassword} />
          <ModalActions saving={saving} onCancel={closeModal} submitLabel="Actualizar contrasenia" />
        </form>
      </Modal>
    </div>
  )
}

function MetricCard({ label, value, tone = 'neutral' }: { label: string; value: number; tone?: 'neutral' | 'success' | 'danger' | 'primary' }) {
  const toneClass = {
    neutral: 'text-[var(--color-text)]',
    success: 'text-[var(--color-success-foreground)]',
    danger: 'text-[var(--color-danger-foreground)]',
    primary: 'text-[var(--color-primary-strong)]',
  }[tone]

  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 shadow-[var(--shadow-card)]">
      <p className="text-xs font-bold uppercase tracking-[0.08em] text-[var(--color-muted)]">{label}</p>
      <p className={`mt-2 text-3xl font-bold ${toneClass}`}>{value}</p>
    </div>
  )
}

function TextField({ label, value, onChange, type = 'text' }: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
}) {
  return (
    <label className="block">
      <span className="text-sm font-semibold text-[var(--color-text)]">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm outline-none transition focus:border-[var(--color-primary)]"
      />
    </label>
  )
}

function RoleSelect({ value, onChange }: { value: UserRole; onChange: (role: UserRole) => void }) {
  return (
    <label className="block">
      <span className="text-sm font-semibold text-[var(--color-text)]">Rol</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value as UserRole)}
        className="mt-2 w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-sm outline-none transition focus:border-[var(--color-primary)]"
      >
        <option value="VENDEDOR">VENDEDOR</option>
        <option value="ADMIN">ADMIN</option>
      </select>
    </label>
  )
}

function ModalActions({ saving, onCancel, submitLabel }: { saving: boolean; onCancel: () => void; submitLabel: string }) {
  return (
    <div className="grid grid-cols-1 gap-2 pt-2 sm:flex sm:justify-end">
      <ActionButton type="button" variant="secondary" onClick={onCancel} disabled={saving}>Cancelar</ActionButton>
      <ActionButton type="submit" disabled={saving}>{saving ? 'Guardando...' : submitLabel}</ActionButton>
    </div>
  )
}

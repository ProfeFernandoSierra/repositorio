import { useState, type FormEvent } from 'react';
import { errorMessage, rolesApi, usersApi } from '../../api';
import { useAuth } from '../../context/AuthContext';
import { useFetch } from '../../hooks/useFetch';
import type { RoleDto, User, UserInput } from '../../types';

const EMPTY: UserInput = { name: '', email: '', password: '', role: 'USER' };

export default function AdminUsers() {
  const { user: me } = useAuth();
  const { data: users, loading, error, reload } = useFetch<User[]>(usersApi.list, []);
  const { data: roles } = useFetch<RoleDto[]>(rolesApi.list, []);
  const [form, setForm] = useState<UserInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  function startEdit(u: User) {
    setEditingId(u.id);
    setForm({ name: u.name, email: u.email, password: '', role: u.role });
    setFormError('');
  }

  function reset() {
    setEditingId(null);
    setForm(EMPTY);
    setFormError('');
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const password = form.password?.trim() ?? '';
    if (!editingId && password.length < 8) return setFormError('La contraseña debe tener al menos 8 caracteres');
    if (editingId && password && password.length < 8) return setFormError('La nueva contraseña debe tener al menos 8 caracteres');
    // En edición, si no se escribió contraseña no se envía (el backend la mantiene)
    const payload: UserInput = { ...form, password: password || undefined };
    setSaving(true);
    setFormError('');
    try {
      if (editingId) await usersApi.update(editingId, payload);
      else await usersApi.create(payload);
      reset();
      await reload();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(u: User) {
    if (!confirm(`¿Eliminar a ${u.name}?`)) return;
    try {
      await usersApi.remove(u.id);
      if (editingId === u.id) reset();
      await reload();
    } catch (err) {
      alert(errorMessage(err));
    }
  }

  return (
    <div className="admin-grid">
      <form className="panel form" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Editar usuario' : 'Nuevo usuario'}</h2>
        <label>Nombre<input value={form.name} maxLength={80} required onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <label>Email<input type="email" value={form.email} required onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
        <label>
          {editingId ? 'Nueva contraseña (vacío = no cambiar)' : 'Contraseña'}
          <input type="password" value={form.password} autoComplete="new-password"
                 onChange={(e) => setForm({ ...form, password: e.target.value })} />
        </label>
        <label>
          Rol
          <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as UserInput['role'] })}
                  disabled={editingId === me?.id}>
            {roles.map((r) => <option key={r.id} value={r.name}>{r.name}</option>)}
          </select>
        </label>
        {editingId === me?.id && <p className="hint">No puedes cambiar tu propio rol.</p>}
        {formError && <p className="error">{formError}</p>}
        <div className="row">
          <button className="btn" disabled={saving}>{editingId ? 'Guardar cambios' : 'Crear'}</button>
          {editingId && <button type="button" className="btn btn-ghost" onClick={reset}>Cancelar</button>}
        </div>
      </form>

      <div className="panel">
        {loading && <p>Cargando…</p>}
        {error && <p className="error">{error}</p>}
        <table className="table">
          <thead><tr><th>Nombre</th><th>Email</th><th>Rol</th><th /></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.name}{u.id === me?.id && ' (tú)'}</td>
                <td>{u.email}</td>
                <td><span className={`role-tag ${u.role === 'ADMIN' ? '' : 'user'}`}>{u.role}</span></td>
                <td className="actions">
                  <button className="btn btn-sm" onClick={() => startEdit(u)}>Editar</button>
                  <button className="btn btn-sm btn-ghost danger" disabled={u.id === me?.id} onClick={() => handleDelete(u)}>
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

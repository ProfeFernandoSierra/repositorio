import { useState, type FormEvent } from 'react';
import { categoriesApi, errorMessage } from '../../api';
import TypeChip from '../../components/TypeChip';
import { useFetch } from '../../hooks/useFetch';
import type { Category, CategoryInput } from '../../types';

const EMPTY: CategoryInput = { name: '', color: '#a8a878' };

export default function AdminCategories() {
  const { data: categories, loading, error, reload } = useFetch<Category[]>(categoriesApi.list, []);
  const [form, setForm] = useState<CategoryInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  function startEdit(c: Category) {
    setEditingId(c.id);
    setForm({ name: c.name, color: c.color });
    setFormError('');
  }

  function reset() {
    setEditingId(null);
    setForm(EMPTY);
    setFormError('');
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    setFormError('');
    try {
      if (editingId) await categoriesApi.update(editingId, form);
      else await categoriesApi.create(form);
      reset();
      await reload();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(c: Category) {
    if (!confirm(`¿Eliminar el tipo "${c.name}"?`)) return;
    try {
      await categoriesApi.remove(c.id);
      if (editingId === c.id) reset();
      await reload();
    } catch (err) {
      alert(errorMessage(err));
    }
  }

  return (
    <div className="admin-grid">
      <form className="panel form" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Editar tipo' : 'Nuevo tipo'}</h2>
        <label>Nombre<input value={form.name} maxLength={30} required onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <label>Color<input type="color" value={form.color} onChange={(e) => setForm({ ...form, color: e.target.value })} /></label>
        <p>Vista previa: <TypeChip category={{ id: 0, name: form.name || 'tipo', color: form.color }} /></p>
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
          <thead><tr><th>Tipo</th><th>Color</th><th /></tr></thead>
          <tbody>
            {categories.map((c) => (
              <tr key={c.id}>
                <td><TypeChip category={c} /></td>
                <td><code>{c.color}</code></td>
                <td className="actions">
                  <button className="btn btn-sm" onClick={() => startEdit(c)}>Editar</button>
                  <button className="btn btn-sm btn-ghost danger" onClick={() => handleDelete(c)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

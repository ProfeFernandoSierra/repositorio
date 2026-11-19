import { useState, type ChangeEvent, type FormEvent } from 'react';
import { categoriesApi, errorMessage, productsApi } from '../../api';
import TypeChip from '../../components/TypeChip';
import { useFetch } from '../../hooks/useFetch';
import type { Category, Product, ProductInput } from '../../types';
import { defaultImage, formatPrice } from '../../utils';

const EMPTY: ProductInput = { pokedexNumber: 1, name: '', price: 0, stock: 0, imageUrl: '', categoryIds: [] };

export default function AdminProducts() {
  const { data: products, loading, error, reload } = useFetch<Product[]>(productsApi.list, []);
  const { data: categories } = useFetch<Category[]>(categoriesApi.list, []);
  const [form, setForm] = useState<ProductInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const preview = form.imageUrl || (form.pokedexNumber > 0 ? defaultImage(form.pokedexNumber) : '');

  function startEdit(p: Product) {
    setEditingId(p.id);
    setForm({
      pokedexNumber: p.pokedexNumber,
      name: p.name,
      price: p.price,
      stock: p.stock,
      // Si es la imagen por defecto se deja vacío para que se recalcule al cambiar el N° de Pokédex
      imageUrl: p.imageUrl === defaultImage(p.pokedexNumber) ? '' : p.imageUrl,
      categoryIds: p.categories.map((c) => c.id),
    });
    setFormError('');
  }

  function reset() {
    setEditingId(null);
    setForm(EMPTY);
    setFormError('');
  }

  function toggleCategory(id: number) {
    setForm((f) => ({
      ...f,
      categoryIds: f.categoryIds.includes(id) ? f.categoryIds.filter((c) => c !== id) : [...f.categoryIds, id],
    }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (form.categoryIds.length === 0) return setFormError('Selecciona al menos un tipo');
    if (form.categoryIds.length > 2) return setFormError('Máximo 2 tipos');
    setSaving(true);
    setFormError('');
    try {
      if (editingId) await productsApi.update(editingId, form);
      else await productsApi.create(form);
      reset();
      await reload();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(p: Product) {
    if (!confirm(`¿Eliminar a ${p.name}?`)) return;
    try {
      await productsApi.remove(p.id);
      if (editingId === p.id) reset();
      await reload();
    } catch (err) {
      alert(errorMessage(err));
    }
  }

  const num = (field: 'pokedexNumber' | 'price' | 'stock') => (e: ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [field]: Number(e.target.value) });

  return (
    <div className="admin-grid">
      <form className="panel form" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Editar Pokémon' : 'Nuevo Pokémon'}</h2>
        {preview && <img className="preview" src={preview} alt="Vista previa" />}
        <label>N° Pokédex<input type="number" min={1} max={1025} value={form.pokedexNumber} onChange={num('pokedexNumber')} required /></label>
        <label>Nombre<input value={form.name} maxLength={50} required onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <div className="row">
          <label>Precio (CLP)<input type="number" min={0} value={form.price} onChange={num('price')} required /></label>
          <label>Stock<input type="number" min={0} value={form.stock} onChange={num('stock')} required /></label>
        </div>
        <label>
          URL imagen (opcional, https)
          <input value={form.imageUrl} placeholder="Se usa la de PokeAPI si se deja vacío"
                 onChange={(e) => setForm({ ...form, imageUrl: e.target.value })} />
        </label>
        <fieldset>
          <legend>Tipos (máx. 2)</legend>
          <div className="checks">
            {categories.map((c) => (
              <label key={c.id} className="check">
                <input type="checkbox" checked={form.categoryIds.includes(c.id)} onChange={() => toggleCategory(c.id)} />
                <TypeChip category={c} />
              </label>
            ))}
          </div>
        </fieldset>
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
          <thead><tr><th /><th>#</th><th>Nombre</th><th>Tipos</th><th>Precio</th><th>Stock</th><th /></tr></thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td><img className="thumb" src={p.imageUrl} alt="" /></td>
                <td>{p.pokedexNumber}</td>
                <td>{p.name}</td>
                <td><div className="types">{p.categories.map((c) => <TypeChip key={c.id} category={c} />)}</div></td>
                <td>{formatPrice(p.price)}</td>
                <td className={p.stock === 0 ? 'error' : ''}>{p.stock}</td>
                <td className="actions">
                  <button className="btn btn-sm" onClick={() => startEdit(p)}>Editar</button>
                  <button className="btn btn-sm btn-ghost danger" onClick={() => handleDelete(p)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

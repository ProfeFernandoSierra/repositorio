import { useState } from 'react';
import { categoriesApi, productsApi } from '../api';
import PokemonCard from '../components/PokemonCard';
import { useFetch } from '../hooks/useFetch';
import type { Category, Product } from '../types';

export default function Home() {
  const products = useFetch<Product[]>(productsApi.list, []);
  const categories = useFetch<Category[]>(categoriesApi.list, []);
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState(0);

  const filtered = products.data.filter(
    (p) =>
      p.name.toLowerCase().includes(search.trim().toLowerCase()) &&
      (categoryId === 0 || p.categories.some((c) => c.id === categoryId)),
  );

  return (
    <section>
      <div className="hero">
        <h1>¡Atrápalos a todos!</h1>
        <p>Elige tu próximo compañero de aventuras.</p>
      </div>

      <div className="filters">
        <input placeholder="Buscar Pokémon…" value={search} onChange={(e) => setSearch(e.target.value)} />
        <select value={categoryId} onChange={(e) => setCategoryId(Number(e.target.value))}>
          <option value={0}>Todos los tipos</option>
          {categories.data.map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
      </div>

      {products.loading && <p className="center">Cargando…</p>}
      {products.error && <p className="error center">{products.error}</p>}
      {!products.loading && !products.error && filtered.length === 0 && <p className="center">No hay resultados.</p>}

      <div className="grid">
        {filtered.map((p) => (
          <PokemonCard key={p.id} product={p} />
        ))}
      </div>
    </section>
  );
}

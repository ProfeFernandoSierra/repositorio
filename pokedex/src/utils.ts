const clp = new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' });
const dateFmt = new Intl.DateTimeFormat('es-CL', { dateStyle: 'medium', timeStyle: 'short' });

export const formatPrice = (value: number) => clp.format(value);
export const formatDate = (iso: string) => dateFmt.format(new Date(iso));

export const defaultImage = (pokedexNumber: number) =>
  `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokedexNumber}.png`;

/** Lee JSON de localStorage sin romper la app si el dato está corrupto. */
export function readStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

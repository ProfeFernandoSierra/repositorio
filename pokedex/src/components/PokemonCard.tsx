import { useCart } from '../context/CartContext';
import type { Product } from '../types';
import { formatPrice } from '../utils';
import TypeChip from './TypeChip';

export default function PokemonCard({ product }: { product: Product }) {
  const { items, addToCart } = useCart();
  const inCart = items.find((i) => i.product.id === product.id)?.quantity ?? 0;
  const available = product.stock - inCart;

  return (
    <article className="card">
      <span className="dex">#{String(product.pokedexNumber).padStart(3, '0')}</span>
      <img src={product.imageUrl} alt={product.name} loading="lazy" />
      <h3>{product.name}</h3>
      <div className="types">
        {product.categories.map((c) => (
          <TypeChip key={c.id} category={c} />
        ))}
      </div>
      <p className="price">{formatPrice(product.price)}</p>
      <p className="stock">{product.stock > 0 ? `Stock: ${product.stock}` : 'Agotado'}</p>
      <button className="btn" disabled={available <= 0} onClick={() => addToCart(product)}>
        {available <= 0 ? 'Sin stock' : 'Agregar al carrito'}
      </button>
    </article>
  );
}

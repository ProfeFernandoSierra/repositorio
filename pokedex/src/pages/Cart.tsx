import { useState } from 'react';
import { Link } from 'react-router-dom';
import { errorMessage, ordersApi } from '../api';
import { useCart } from '../context/CartContext';
import { formatPrice } from '../utils';

export default function Cart() {
  const { items, setQuantity, removeFromCart, clearCart, totalPrice } = useCart();
  const [status, setStatus] = useState<'idle' | 'loading' | 'done' | 'error'>('idle');
  const [message, setMessage] = useState('');

  async function handleCheckout() {
    setStatus('loading');
    try {
      // Solo se envía qué y cuánto: el backend valida stock y calcula el total con sus precios
      const order = await ordersApi.create(items.map((i) => ({ productId: i.product.id, quantity: i.quantity })));
      clearCart();
      setStatus('done');
      setMessage(`¡Compra realizada! Orden #${order.id} por ${formatPrice(order.total)}`);
    } catch (err) {
      setStatus('error');
      setMessage(errorMessage(err));
    }
  }

  if (status === 'done') {
    return (
      <section className="empty">
        <h1>🎉 {message}</h1>
        <div className="row">
          <Link to="/" className="btn">Seguir comprando</Link>
          <Link to="/mis-compras" className="btn btn-ghost">Ver mis compras</Link>
        </div>
      </section>
    );
  }

  if (items.length === 0) {
    return (
      <section className="empty">
        <h1>Tu carrito está vacío</h1>
        <Link to="/" className="btn">Ver Pokémon</Link>
      </section>
    );
  }

  return (
    <section className="cart">
      <h1>Carrito</h1>
      <ul className="cart-list">
        {items.map(({ product, quantity }) => (
          <li key={product.id} className="cart-item">
            <img src={product.imageUrl} alt={product.name} />
            <div className="info">
              <strong>{product.name}</strong>
              <span>{formatPrice(product.price)} c/u</span>
            </div>
            <div className="qty">
              <button onClick={() => setQuantity(product.id, quantity - 1)} disabled={quantity <= 1}>−</button>
              <span>{quantity}</span>
              <button onClick={() => setQuantity(product.id, quantity + 1)} disabled={quantity >= product.stock}>+</button>
            </div>
            <strong className="subtotal">{formatPrice(product.price * quantity)}</strong>
            <button className="btn btn-ghost danger" onClick={() => removeFromCart(product.id)}>Quitar</button>
          </li>
        ))}
      </ul>

      <div className="cart-footer">
        <button className="btn btn-ghost" onClick={clearCart}>Vaciar carrito</button>
        <div className="total">Total: <strong>{formatPrice(totalPrice)}</strong></div>
        <button className="btn" onClick={handleCheckout} disabled={status === 'loading'}>
          {status === 'loading' ? 'Procesando…' : 'Comprar'}
        </button>
      </div>
      {status === 'error' && <p className="error">{message}</p>}
    </section>
  );
}

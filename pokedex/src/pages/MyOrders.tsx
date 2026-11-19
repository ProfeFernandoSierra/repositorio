import { ordersApi } from '../api';
import { useFetch } from '../hooks/useFetch';
import type { Order } from '../types';
import { formatDate, formatPrice } from '../utils';

export default function MyOrders() {
  const { data: orders, loading, error } = useFetch<Order[]>(ordersApi.mine, []);

  return (
    <section>
      <h1>Mis compras</h1>
      {loading && <p>Cargando…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && !error && orders.length === 0 && <p>Aún no has comprado nada.</p>}
      <div className="orders">
        {orders.map((o) => (
          <article key={o.id} className="panel">
            <header className="order-head">
              <strong>Orden #{o.id}</strong>
              <span>{formatDate(o.createdAt)}</span>
              <strong>{formatPrice(o.total)}</strong>
            </header>
            <ul>
              {o.items.map((i) => (
                <li key={i.productId}>
                  {i.quantity} × {i.productName} — {formatPrice(i.subtotal)}
                </li>
              ))}
            </ul>
          </article>
        ))}
      </div>
    </section>
  );
}

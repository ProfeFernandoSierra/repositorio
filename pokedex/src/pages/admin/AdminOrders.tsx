import { ordersApi } from '../../api';
import { useFetch } from '../../hooks/useFetch';
import type { Order } from '../../types';
import { formatDate, formatPrice } from '../../utils';

export default function AdminOrders() {
  const { data: orders, loading, error } = useFetch<Order[]>(ordersApi.all, []);

  return (
    <div className="panel">
      {loading && <p>Cargando…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && !error && orders.length === 0 && <p>Todavía no hay ventas.</p>}
      <table className="table">
        <thead><tr><th>#</th><th>Fecha</th><th>Cliente</th><th>Detalle</th><th>Total</th></tr></thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td>{o.id}</td>
              <td>{formatDate(o.createdAt)}</td>
              <td>{o.userName}<br /><small>{o.userEmail}</small></td>
              <td>{o.items.map((i) => `${i.quantity}× ${i.productName}`).join(', ')}</td>
              <td><strong>{formatPrice(o.total)}</strong></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

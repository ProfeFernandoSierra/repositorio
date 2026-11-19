import { NavLink, Outlet } from 'react-router-dom';

export default function AdminLayout() {
  return (
    <section>
      <h1>Panel de administración</h1>
      <nav className="tabs">
        <NavLink to="/admin/pokemon">Pokémon</NavLink>
        <NavLink to="/admin/tipos">Tipos</NavLink>
        <NavLink to="/admin/usuarios">Usuarios</NavLink>
        <NavLink to="/admin/ordenes">Órdenes</NavLink>
      </nav>
      <Outlet />
    </section>
  );
}

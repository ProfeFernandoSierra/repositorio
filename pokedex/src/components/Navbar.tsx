import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, isAdmin, signOut } = useAuth();
  const { totalItems, clearCart } = useCart();
  const navigate = useNavigate();

  function handleLogout() {
    signOut();
    clearCart(); // el carrito no debe quedar para el siguiente usuario del PC
    navigate('/login');
  }

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        <span className="pokeball" aria-hidden /> PokéStore
      </Link>
      <nav>
        <NavLink to="/" end>Inicio</NavLink>
        <NavLink to="/carrito">
          Carrito <span className="badge">{totalItems}</span>
        </NavLink>
        {user && <NavLink to="/mis-compras">Mis compras</NavLink>}
        {isAdmin && <NavLink to="/admin">Admin</NavLink>}
        {user ? (
          <>
            <span className="user">
              Hola, {user.name.split(' ')[0]} {isAdmin && <span className="role-tag">ADMIN</span>}
            </span>
            <button className="btn btn-ghost" onClick={handleLogout}>Salir</button>
          </>
        ) : (
          <NavLink to="/login">Iniciar sesión</NavLink>
        )}
      </nav>
    </header>
  );
}

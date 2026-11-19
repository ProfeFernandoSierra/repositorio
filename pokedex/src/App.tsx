import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import AdminCategories from './pages/admin/AdminCategories';
import AdminLayout from './pages/admin/AdminLayout';
import AdminOrders from './pages/admin/AdminOrders';
import AdminProducts from './pages/admin/AdminProducts';
import AdminUsers from './pages/admin/AdminUsers';
import Cart from './pages/Cart';
import Home from './pages/Home';
import Login from './pages/Login';
import MyOrders from './pages/MyOrders';
import Register from './pages/Register';

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Register />} />
          <Route path="/carrito" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
          <Route path="/mis-compras" element={<ProtectedRoute><MyOrders /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminLayout /></ProtectedRoute>}>
            <Route index element={<Navigate to="pokemon" replace />} />
            <Route path="pokemon" element={<AdminProducts />} />
            <Route path="tipos" element={<AdminCategories />} />
            <Route path="usuarios" element={<AdminUsers />} />
            <Route path="ordenes" element={<AdminOrders />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </>
  );
}

import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../types';

/**
 * Protege rutas en el FRONT. Es experiencia de usuario, no seguridad:
 * la seguridad real está en Spring Security (cada endpoint valida el token y el rol).
 */
export default function ProtectedRoute({ children, role }: { children: ReactNode; role?: Role }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return <>{children}</>;
}

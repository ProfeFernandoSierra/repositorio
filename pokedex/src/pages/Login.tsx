import { useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { user, signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from ?? '/';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) return <Navigate to={from} replace />;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    if (!email.trim() || !password) return setError('Completa email y contraseña');
    setLoading(true);
    try {
      await signIn(email, password);
      navigate(from, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="login">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>Iniciar sesión</h1>
        <label>
          Email
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="username" />
        </label>
        <label>
          Contraseña
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="btn" disabled={loading}>{loading ? 'Ingresando…' : 'Ingresar'}</button>
        <p className="hint">
          ¿No tienes cuenta? <Link to="/registro">Regístrate</Link>
        </p>
        <p className="hint">
          Admin: admin@pokestore.cl / Admin1234 · User: ash@pokemon.com / Pikachu123
        </p>
      </form>
    </section>
  );
}

import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { user, signUp } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const set = (field: keyof typeof form) => (e: ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [field]: e.target.value });

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    if (form.password.length < 8) return setError('La contraseña debe tener al menos 8 caracteres');
    if (form.password !== form.confirm) return setError('Las contraseñas no coinciden');
    setLoading(true);
    try {
      await signUp(form.name, form.email, form.password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="login">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>Crear cuenta</h1>
        <label>Nombre<input value={form.name} onChange={set('name')} maxLength={80} required /></label>
        <label>Email<input type="email" value={form.email} onChange={set('email')} autoComplete="username" required /></label>
        <label>Contraseña<input type="password" value={form.password} onChange={set('password')} autoComplete="new-password" required /></label>
        <label>Repetir contraseña<input type="password" value={form.confirm} onChange={set('confirm')} autoComplete="new-password" required /></label>
        {error && <p className="error">{error}</p>}
        <button className="btn" disabled={loading}>{loading ? 'Creando…' : 'Registrarme'}</button>
        <p className="hint">¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </form>
    </section>
  );
}

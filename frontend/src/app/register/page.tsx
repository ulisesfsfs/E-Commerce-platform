'use client';
import { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import styles from '../auth.module.css';

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handle = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (form.password.length < 6) { setError('La contraseña debe tener al menos 6 caracteres.'); return; }
    setLoading(true);
    try {
      await register(form.firstName, form.lastName, form.email, form.password);
      router.push('/');
    } catch (err: any) {
      setError(err.message || 'Error al registrarse. Intentá con otro email.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.header}>
          <div className={styles.logo}>⚡</div>
          <h1 className={styles.title}>Creá tu cuenta</h1>
          <p className={styles.sub}>Gratis, rápido y sin complicaciones</p>
        </div>

        <form onSubmit={submit} className={styles.form}>
          {error && <div className="alert alert-error">{error}</div>}

          <div className={styles.row}>
            <div className="form-group">
              <label htmlFor="firstName" className="form-label">Nombre</label>
              <input id="firstName" name="firstName" type="text" className="form-input"
                placeholder="Juan" value={form.firstName} onChange={handle} required />
            </div>
            <div className="form-group">
              <label htmlFor="lastName" className="form-label">Apellido</label>
              <input id="lastName" name="lastName" type="text" className="form-input"
                placeholder="Pérez" value={form.lastName} onChange={handle} required />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="email" className="form-label">Email</label>
            <input id="email" name="email" type="email" className="form-input"
              placeholder="tu@email.com" value={form.email} onChange={handle} required autoComplete="email" />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">Contraseña</label>
            <input id="password" name="password" type="password" className="form-input"
              placeholder="Mínimo 6 caracteres" value={form.password} onChange={handle} required autoComplete="new-password" />
          </div>

          <button id="register-btn" type="submit" className="btn btn-primary btn-full btn-lg" disabled={loading}>
            {loading ? <><div className="spinner" /> Creando cuenta...</> : 'Crear cuenta'}
          </button>
        </form>

        <p className={styles.footer}>
          ¿Ya tenés cuenta?{' '}
          <Link href="/login" className={styles.link}>Ingresá acá</Link>
        </p>
      </div>
      <div className={styles.glow} />
    </div>
  );
}

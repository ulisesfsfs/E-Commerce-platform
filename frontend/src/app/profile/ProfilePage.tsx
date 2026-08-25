'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import Link from 'next/link';
import { userApi, Address, UserProfile } from '@/lib/api';
import styles from './ProfilePage.module.css';

const EMPTY_ADDRESS: Address = {
  address: '',
  city: '',
  state: '',
  zipCode: '',
  country: '',
};

import { useToast } from '@/context/ToastContext';
import { ProfileSkeleton } from '@/components/skeletons/ProfileSkeleton';

export default function ProfilePage() {
  const { token, loading, logout } = useAuth();
  const { addToast } = useToast();
  const router = useRouter();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [form, setForm] = useState<Address>(EMPTY_ADDRESS);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'data' | 'address'>('data');

  useEffect(() => {
    if (!loading && !token) router.push('/login');
  }, [loading, token, router]);

  useEffect(() => {
    if (!token) return;
    userApi
      .getProfile()
      .then((data) => {
        setProfile(data);
        if (data.address) {
          setForm({
            address: data.address.address ?? '',
            city: data.address.city ?? '',
            state: data.address.state ?? '',
            zipCode: data.address.zipCode ?? '',
            country: data.address.country ?? '',
          });
        }
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoadingProfile(false));
  }, [token]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setSuccess(false);
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(false);
    try {
      await userApi.updateAddress(form);
      setSuccess(true);
      setProfile((prev) => prev ? { ...prev, address: form } : null);
      addToast('¡Dirección guardada con éxito!', 'success');
    } catch (e: unknown) {
      const errMsg = e instanceof Error ? e.message : String(e);
      setError(errMsg);
      addToast(errMsg, 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    logout();
    addToast('Sesión cerrada', 'info');
    router.push('/login');
  };

  if (loading || loadingProfile) {
    return <ProfileSkeleton />;
  }

  const initials = `${profile?.firstName?.[0] ?? ''}${profile?.lastName?.[0] ?? ''}`.toUpperCase() || '?';
  const isAdmin = profile?.roles?.includes('ROLE_ADMIN');
  const hasAddress = !!(profile?.address?.address || profile?.address?.city || profile?.address?.country);

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.container}>
        {/* ── Sidebar ── */}
        <aside className={styles.sidebar}>
          <div className={styles.sidebarHeader}>
            <div className={styles.avatarWrap}>
              <div className={styles.avatar}>{initials}</div>
              {isAdmin && <span className={styles.adminBadge}>Admin</span>}
            </div>
            <div className={styles.sidebarInfo}>
              <p className={styles.sidebarGreeting}>Hola,</p>
              <p className={styles.sidebarName}>{profile?.firstName} {profile?.lastName}</p>
            </div>
          </div>

          <nav className={styles.sidebarNav}>
            <button 
              className={`${styles.navItem} ${activeTab === 'data' ? styles.navItemActive : ''}`}
              onClick={() => setActiveTab('data')}
            >
              <UserIcon /> Datos de tu cuenta
            </button>
            <button 
              className={`${styles.navItem} ${activeTab === 'address' ? styles.navItemActive : ''}`}
              onClick={() => setActiveTab('address')}
            >
              <MapPinIcon /> Direcciones
            </button>
            <Link href="/orders" className={styles.navItem}>
              <OrdersIcon /> Compras
            </Link>
            <div className={styles.navDivider}></div>
            <button className={styles.navItemLogout} onClick={handleLogout}>
              Salir
            </button>
          </nav>
        </aside>

        {/* ── Main Content ── */}
        <main className={styles.mainContent}>
          
          {activeTab === 'data' && (
            <section className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Datos de tu cuenta</h2>
                <p className={styles.sectionDesc}>
                  Información personal asociada a tu perfil.
                </p>
              </div>

              <div className={styles.cardList}>
                <div className={styles.dataCard}>
                  <div className={styles.dataCardContent}>
                    <p className={styles.dataLabel}>Email</p>
                    <p className={styles.dataValue}>{profile?.email}</p>
                  </div>
                  <div className={styles.dataCardStatus}>
                    <CheckCircleIcon /> Validado
                  </div>
                </div>
                
                <div className={styles.dataCard}>
                  <div className={styles.dataCardContent}>
                    <p className={styles.dataLabel}>Nombre y apellido</p>
                    <p className={styles.dataValue}>{profile?.firstName} {profile?.lastName}</p>
                  </div>
                </div>
              </div>
            </section>
          )}

          {activeTab === 'address' && (
            <section className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>Direcciones</h2>
                <p className={styles.sectionDesc}>
                  Tus direcciones guardadas para recibir tus compras.
                </p>
              </div>

              {/* Current address preview */}
              {hasAddress && (
                <div className={styles.addressPreviewCard}>
                  <div className={styles.addressPreviewIcon}>
                    <MapPinIcon />
                  </div>
                  <div className={styles.addressPreviewContent}>
                    <p className={styles.previewStreet}>{profile?.address?.address}</p>
                    <p className={styles.previewDetails}>
                      {[profile?.address?.zipCode, profile?.address?.city, profile?.address?.state, profile?.address?.country].filter(Boolean).join(' - ')}
                    </p>
                  </div>
                </div>
              )}

              <div className={styles.formContainer}>
                <h3 className={styles.formTitle}>{hasAddress ? 'Editar dirección' : 'Agregar dirección'}</h3>
                <form onSubmit={handleSubmit} className={styles.form}>
                  {/* Street */}
                  <div className={styles.field}>
                    <label className={styles.label} htmlFor="address">
                      Calle y número <span className={styles.optional}>*</span>
                    </label>
                    <input
                      id="address"
                      name="address"
                      className={styles.input}
                      placeholder="Ej: Av. Corrientes 1234, Piso 2"
                      value={form.address}
                      onChange={handleChange}
                      autoComplete="street-address"
                    />
                  </div>

                  {/* City + State */}
                  <div className={styles.twoCol}>
                    <div className={styles.field}>
                      <label className={styles.label} htmlFor="city">Ciudad</label>
                      <input
                        id="city"
                        name="city"
                        className={styles.input}
                        placeholder="Buenos Aires"
                        value={form.city}
                        onChange={handleChange}
                        autoComplete="address-level2"
                      />
                    </div>
                    <div className={styles.field}>
                      <label className={styles.label} htmlFor="state">Provincia / Estado</label>
                      <input
                        id="state"
                        name="state"
                        className={styles.input}
                        placeholder="CABA"
                        value={form.state}
                        onChange={handleChange}
                        autoComplete="address-level1"
                      />
                    </div>
                  </div>

                  {/* Zip + Country */}
                  <div className={styles.twoCol}>
                    <div className={styles.field}>
                      <label className={styles.label} htmlFor="zipCode">Código postal</label>
                      <input
                        id="zipCode"
                        name="zipCode"
                        className={styles.input}
                        placeholder="1414"
                        value={form.zipCode}
                        onChange={handleChange}
                        autoComplete="postal-code"
                      />
                    </div>
                    <div className={styles.field}>
                      <label className={styles.label} htmlFor="country">País</label>
                      <input
                        id="country"
                        name="country"
                        className={styles.input}
                        placeholder="Argentina"
                        value={form.country}
                        onChange={handleChange}
                        autoComplete="country-name"
                      />
                    </div>
                  </div>

                  {/* Feedback */}
                  {error && (
                    <div className={styles.alertError}>
                      <span>⚠</span> {error}
                    </div>
                  )}
                  {success && (
                    <div className={styles.alertSuccess}>
                      <CheckCircleIcon /> Dirección guardada con éxito
                    </div>
                  )}

                  <div className={styles.formActions}>
                    <button
                      type="submit"
                      className={styles.saveBtn}
                      disabled={saving}
                    >
                      {saving ? (
                        <>
                          <span className={styles.spinner} /> Guardando...
                        </>
                      ) : (
                        'Guardar dirección'
                      )}
                    </button>
                  </div>
                </form>
              </div>
            </section>
          )}
        </main>
      </div>
    </div>
  );
}

/* ── Icons ── */
function MapPinIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z" />
      <circle cx="12" cy="10" r="3" />
    </svg>
  );
}

function OrdersIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 11l3 3L22 4" />
      <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
      <circle cx="12" cy="7" r="4"></circle>
    </svg>
  );
}

function CheckCircleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
      <polyline points="22 4 12 14.01 9 11.01"></polyline>
    </svg>
  );
}

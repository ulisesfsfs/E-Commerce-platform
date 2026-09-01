'use client';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useCart } from '@/context/CartContext';
import { useLanguage } from '@/context/LanguageContext';
import LanguageSelector from '@/components/LanguageSelector';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { cart, openCart } = useCart();
  const { t } = useLanguage();
  const router = useRouter();
  const itemCount = cart?.totalItems ?? 0;

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <header className={styles.navbar}>
      <div className={`container ${styles.inner}`}>
        {/* Logo */}
        <Link href="/" className={styles.logo}>
          <span className={styles.logoIcon}>
            <LogoIcon />
          </span>
          <span className={styles.logoText}>NEXUS</span>
        </Link>

        {/* Nav Links */}
        <nav className={styles.nav}>
          <Link href="/" className={styles.navLink}>{t('nav.products')}</Link>
          {user && <Link href="/orders" className={styles.navLink}>{t('nav.orders')}</Link>}
          {user && <Link href="/profile" className={styles.navLink}>{t('nav.profile')}</Link>}
          {user?.roles?.includes('ROLE_ADMIN') && (
            <Link href="/admin" className={styles.navLink}>
              {t('nav.admin')}
            </Link>
          )}
        </nav>

        {/* Actions */}
        <div className={styles.actions}>
          <LanguageSelector />
          {user ? (
            <>
              <span className={styles.greeting}>{t('nav.hello')}, {user.firstName}</span>
              <button
                id="cart-btn"
                className={styles.cartBtn}
                onClick={openCart}
                aria-label="Abrir carrito"
              >
                <CartIcon />
                {itemCount > 0 && (
                  <span className={styles.cartBadge}>{itemCount > 99 ? '99+' : itemCount}</span>
                )}
              </button>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                {t('nav.logout')}
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="btn btn-ghost btn-sm">{t('nav.login')}</Link>
              <Link href="/register" className="btn btn-primary btn-sm">{t('nav.register')}</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

function LogoIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="12 2 2 7 12 12 22 7 12 2" />
      <polyline points="2 17 12 22 22 17" />
      <polyline points="2 12 12 17 22 12" />
    </svg>
  );
}

function CartIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
      <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
    </svg>
  );
}

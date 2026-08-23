'use client';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useCart } from '@/context/CartContext';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { cart, openCart } = useCart();
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
          <span className={styles.logoIcon}>⚡</span>
          <span className={styles.logoText}>ShopEx</span>
        </Link>

        {/* Nav Links */}
        <nav className={styles.nav}>
          <Link href="/" className={styles.navLink}>Productos</Link>
          {user && <Link href="/orders" className={styles.navLink}>Mis Órdenes</Link>}
          {user && <Link href="/profile" className={styles.navLink}>Mi Perfil</Link>}
        </nav>

        {/* Actions */}
        <div className={styles.actions}>
          {user ? (
            <>
              <span className={styles.greeting}>Hola, {user.firstName}</span>
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
                Salir
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="btn btn-ghost btn-sm">Ingresar</Link>
              <Link href="/register" className="btn btn-primary btn-sm">Registrarse</Link>
            </>
          )}
        </div>
      </div>
    </header>
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

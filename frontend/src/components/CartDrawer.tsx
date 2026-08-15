'use client';
import { useCart } from '@/context/CartContext';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import styles from './CartDrawer.module.css';

export default function CartDrawer() {
  const { cart, isOpen, closeCart, updateQuantity, removeItem, loading } = useCart();
  const { user } = useAuth();
  const router = useRouter();

  const handleCheckout = () => {
    closeCart();
    router.push('/checkout');
  };

  return (
    <>
      {/* Backdrop */}
      <div
        className={`${styles.backdrop} ${isOpen ? styles.visible : ''}`}
        onClick={closeCart}
        aria-hidden
      />

      {/* Drawer */}
      <aside className={`${styles.drawer} ${isOpen ? styles.open : ''}`} aria-label="Carrito de compras">
        <div className={styles.header}>
          <h3>Carrito</h3>
          <button className={styles.closeBtn} onClick={closeCart} aria-label="Cerrar carrito">
            <CloseIcon />
          </button>
        </div>

        <div className={styles.body}>
          {loading ? (
            <div className="flex-center" style={{ padding: '60px 0' }}>
              <div className="spinner spinner-lg" />
            </div>
          ) : !user ? (
            <div className="empty-state">
              <div className="empty-state-icon">🔐</div>
              <h3>Iniciá sesión</h3>
              <p>Para ver tu carrito necesitás estar logueado.</p>
            </div>
          ) : !cart || cart.items.length === 0 ? (
            <div className="empty-state">
              <div className="empty-state-icon">🛒</div>
              <h3>Tu carrito está vacío</h3>
              <p>Explorá los productos y agregá algo.</p>
            </div>
          ) : (
            <ul className={styles.itemList}>
              {cart.items.map((item) => (
                <li key={item.productId} className={styles.item}>
                  <div className={styles.itemImage}>
                    {item.imageUrl
                      ? <img src={item.imageUrl} alt={item.productName} />
                      : <ProductPlaceholder />
                    }
                  </div>
                  <div className={styles.itemInfo}>
                    <span className={styles.itemName}>{item.productName}</span>
                    <span className={styles.itemPrice}>${item.unitPrice.toFixed(2)}</span>
                    <div className={styles.itemActions}>
                      <div className={styles.qtyControl}>
                        <button onClick={() => updateQuantity(item.productId, item.quantity - 1)}>−</button>
                        <span>{item.quantity}</span>
                        <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
                      </div>
                      <button
                        className={styles.removeBtn}
                        onClick={() => removeItem(item.productId)}
                        aria-label="Eliminar"
                      >
                        <TrashIcon />
                      </button>
                    </div>
                  </div>
                  <div className={styles.itemSubtotal}>${item.subtotal.toFixed(2)}</div>
                </li>
              ))}
            </ul>
          )}
        </div>

        {cart && cart.items.length > 0 && (
          <div className={styles.footer}>
            <div className={styles.summary}>
              <div className={styles.summaryRow}>
                <span>Subtotal ({cart.totalItems} items)</span>
                <span>${cart.totalPrice.toFixed(2)}</span>
              </div>
            </div>
            <button id="checkout-btn" className="btn btn-primary btn-full btn-lg" onClick={handleCheckout}>
              Ir al checkout
            </button>
          </div>
        )}
      </aside>
    </>
  );
}

function CloseIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
      <path d="M18 6 6 18M6 6l12 12"/>
    </svg>
  );
}
function TrashIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <polyline points="3 6 5 6 21 6"/><path d="m19 6-.867 13.142A2 2 0 0 1 16.138 21H7.862a2 2 0 0 1-1.995-1.858L5 6m5 0V4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v2"/>
    </svg>
  );
}
function ProductPlaceholder() {
  return (
    <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem', opacity: 0.3 }}>
      📦
    </div>
  );
}

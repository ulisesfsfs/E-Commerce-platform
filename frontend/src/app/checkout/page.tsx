'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useCart } from '@/context/CartContext';
import { ordersApi, paymentsApi, cartApi, userApi } from '@/lib/api';
import Link from 'next/link';
import styles from './page.module.css';

const PAYMENT_METHODS = [
  { id: 'CREDIT_CARD', label: 'Tarjeta de Crédito', icon: '💳' },
  { id: 'DEBIT_CARD', label: 'Tarjeta de Débito', icon: '🏦' },
  { id: 'PAYPAL', label: 'PayPal / Digital Wallet', icon: '📱' },
  { id: 'BANK_TRANSFER', label: 'Transferencia Bancaria', icon: '💸' },
];

import { useToast } from '@/context/ToastContext';
import { CheckoutSkeleton } from '@/components/skeletons/CheckoutSkeleton';

export default function CheckoutPage() {
  const { user, loading: authLoading } = useAuth();
  const { cart, loading: cartLoading, refresh } = useCart();
  const { addToast } = useToast();
  const router = useRouter();

  const [shippingAddress, setShippingAddress] = useState('');
  const [profileLoading, setProfileLoading] = useState(true);
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD');
  const [cardNumber, setCardNumber] = useState('');
  const [cardExpiry, setCardExpiry] = useState('');
  const [cardCvc, setCardCvc] = useState('');

  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!authLoading && !user) {
      router.push('/login');
    }
  }, [user, authLoading, router]);

  useEffect(() => {
    if (user) {
      userApi.getProfile()
        .then(profile => {
          if (profile.address && profile.address.address && !shippingAddress) {
             const a = profile.address;
             const formatted = [a.address, a.city, a.state, a.zipCode, a.country].filter(Boolean).join(', ');
             setShippingAddress(formatted);
          }
        })
        .catch(e => console.error("Error fetching profile for checkout:", e))
        .finally(() => setProfileLoading(false));
    } else if (!authLoading) {
      setProfileLoading(false);
    }
  }, [user, authLoading]);

  if (authLoading || cartLoading || profileLoading) {
    return <CheckoutSkeleton />;
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="page-content">
        <div className="container">
          <div className="empty-state">
            <div className="empty-state-icon">🛒</div>
            <h3>Tu carrito está vacío</h3>
            <p>Agregá productos antes de iniciar el checkout.</p>
            <Link href="/" className="btn btn-primary" style={{ marginTop: '16px' }}>
              Explorar productos
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    if (!shippingAddress.trim()) {
      const msg = 'Por favor ingresá la dirección de envío.';
      setError(msg);
      addToast(msg, 'warning');
      return;
    }

    setProcessing(true);
    setError('');
    addToast('Procesando tu pago de forma segura...', 'info');

    try {
      // 1. Crear Orden en Order Service
      const order = await ordersApi.create(user.userId, {
        shippingAddress: shippingAddress.trim(),
      });

      // 2. Procesar Pago en Payment Service
      const idempotencyKey = `PAY-${order.id}-${Date.now()}`;
      await paymentsApi.process({
        orderId: order.id,
        userId: user.userId,
        amount: order.totalAmount,
        paymentMethod,
        idempotencyKey,
      });

      // 3. Clear cart backend & refresh local state
      try {
        await cartApi.clear(user.userId);
      } catch (cartErr) {
        console.warn('Cart clear warning:', cartErr);
      }
      await refresh();

      addToast('¡Compra realizada con éxito! 🛍️', 'success');

      // Redirect to Order Detail Page
      router.push(`/orders/${order.id}?success=true`);
    } catch (err: any) {
      console.error(err);
      const errMsg = err.message || 'Ocurrió un error al procesar la orden o el pago.';
      setError(errMsg);
      addToast(errMsg, 'error');
      setProcessing(false);
    }
  };

  return (
    <div className="page-content">
      <div className="container">
        <h1 className={styles.title}>Checkout</h1>
        <p className={styles.sub}>Finalizá tu compra completando tus datos de envío y pago</p>

        {error && <div className="alert alert-error" style={{ marginBottom: '24px' }}>{error}</div>}

        <form onSubmit={handleCheckout} className={styles.grid}>
          {/* Main Form Details */}
          <div className={styles.mainCol}>
            {/* Section 1: Shipping Address */}
            <div className="card">
              <h2 className={styles.sectionTitle}>
                <span className={styles.stepNum}>1</span> Dirección de Envío
              </h2>
              <div className="form-group">
                <label htmlFor="address" className="form-label">
                  Calle, número, depto, ciudad y código postal
                </label>
                <textarea
                  id="address"
                  className="form-input"
                  rows={3}
                  placeholder="Ej: Av. Corrientes 1234, 4B, CABA, Argentina"
                  value={shippingAddress}
                  onChange={(e) => setShippingAddress(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* Section 2: Payment Method */}
            <div className="card" style={{ marginTop: '24px' }}>
              <h2 className={styles.sectionTitle}>
                <span className={styles.stepNum}>2</span> Método de Pago
              </h2>

              <div className={styles.methodGrid}>
                {PAYMENT_METHODS.map((m) => (
                  <button
                    type="button"
                    key={m.id}
                    className={`${styles.methodCard} ${paymentMethod === m.id ? styles.methodActive : ''}`}
                    onClick={() => setPaymentMethod(m.id)}
                  >
                    <span className={styles.methodIcon}>{m.icon}</span>
                    <span className={styles.methodLabel}>{m.label}</span>
                  </button>
                ))}
              </div>

              {(paymentMethod === 'CREDIT_CARD' || paymentMethod === 'DEBIT_CARD') && (
                <div className={styles.cardFields}>
                  <div className="form-group">
                    <label htmlFor="cardNumber" className="form-label">Número de Tarjeta</label>
                    <input
                      id="cardNumber"
                      type="text"
                      className="form-input"
                      placeholder="4500 0000 0000 0000"
                      value={cardNumber}
                      onChange={(e) => setCardNumber(e.target.value)}
                    />
                  </div>
                  <div className={styles.cardRow}>
                    <div className="form-group">
                      <label htmlFor="cardExpiry" className="form-label">Vencimiento</label>
                      <input
                        id="cardExpiry"
                        type="text"
                        className="form-input"
                        placeholder="MM/AA"
                        value={cardExpiry}
                        onChange={(e) => setCardExpiry(e.target.value)}
                      />
                    </div>
                    <div className="form-group">
                      <label htmlFor="cardCvc" className="form-label">CVC</label>
                      <input
                        id="cardCvc"
                        type="text"
                        className="form-input"
                        placeholder="123"
                        value={cardCvc}
                        onChange={(e) => setCardCvc(e.target.value)}
                      />
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Sidebar Summary */}
          <div className={styles.sidebarCol}>
            <div className="card">
              <h2 className={styles.summaryTitle}>Resumen de Orden</h2>
              <div className={styles.itemList}>
                {cart.items.map((item) => (
                  <div key={item.productId} className={styles.summaryItem}>
                    <div>
                      <div className={styles.summaryItemName}>{item.productName}</div>
                      <div className={styles.summaryItemQty}>Cant: {item.quantity} × ${item.unitPrice.toFixed(2)}</div>
                    </div>
                    <div className={styles.summaryItemSubtotal}>${item.subtotal.toFixed(2)}</div>
                  </div>
                ))}
              </div>

              <div className={styles.divider} />

              <div className={styles.totalRow}>
                <span>Total a pagar</span>
                <span className={styles.totalAmount}>${cart.totalPrice.toFixed(2)}</span>
              </div>

              <button
                id="submit-order-btn"
                type="submit"
                className="btn btn-primary btn-lg btn-full"
                disabled={processing}
                style={{ marginTop: '24px' }}
              >
                {processing ? (
                  <>
                    <div className="spinner" /> Procesando pago...
                  </>
                ) : (
                  `Pagar $${cart.totalPrice.toFixed(2)}`
                )}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

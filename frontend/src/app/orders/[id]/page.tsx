'use client';
import { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { ordersApi, Order } from '@/lib/api';
import Link from 'next/link';
import styles from '../orders.module.css';

export default function OrderDetailPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const orderId = Number(params.id);
  const isSuccessRedirect = searchParams.get('success') === 'true';

  const { user, loading: authLoading } = useAuth();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!orderId) return;
    const fetchOrder = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await ordersApi.get(orderId);
        setOrder(data);
      } catch (err: any) {
        setError('No se pudo cargar la orden requerida.');
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();

    // If redirected from checkout success, poll status every 2 seconds (3 times) to see if Kafka event updated status to CONFIRMED
    if (isSuccessRedirect) {
      const interval = setInterval(async () => {
        try {
          const updated = await ordersApi.get(orderId);
          setOrder(updated);
        } catch {}
      }, 2500);
      return () => clearInterval(interval);
    }
  }, [orderId, isSuccessRedirect]);

  if (authLoading || loading) {
    return (
      <div className="page-content flex-center" style={{ minHeight: '60vh' }}>
        <div className="spinner spinner-lg" />
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="page-content">
        <div className="container">
          <div className="alert alert-error" style={{ marginBottom: '20px' }}>
            {error || 'Orden no encontrada.'}
          </div>
          <Link href="/orders" className="btn btn-ghost">
            ← Volver a Mis Órdenes
          </Link>
        </div>
      </div>
    );
  }

  const getStatusBadge = (status: string) => {
    switch (status.toUpperCase()) {
      case 'CONFIRMED':
      case 'PAID':
      case 'DELIVERED':
        return <span className="badge badge-success">{status}</span>;
      case 'PENDING':
        return <span className="badge badge-warning">{status}</span>;
      case 'CANCELLED':
      case 'FAILED':
        return <span className="badge badge-danger">{status}</span>;
      default:
        return <span className="badge badge-primary">{status}</span>;
    }
  };

  return (
    <div className="page-content">
      <div className="container">
        {isSuccessRedirect && (
          <div className="alert alert-success fade-up" style={{ marginBottom: '24px' }}>
            🎉 ¡Orden creada y pago procesado con éxito! Se ha enviado una confirmación a tu correo electrónico.
          </div>
        )}

        <div className={styles.detailHeader}>
          <div>
            <Link href="/orders" className="btn btn-ghost btn-sm" style={{ marginBottom: '12px' }}>
              ← Volver a Mis Órdenes
            </Link>
            <h1 className={styles.title}>Orden #{order.id}</h1>
          </div>
          <div>{getStatusBadge(order.status)}</div>
        </div>

        <div className={styles.detailGrid}>
          {/* Order Items */}
          <div className="card">
            <h2 className={styles.summaryTitle}>Productos</h2>
            <div className={styles.itemList}>
              {order.items.map((item) => (
                <div key={item.id} className={styles.itemRow}>
                  <div>
                    <div className={styles.itemName}>{item.productName}</div>
                    <div className={styles.itemMeta}>
                      {item.quantity} x ${item.unitPrice.toFixed(2)}
                    </div>
                  </div>
                  <div className={styles.itemPrice}>${item.subtotal.toFixed(2)}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Order Summary & Address */}
          <div className="card">
            <h2 className={styles.summaryTitle}>Resumen</h2>
            <div className={styles.summaryMeta}>
              <div className={styles.metaBlock}>
                <h4>Fecha</h4>
                <p>
                  {new Date(order.createdAt).toLocaleString('es-AR', {
                    dateStyle: 'medium',
                    timeStyle: 'short',
                  })}
                </p>
              </div>

              <div className={styles.metaBlock}>
                <h4>Dirección de envío</h4>
                <p>{order.shippingAddress}</p>
              </div>

              <div className={styles.divider} />

              <div className={styles.totalRow}>
                <span>Total</span>
                <span className={styles.totalAmount}>${order.totalAmount.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

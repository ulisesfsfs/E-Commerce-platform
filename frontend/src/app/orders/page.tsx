'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { ordersApi, Order } from '@/lib/api';
import Link from 'next/link';
import styles from './orders.module.css';

export default function UserOrdersPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();

  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!authLoading && !user) {
      router.push('/login');
      return;
    }

    if (user) {
      const fetchOrders = async () => {
        setLoading(true);
        setError('');
        try {
          const res = await ordersApi.getUserOrders(user.userId, 0, 20);
          setOrders(res.content);
        } catch (err: any) {
          setError('No se pudieron cargar tus órdenes.');
        } finally {
          setLoading(false);
        }
      };
      fetchOrders();
    }
  }, [user, authLoading, router]);

  if (authLoading || loading) {
    return (
      <div className="page-content flex-center" style={{ minHeight: '60vh' }}>
        <div className="spinner spinner-lg" />
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
        <h1 className={styles.title}>Mis Órdenes</h1>
        <p className={styles.sub}>Historial de compras realizadas en la plataforma</p>

        {error && <div className="alert alert-error" style={{ marginBottom: '24px' }}>{error}</div>}

        {orders.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">📦</div>
            <h3>No tenés órdenes registradas</h3>
            <p>Tus compras completadas aparecerán acá.</p>
            <Link href="/" className="btn btn-primary" style={{ marginTop: '16px' }}>
              Ir al catálogo
            </Link>
          </div>
        ) : (
          <div className={styles.orderList}>
            {orders.map((o) => (
              <div key={o.id} className={styles.orderCard}>
                <div className={styles.orderInfo}>
                  <span className={styles.orderId}>Orden #{o.id}</span>
                  <span className={styles.orderDate}>
                    {new Date(o.createdAt).toLocaleDateString('es-AR', {
                      day: '2-digit',
                      month: 'short',
                      year: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </span>
                  <span className={styles.orderItemsCount}>
                    {o.items?.length || 0} producto(s)
                  </span>
                </div>

                <div className={styles.orderRight}>
                  <div>
                    <div className={styles.orderAmount}>${o.totalAmount.toFixed(2)}</div>
                    <div style={{ textAlign: 'right', marginTop: '4px' }}>
                      {getStatusBadge(o.status)}
                    </div>
                  </div>

                  <Link href={`/orders/${o.id}`} className="btn btn-ghost btn-sm">
                    Ver detalle
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

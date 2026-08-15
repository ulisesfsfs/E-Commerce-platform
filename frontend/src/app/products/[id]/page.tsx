'use client';
import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { productsApi, Product } from '@/lib/api';
import { useCart } from '@/context/CartContext';
import { useAuth } from '@/context/AuthContext';
import Link from 'next/link';
import styles from './page.module.css';

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;
  const { user } = useAuth();
  const { addItem } = useCart();

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [addedSuccess, setAddedSuccess] = useState(false);

  useEffect(() => {
    if (!id) return;
    const fetchProduct = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await productsApi.get(id);
        setProduct(data);
      } catch (err: any) {
        setError('No se pudo cargar la información del producto.');
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!product) return;
    if (!user) {
      router.push('/login');
      return;
    }
    setAdding(true);
    setAddedSuccess(false);
    try {
      await addItem({
        productId: product.id,
        productName: product.name,
        unitPrice: product.price,
        quantity,
        imageUrl: product.imageUrl,
      });
      setAddedSuccess(true);
      setTimeout(() => setAddedSuccess(false), 3000);
    } catch (err: any) {
      setError('Error al agregar el producto al carrito.');
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="page-content flex-center" style={{ minHeight: '60vh' }}>
        <div className="spinner spinner-lg" />
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="page-content">
        <div className="container">
          <div className="alert alert-error" style={{ marginBottom: '20px' }}>
            {error || 'Producto no encontrado.'}
          </div>
          <Link href="/" className="btn btn-ghost">
            ← Volver al catálogo
          </Link>
        </div>
      </div>
    );
  }

  const inStock = product.stock > 0;

  return (
    <div className="page-content">
      <div className="container">
        <Link href="/" className={`${styles.backBtn} btn btn-ghost btn-sm`}>
          ← Volver al catálogo
        </Link>

        <div className={styles.grid}>
          {/* Image */}
          <div className={styles.imageWrap}>
            {product.imageUrl ? (
              <img src={product.imageUrl} alt={product.name} className={styles.image} />
            ) : (
              <div className={styles.placeholder}>
                <span>📦</span>
              </div>
            )}
          </div>

          {/* Details */}
          <div className={styles.info}>
            <div className={styles.badge}>{product.category}</div>
            <h1 className={styles.title}>{product.name}</h1>
            <p className={styles.price}>${product.price.toFixed(2)}</p>

            <div className={styles.stockStatus}>
              {inStock ? (
                <span className="badge badge-success">En Stock ({product.stock} unidades)</span>
              ) : (
                <span className="badge badge-danger">Sin Stock</span>
              )}
            </div>

            <div className={styles.divider} />

            <div className={styles.descSection}>
              <h3>Descripción</h3>
              <p>{product.description}</p>
            </div>

            <div className={styles.divider} />

            {/* Actions */}
            {inStock && (
              <div className={styles.actions}>
                <div className={styles.qtyControl}>
                  <label htmlFor="qty" className="form-label" style={{ marginBottom: 0 }}>
                    Cantidad:
                  </label>
                  <div className={styles.qtyButtons}>
                    <button
                      type="button"
                      onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                      disabled={quantity <= 1}
                    >
                      −
                    </button>
                    <span>{quantity}</span>
                    <button
                      type="button"
                      onClick={() => setQuantity((q) => Math.min(product.stock, q + 1))}
                      disabled={quantity >= product.stock}
                    >
                      +
                    </button>
                  </div>
                </div>

                <button
                  id="add-to-cart-detail"
                  className="btn btn-primary btn-lg btn-full"
                  onClick={handleAddToCart}
                  disabled={adding}
                >
                  {adding ? (
                    <>
                      <div className="spinner" /> Agregando...
                    </>
                  ) : (
                    'Agregar al carrito'
                  )}
                </button>
              </div>
            )}

            {addedSuccess && (
              <div className="alert alert-success fade-up" style={{ marginTop: '16px' }}>
                ✓ ¡Producto agregado al carrito!
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

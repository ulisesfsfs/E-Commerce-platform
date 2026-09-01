'use client';
import Link from 'next/link';
import { Product } from '@/lib/api';
import { useCart } from '@/context/CartContext';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useLanguage } from '@/context/LanguageContext';
import styles from './ProductCard.module.css';

import { useToast } from '@/context/ToastContext';

interface Props { product: Product; }

export default function ProductCard({ product }: Props) {
  const { cart, addItem } = useCart();
  const { user } = useAuth();
  const { addToast } = useToast();
  const { t } = useLanguage();
  const router = useRouter();
  const [adding, setAdding] = useState(false);

  const existingInCart = cart?.items.find(i => i.productId === product.id)?.quantity || 0;
  const inStock = product.stock > 0;
  const isLowStock = product.stock > 0 && product.stock < 5;

  const handleAdd = async (e: React.MouseEvent) => {
    e.preventDefault();
    if (!user) { router.push('/login'); return; }

    if (existingInCart + 1 > product.stock) {
      addToast(`Alcanzaste el límite de stock disponible (${product.stock} unidades)`, 'warning');
      return;
    }

    setAdding(true);
    try {
      await addItem({
        productId: product.id,
        productName: product.name,
        unitPrice: product.price,
        quantity: 1,
        imageUrl: product.imageUrl,
      });
      addToast(`"${product.name}" agregado al carrito 🛒`, 'success');
    } catch (err: any) {
      addToast('Error al agregar el producto al carrito', 'error');
    } finally {
      setAdding(false);
    }
  };

  return (
    <Link href={`/products/${product.id}`} className={styles.card}>
      <div className={styles.imageWrap}>
        {product.imageUrl
          ? <img src={product.imageUrl} alt={product.name} className={styles.image} />
          : <div className={styles.imagePlaceholder}><span>📦</span></div>
        }
        {!inStock && <div className={styles.outOfStock}>{t('catalog.outOfStock')}</div>}
        {isLowStock && <div className={styles.lowStockBadge}>{t('catalog.lowStock', { count: product.stock })}</div>}
        <div className={styles.categoryBadge}>{product.category}</div>
      </div>

      <div className={styles.body}>
        <h3 className={styles.name}>{product.name}</h3>
        <p className={styles.desc}>{product.description}</p>
        <div className={styles.footer}>
          <span className={styles.price}>${product.price.toFixed(2)}</span>
          <button
            id={`add-to-cart-${product.id}`}
            className={`btn btn-primary btn-sm ${styles.addBtn}`}
            onClick={handleAdd}
            disabled={!inStock || adding}
            aria-label={`Agregar ${product.name} al carrito`}
          >
            {adding ? <div className="spinner" /> : '+ Carrito'}
          </button>
        </div>
      </div>
    </Link>
  );
}

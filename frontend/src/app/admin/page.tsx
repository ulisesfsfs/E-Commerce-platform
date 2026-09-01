'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/context/ToastContext';
import { productsApi, Product } from '@/lib/api';
import styles from './page.module.css';

const CATEGORIES = ['ELECTRONICS', 'CLOTHING', 'BOOKS', 'HOME', 'SPORTS', 'BEAUTY', 'TOYS'];

export default function AdminPage() {
  const { user, loading: authLoading } = useAuth();
  const { addToast } = useToast();
  const router = useRouter();

  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  // Form State
  const [name, setName] = useState('');
  const [sku, setSku] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState('');
  const [stock, setStock] = useState('');
  const [category, setCategory] = useState(CATEGORIES[0]);
  const [imageUrl, setImageUrl] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    if (!authLoading && !isAdmin) {
      addToast('Acceso denegado. Se requieren permisos de Administrador.', 'error');
    }
  }, [authLoading, isAdmin]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const data = await productsApi.list(0, 100);
      setProducts(data.content);
    } catch (err: any) {
      addToast('Error al cargar la lista de productos', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAdmin) {
      fetchProducts();
    }
  }, [isAdmin]);

  const handleOpenCreate = () => {
    setEditingProduct(null);
    setName('');
    setSku(`SKU-${Math.floor(1000 + Math.random() * 9000)}`);
    setDescription('');
    setPrice('');
    setStock('10');
    setCategory(CATEGORIES[0]);
    setImageUrl('');
    setShowModal(true);
  };

  const handleOpenEdit = (p: Product) => {
    setEditingProduct(p);
    setName(p.name);
    setSku(p.sku || '');
    setDescription(p.description || '');
    setPrice(String(p.price));
    setStock(String(p.stock));
    setCategory(p.category);
    setImageUrl(p.imageUrl || '');
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !price || !stock || !sku) {
      addToast('Por favor completa todos los campos obligatorios', 'warning');
      return;
    }

    setSubmitting(true);
    try {
      if (editingProduct) {
        // Update
        await productsApi.update(editingProduct.id, {
          name,
          sku,
          description,
          price: Number(price),
          stock: Number(stock),
          category,
          imageUrl: imageUrl || undefined,
        });
        addToast(`Producto "${name}" actualizado correctamente ✏️`, 'success');
      } else {
        // Create
        await productsApi.create({
          name,
          sku,
          description,
          price: Number(price),
          stock: Number(stock),
          category,
          imageUrl: imageUrl || undefined,
          active: true,
        });
        addToast(`Producto "${name}" creado exitosamente 🚀`, 'success');
      }
      setShowModal(false);
      fetchProducts();
    } catch (err: any) {
      addToast(err.message || 'Error al guardar el producto', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (!confirm(`¿Estás seguro de que deseas desactivar/eliminar "${name}"?`)) return;
    try {
      await productsApi.delete(id);
      addToast(`Producto "${name}" desactivado`, 'info');
      fetchProducts();
    } catch (err: any) {
      addToast('Error al desactivar el producto', 'error');
    }
  };

  if (authLoading) {
    return <div className="container" style={{ padding: '40px 0' }}>Cargando sesión...</div>;
  }

  if (!isAdmin) {
    return (
      <div className="container" style={{ padding: '60px 0', textAlign: 'center' }}>
        <h2>🔒 Acceso Restringido</h2>
        <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>
          Se requieren permisos de <strong>Administrador (ROLE_ADMIN)</strong> para acceder a este panel.
        </p>
        <button className="btn btn-primary" style={{ marginTop: '20px' }} onClick={() => router.push('/')}>
          Volver a Inicio
        </button>
      </div>
    );
  }

  // Calculate Metrics
  const totalProducts = products.length;
  const lowStockCount = products.filter(p => p.stock > 0 && p.stock < 5).length;
  const outOfStockCount = products.filter(p => p.stock === 0).length;

  return (
    <div className="page-content">
      <div className={`container ${styles.adminContainer}`}>
        {/* Header */}
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>Panel de Administración</h1>
            <p className={styles.subtitle}>Gestión integral del catálogo de productos y stock</p>
          </div>
          <button className="btn btn-primary" onClick={handleOpenCreate}>
            + Nuevo Producto
          </button>
        </div>

        {/* Metrics Grid */}
        <div className={styles.metricsGrid}>
          <div className={styles.metricCard}>
            <span className={styles.metricLabel}>Total Productos</span>
            <span className={styles.metricValue}>{totalProducts}</span>
          </div>
          <div className={styles.metricCard}>
            <span className={styles.metricLabel}>Bajo Stock (&lt; 5)</span>
            <span className={styles.metricValue} style={{ color: '#d48806' }}>{lowStockCount}</span>
          </div>
          <div className={styles.metricCard}>
            <span className={styles.metricLabel}>Sin Stock</span>
            <span className={styles.metricValue} style={{ color: 'var(--color-danger)' }}>{outOfStockCount}</span>
          </div>
        </div>

        {/* Table Card */}
        <div className={styles.tableCard}>
          <div className={styles.tableHeaderBar}>
            <span className={styles.tableTitle}>Catálogo de Productos</span>
            <button className="btn btn-ghost btn-sm" onClick={fetchProducts}>
              🔄 Actualizar
            </button>
          </div>

          {loading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
              Cargando productos...
            </div>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Imagen</th>
                  <th>Producto</th>
                  <th>SKU</th>
                  <th>Categoría</th>
                  <th>Precio</th>
                  <th>Stock</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {products.map(p => (
                  <tr key={p.id}>
                    <td>
                      {p.imageUrl ? (
                        <img src={p.imageUrl} alt={p.name} className={styles.thumbImg} />
                      ) : (
                        <div className={styles.thumbPlaceholder}>📦</div>
                      )}
                    </td>
                    <td>
                      <strong>{p.name}</strong>
                    </td>
                    <td><code>{p.sku || 'N/A'}</code></td>
                    <td>{p.category}</td>
                    <td><strong>${p.price.toFixed(2)}</strong></td>
                    <td>
                      <span style={{ fontWeight: 600, color: p.stock === 0 ? 'var(--color-danger)' : p.stock < 5 ? '#d48806' : 'inherit' }}>
                        {p.stock} u.
                      </span>
                    </td>
                    <td>
                      {p.active !== false ? (
                        <span className={styles.badgeActive}>Activo</span>
                      ) : (
                        <span className={styles.badgeInactive}>Inactivo</span>
                      )}
                    </td>
                    <td>
                      <div className={styles.actionsCell}>
                        <button className="btn btn-ghost btn-sm" onClick={() => handleOpenEdit(p)}>
                          ✏️ Editar
                        </button>
                        {p.active !== false && (
                          <button className="btn btn-ghost btn-sm" style={{ color: 'var(--color-danger)' }} onClick={() => handleDelete(p.id, p.name)}>
                            🗑️ Desactivar
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Create / Edit Modal */}
        {showModal && (
          <div className={styles.modalOverlay} onClick={() => setShowModal(false)}>
            <div className={styles.modalContent} onClick={e => e.stopPropagation()}>
              <div className={styles.modalHeader}>
                <h3 className={styles.modalTitle}>
                  {editingProduct ? 'Editar Producto' : 'Crear Nuevo Producto'}
                </h3>
                <button className={styles.closeBtn} onClick={() => setShowModal(false)}>×</button>
              </div>

              <form onSubmit={handleSubmit}>
                <div className={styles.formGrid}>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Nombre del Producto *</label>
                    <input
                      type="text"
                      className={styles.formInput}
                      value={name}
                      onChange={e => setName(e.target.value)}
                      required
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>SKU *</label>
                    <input
                      type="text"
                      className={styles.formInput}
                      value={sku}
                      onChange={e => setSku(e.target.value)}
                      required
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Categoría *</label>
                    <select
                      className={styles.formSelect}
                      value={category}
                      onChange={e => setCategory(e.target.value)}
                    >
                      {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                    </select>
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Precio ($) *</label>
                    <input
                      type="number"
                      step="0.01"
                      className={styles.formInput}
                      value={price}
                      onChange={e => setPrice(e.target.value)}
                      required
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>Stock Unidades *</label>
                    <input
                      type="number"
                      className={styles.formInput}
                      value={stock}
                      onChange={e => setStock(e.target.value)}
                      required
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>URL de Imagen (Unsplash, CDN)</label>
                    <input
                      type="url"
                      placeholder="https://images.unsplash.com/photo-..."
                      className={styles.formInput}
                      value={imageUrl}
                      onChange={e => setImageUrl(e.target.value)}
                    />
                  </div>

                  <div className={`${styles.formGroup} ${styles.formFull}`}>
                    <label className={styles.formLabel}>Descripción</label>
                    <textarea
                      rows={3}
                      className={styles.formTextarea}
                      value={description}
                      onChange={e => setDescription(e.target.value)}
                    />
                  </div>
                </div>

                <div className={styles.modalActions}>
                  <button
                    type="button"
                    className="btn btn-ghost"
                    onClick={() => setShowModal(false)}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={submitting}
                  >
                    {submitting ? 'Guardando...' : editingProduct ? 'Guardar Cambios' : 'Crear Producto'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

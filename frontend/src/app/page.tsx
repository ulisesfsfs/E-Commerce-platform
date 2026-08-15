'use client';
import { useState, useEffect } from 'react';
import { productsApi, Product, PagedResponse } from '@/lib/api';
import ProductCard from '@/components/ProductCard';
import styles from './page.module.css';

const CATEGORIES = ['Todas', 'Electronics', 'Clothing', 'Books', 'Home', 'Sports', 'Beauty', 'Toys'];

export default function HomePage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [category, setCategory] = useState('');
  const [search, setSearch] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      setError('');
      try {
        const cat = category === 'Todas' || category === '' ? undefined : category;
        const data: PagedResponse<Product> = await productsApi.list(page, 12, cat);
        setProducts(data.content);
        setTotalPages(data.totalPages);
      } catch (e: any) {
        setError('No se pudieron cargar los productos.');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [page, category]);

  const filtered = search.trim()
    ? products.filter(p => p.name.toLowerCase().includes(search.toLowerCase()))
    : products;

  return (
    <div className="page-content">
      <div className="container">
        {/* Hero */}
        <section className={styles.hero}>
          <h1 className={styles.heroTitle}>
            Descubrí productos<br />
            <span className={styles.heroGradient}>increíbles</span>
          </h1>
          <p className={styles.heroSub}>Todo lo que necesitás, en un solo lugar.</p>

          {/* Search */}
          <div className={styles.searchWrap}>
            <SearchIcon />
            <input
              id="search-input"
              type="text"
              placeholder="Buscar productos..."
              className={styles.searchInput}
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
        </section>

        {/* Category filter */}
        <div className={styles.categories}>
          {CATEGORIES.map(cat => (
            <button
              key={cat}
              id={`cat-${cat.toLowerCase()}`}
              className={`${styles.catBtn} ${(category === cat || (cat === 'Todas' && !category)) ? styles.catActive : ''}`}
              onClick={() => { setCategory(cat === 'Todas' ? '' : cat); setPage(0); }}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Error */}
        {error && <div className="alert alert-error" style={{ marginBottom: '24px' }}>{error}</div>}

        {/* Grid */}
        {loading ? (
          <div className={styles.loadingGrid}>
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className={styles.skeleton} />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🔍</div>
            <h3>Sin resultados</h3>
            <p>Probá con otra búsqueda o categoría.</p>
          </div>
        ) : (
          <div className={`grid-products fade-up`}>
            {filtered.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && !search && (
          <div className={styles.pagination}>
            <button
              className="btn btn-ghost"
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
            >
              ← Anterior
            </button>
            <span className={styles.pageInfo}>{page + 1} / {totalPages}</span>
            <button
              className="btn btn-ghost"
              disabled={page >= totalPages - 1}
              onClick={() => setPage(p => p + 1)}
            >
              Siguiente →
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

function SearchIcon() {
  return (
    <svg className={styles.searchIcon} width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
    </svg>
  );
}

'use client';
import { useState, useEffect } from 'react';
import { productsApi, Product, PagedResponse } from '@/lib/api';
import ProductCard from '@/components/ProductCard';
import { useLanguage } from '@/context/LanguageContext';
import styles from './page.module.css';

const CATEGORIES = ['Todas', 'ELECTRONICS', 'CLOTHING', 'BOOKS', 'HOME', 'SPORTS', 'BEAUTY', 'TOYS'];

export default function HomePage() {
  const { t, locale } = useLanguage();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [category, setCategory] = useState('');
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');
  const [sortBy, setSortBy] = useState('newest');
  const [error, setError] = useState('');

  // Debounce search input by 350ms
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 350);
    return () => clearTimeout(handler);
  }, [search]);

  // Fetch filtered products from server
  useEffect(() => {
    const fetchFiltered = async () => {
      setLoading(true);
      setError('');
      try {
        const catParam = category === 'Todas' || category === '' ? undefined : category;
        const minP = minPrice !== '' ? Number(minPrice) : undefined;
        const maxP = maxPrice !== '' ? Number(maxPrice) : undefined;

        const data: PagedResponse<Product> = await productsApi.filter({
          search: debouncedSearch,
          category: catParam,
          minPrice: minP,
          maxPrice: maxP,
          sortBy,
          page,
          size: 12,
        });

        setProducts(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      } catch (e: any) {
        setError('No se pudieron cargar los productos.');
      } finally {
        setLoading(false);
      }
    };

    fetchFiltered();
  }, [page, category, debouncedSearch, minPrice, maxPrice, sortBy]);

  const handleResetFilters = () => {
    setSearch('');
    setCategory('');
    setMinPrice('');
    setMaxPrice('');
    setSortBy('newest');
    setPage(0);
  };

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
              placeholder={t('catalog.searchPlaceholder')}
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
              {cat === 'Todas' ? t('catalog.allCategories') :
               cat === 'ELECTRONICS' ? (locale === 'es' ? 'Electrónica' : 'Electronics') :
               cat === 'CLOTHING' ? (locale === 'es' ? 'Ropa' : 'Clothing') :
               cat === 'BOOKS' ? (locale === 'es' ? 'Libros' : 'Books') :
               cat === 'HOME' ? (locale === 'es' ? 'Hogar' : 'Home') :
               cat === 'SPORTS' ? (locale === 'es' ? 'Deportes' : 'Sports') :
               cat === 'BEAUTY' ? (locale === 'es' ? 'Belleza' : 'Beauty') :
               cat === 'TOYS' ? (locale === 'es' ? 'Juguetes' : 'Toys') : cat}
            </button>
          ))}
        </div>

        {/* Advanced Filters & Sorting Controls */}
        <div className={styles.filterControlsBar}>
          {/* Price range inputs */}
          <div className={styles.priceGroup}>
            <span>{t('catalog.priceRange')}:</span>
            <input
              type="number"
              placeholder={`${t('catalog.minPrice')} ($)`}
              className={styles.priceInput}
              value={minPrice}
              onChange={e => { setMinPrice(e.target.value); setPage(0); }}
            />
            <span>-</span>
            <input
              type="number"
              placeholder={`${t('catalog.maxPrice')} ($)`}
              className={styles.priceInput}
              value={maxPrice}
              onChange={e => { setMaxPrice(e.target.value); setPage(0); }}
            />
          </div>

          {/* Sorting dropdown */}
          <div className={styles.sortGroup}>
            <span>{t('catalog.sortBy')}:</span>
            <select
              className={styles.selectInput}
              value={sortBy}
              onChange={e => { setSortBy(e.target.value); setPage(0); }}
            >
              <option value="newest">{t('catalog.sortNewest')}</option>
              <option value="price_asc">{t('catalog.sortPriceAsc')}</option>
              <option value="price_desc">{t('catalog.sortPriceDesc')}</option>
              <option value="name_asc">{t('catalog.sortName')}</option>
            </select>

            {(search || category || minPrice || maxPrice || sortBy !== 'newest') && (
              <button className="btn btn-ghost btn-sm" onClick={handleResetFilters}>
                Limpiar filtros
              </button>
            )}
          </div>
        </div>

        {/* Error */}
        {error && <div className="alert alert-error" style={{ marginBottom: '24px' }}>{error}</div>}

        {/* Results Info */}
        {!loading && (
          <div style={{ marginBottom: '16px', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            Se encontraron <strong>{totalElements}</strong> productos.
          </div>
        )}

        {/* Grid */}
        {loading ? (
          <div className={styles.loadingGrid}>
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className={styles.skeleton} />
            ))}
          </div>
        ) : products.length === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">🔍</div>
            <h3>Sin resultados</h3>
            <p>Probá ajustando tus criterios de búsqueda o rango de precios.</p>
          </div>
        ) : (
          <div className={`grid-products fade-up`}>
            {products.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className={styles.pagination}>
            <button
              className="btn btn-ghost"
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
            >
              ← Anterior
            </button>
            <span className={styles.pageInfo}>Página {page + 1} de {totalPages}</span>
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

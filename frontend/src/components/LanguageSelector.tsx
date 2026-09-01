'use client';
import { useLanguage } from '@/context/LanguageContext';
import styles from './LanguageSelector.module.css';

export default function LanguageSelector() {
  const { locale, setLocale } = useLanguage();

  return (
    <div className={styles.langToggle} aria-label="Seleccionar idioma">
      <button
        type="button"
        className={`${styles.langBtn} ${locale === 'es' ? styles.active : ''}`}
        onClick={() => setLocale('es')}
      >
        ES
      </button>
      <button
        type="button"
        className={`${styles.langBtn} ${locale === 'en' ? styles.active : ''}`}
        onClick={() => setLocale('en')}
      >
        EN
      </button>
    </div>
  );
}

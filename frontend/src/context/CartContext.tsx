'use client';
import { createContext, useContext, useEffect, useState, ReactNode, useCallback } from 'react';
import { cartApi, Cart, CartItem } from '@/lib/api';
import { useAuth } from './AuthContext';

interface CartContextValue {
  cart: Cart | null;
  loading: boolean;
  isOpen: boolean;
  openCart: () => void;
  closeCart: () => void;
  addItem: (item: { productId: string; productName: string; unitPrice: number; quantity: number; imageUrl?: string }) => Promise<void>;
  updateQuantity: (productId: string, quantity: number) => Promise<void>;
  removeItem: (productId: string) => Promise<void>;
  refresh: () => Promise<void>;
}

const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const refresh = useCallback(async () => {
    if (!user) { setCart(null); return; }
    try {
      setLoading(true);
      const data = await cartApi.get(user.userId);
      setCart(data);
    } catch {
      setCart(null);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => { refresh(); }, [refresh]);

  const addItem = async (item: Parameters<CartContextValue['addItem']>[0]) => {
    if (!user) return;
    const data = await cartApi.addItem(user.userId, item);
    setCart(data);
    setIsOpen(true);
  };

  const updateQuantity = async (productId: string, quantity: number) => {
    if (!user) return;
    if (quantity <= 0) return removeItem(productId);
    const data = await cartApi.updateQuantity(user.userId, productId, quantity);
    setCart(data);
  };

  const removeItem = async (productId: string) => {
    if (!user) return;
    const data = await cartApi.removeItem(user.userId, productId);
    setCart(data);
  };

  return (
    <CartContext.Provider value={{
      cart, loading, isOpen,
      openCart: () => setIsOpen(true),
      closeCart: () => setIsOpen(false),
      addItem, updateQuantity, removeItem, refresh,
    }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used within CartProvider');
  return ctx;
}

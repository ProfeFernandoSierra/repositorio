import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import type { CartItem, Product } from '../types';
import { readStorage } from '../utils';

const STORAGE_KEY = 'pokestore_cart_v2';

interface CartValue {
  items: CartItem[];
  addToCart: (product: Product) => void;
  setQuantity: (productId: number, quantity: number) => void;
  removeFromCart: (productId: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}

const CartContext = createContext<CartValue | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(() =>
    // Se filtran entradas con forma inválida (datos viejos o editados a mano)
    readStorage<CartItem[]>(STORAGE_KEY, []).filter((i) => i?.product?.id && i.quantity > 0),
  );

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }, [items]);

  function addToCart(product: Product) {
    setItems((prev) => {
      const existing = prev.find((i) => i.product.id === product.id);
      if (!existing) return product.stock > 0 ? [...prev, { product, quantity: 1 }] : prev;
      if (existing.quantity >= product.stock) return prev;
      return prev.map((i) => (i.product.id === product.id ? { ...i, quantity: i.quantity + 1 } : i));
    });
  }

  function setQuantity(productId: number, quantity: number) {
    setItems((prev) =>
      prev.map((i) => {
        if (i.product.id !== productId) return i;
        const safe = Math.min(Math.max(1, Math.floor(quantity)), i.product.stock, 99);
        return { ...i, quantity: safe };
      }),
    );
  }

  const removeFromCart = (productId: number) => setItems((prev) => prev.filter((i) => i.product.id !== productId));
  const clearCart = () => setItems([]);

  const totalItems = items.reduce((acc, i) => acc + i.quantity, 0);
  // Total referencial: el total real lo calcula el backend al comprar
  const totalPrice = items.reduce((acc, i) => acc + i.quantity * i.product.price, 0);

  return (
    <CartContext.Provider value={{ items, addToCart, setQuantity, removeFromCart, clearCart, totalItems, totalPrice }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart debe usarse dentro de <CartProvider>');
  return ctx;
}

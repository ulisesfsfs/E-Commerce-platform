// src/lib/api.ts
const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, { ...options, headers });
  } catch (e: any) {
    throw new Error(`Error de conexión con el servidor (${BASE_URL}). Verificá que los contenedores o el Gateway estén corriendo.`);
  }

  if (!res.ok) {
    const rawError = await res.text();
    let msg = rawError;
    try {
      const parsed = JSON.parse(rawError);
      if (Array.isArray(parsed.errors) && parsed.errors.length > 0) {
        msg = parsed.errors.map((e: any) => e.defaultMessage || e.message).join(', ');
      } else {
        msg = parsed.message || parsed.error || rawError;
      }
    } catch {}

    if (msg.includes('Email is already registered')) {
      msg = 'Este email ya está registrado. Intentá iniciar sesión.';
    } else if (msg.includes('Invalid email or password')) {
      msg = 'Email o contraseña incorrectos.';
    } else if (msg.includes('Invalid email format')) {
      msg = 'El formato de email no es válido.';
    }

    throw new Error(msg || `HTTP ${res.status}`);
  }

  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text);
}

// ---- Auth ----
export const authApi = {
  register: (body: { firstName: string; lastName: string; email: string; password: string }) =>
    request<{ token: string; userId: string | number; email: string; firstName?: string }>('/api/users/auth/register', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  login: (body: { email: string; password: string }) =>
    request<{ token: string; userId: string; email: string; firstName: string }>('/api/users/auth/login', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  profile: () =>
    request<{ userId: string; email: string; firstName: string; lastName: string }>('/api/users/profile'),
};

// ---- User Profile ----
export interface Address {
  address: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  address?: Address | null;
}

export const userApi = {
  getProfile: () => request<UserProfile>('/api/users/profile'),

  updateAddress: (body: Address) =>
    request<void>('/api/users/profile/address', {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
};

// ---- Products ----
export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  category: string;
  sku?: string;
  active?: boolean;
  imageUrl?: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ProductFilterParams {
  search?: string;
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: string;
  page?: number;
  size?: number;
}

export const productsApi = {
  list: (page = 0, size = 12, category?: string) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (category) params.set('category', category);
    return request<PagedResponse<Product>>(`/api/products?${params}`);
  },

  filter: (params: ProductFilterParams = {}) => {
    const query = new URLSearchParams();
    if (params.search?.trim()) query.set('search', params.search.trim());
    if (params.category && params.category !== 'Todas') query.set('category', params.category);
    if (params.minPrice !== undefined && params.minPrice !== null && !isNaN(params.minPrice)) {
      query.set('minPrice', String(params.minPrice));
    }
    if (params.maxPrice !== undefined && params.maxPrice !== null && !isNaN(params.maxPrice)) {
      query.set('maxPrice', String(params.maxPrice));
    }
    if (params.sortBy) query.set('sortBy', params.sortBy);
    query.set('page', String(params.page || 0));
    query.set('size', String(params.size || 12));
    return request<PagedResponse<Product>>(`/api/products/filter?${query.toString()}`);
  },

  get: (id: string) => request<Product>(`/api/products/${id}`),

  create: (body: Omit<Product, 'id'>) =>
    request<Product>('/api/products', { method: 'POST', body: JSON.stringify(body) }),

  update: (id: string, body: Partial<Product>) =>
    request<Product>(`/api/products/${id}`, { method: 'PATCH', body: JSON.stringify(body) }),

  delete: (id: string) =>
    request<void>(`/api/products/${id}`, { method: 'DELETE' }),
};

// ---- Cart ----
export interface CartItem {
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  imageUrl?: string;
  subtotal: number;
}

export interface Cart {
  userId: string;
  items: CartItem[];
  totalPrice: number;
  totalItems: number;
}

export const cartApi = {
  get: (userId: string) => request<Cart>(`/api/carts/${userId}`),

  addItem: (
    userId: string,
    body: { productId: string; productName: string; unitPrice: number; quantity: number; imageUrl?: string }
  ) =>
    request<Cart>(`/api/carts/${userId}/items`, { method: 'POST', body: JSON.stringify(body) }),

  updateQuantity: (userId: string, productId: string, quantity: number) =>
    request<Cart>(`/api/carts/${userId}/items/${productId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }),

  removeItem: (userId: string, productId: string) =>
    request<Cart>(`/api/carts/${userId}/items/${productId}`, { method: 'DELETE' }),

  clear: (userId: string) =>
    request<void>(`/api/carts/${userId}`, { method: 'DELETE' }),
};

// ---- Orders ----
export interface OrderItem {
  id: number;
  productId: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userId: string;
  shippingAddress: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
}

export const ordersApi = {
  create: (userId: string, body: { shippingAddress: string }) =>
    request<Order>(`/api/orders/${userId}`, { method: 'POST', body: JSON.stringify(body) }),

  get: (orderId: number) => request<Order>(`/api/orders/${orderId}`),

  getUserOrders: (userId: string, page = 0, size = 10) =>
    request<PagedResponse<Order>>(`/api/orders/user/${userId}?page=${page}&size=${size}`),
};

// ---- Payments ----
export const paymentsApi = {
  process: (body: {
    orderId: number;
    userId: string;
    amount: number;
    paymentMethod: string;
    idempotencyKey?: string;
  }) =>
    request<{ id: number; status: string; transactionReference: string }>('/api/payments', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
};

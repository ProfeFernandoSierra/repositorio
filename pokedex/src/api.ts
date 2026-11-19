import type {
  AuthResponse, Category, CategoryInput, Order, Product, ProductInput, RoleDto, User, UserInput,
} from './types';

// Se lee desde .env para poder cambiarla al desplegar (AWS) sin tocar código.
const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';
const TOKEN_KEY = 'pokestore_token';

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

/** Error con el mensaje que devuelve el backend ({ status, message, errors }). */
export class ApiError extends Error {
  status: number;
  fieldErrors?: Record<string, string>;

  constructor(status: number, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

// Si el token expira o es inválido, el AuthContext registra aquí qué hacer (cerrar sesión).
let onUnauthorized: (() => void) | null = null;
export const setUnauthorizedHandler = (fn: (() => void) | null) => {
  onUnauthorized = fn;
};

async function request<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const token = tokenStorage.get();
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_URL}${path}`, {
    method: options.method ?? 'GET',
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  if (res.status === 204) return undefined as T;

  const data = await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 401 && token) onUnauthorized?.();
    throw new ApiError(res.status, data?.message ?? `Error ${res.status}`, data?.errors);
  }
  return data as T;
}

function crud<T, I>(base: string){
  return {
    list: () => request<T[]>(base),
    create: (input: I) => request<T>(base, {method: 'POST', body: input}),
    update: (id: number, input: I) => request<T>(`${base}/${id}`, {method: 'PUT', body:input}),
    remove: (id: number) => request<void>(`${base}/${id}`, {method: 'DELETE'}),
  };
}


export const authApi = {
  login: (email: string, password: string) =>
    request<AuthResponse>('/auth/login', {method: 'POST', body: {email, password}}),
  register: (name: string, email: string, password: string) =>
    request<AuthResponse>('/auth/register', {method: 'POST', body: {name, email, password}}),
  me: () => request<User>('/auth/me'),
};

export const productsApi = crud<Product, ProductInput>('/products');
export const categoriesApi = crud<Category, CategoryInput>('/categories');
export const usersApi = crud<User, UserInput>('/users');
export const rolesApi = {list: () => request<RoleDto[]>('/roles')};
export const ordersApi = {
  create: (items: {productId: number, quantity: number}[]) =>
    request<Order>('/orders', {method: 'POST', body: {items}}),
  mine: () => request<Order[]>('/orders/me'),
  all: () => request<Order[]>('/orders'),
};

export function errorMessage(err: unknown): string {
  if(err instanceof ApiError){
    const fields = err.fieldErrors ? ' . ' + Object.entries(err.fieldErrors).map(([f, m]) => `${f}: ${m}`).join(' . ') : '';
    return err.message + fields;
  }
  if(err instanceof TypeError) return 'No se pudo conectar con el backend. Está corriendo spring??';
  return err instanceof Error ? err.message : 'Error inesperado';
}
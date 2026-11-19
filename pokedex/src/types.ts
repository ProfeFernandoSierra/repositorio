export type Role = 'ADMIN' | 'USER';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface UserInput {
  name: string;
  email: string;
  password?: string; // en edición: vacío = no cambiar
  role: Role;
}

export interface RoleDto {
  id: number;
  name: Role;
}

/** Categoría = tipo de Pokémon */
export interface Category {
  id: number;
  name: string;
  color: string;
}

export interface CategoryInput {
  name: string;
  color: string;
}

/** Producto = Pokémon a la venta */
export interface Product {
  id: number;
  pokedexNumber: number;
  name: string;
  price: number;
  stock: number;
  imageUrl: string;
  categories: Category[];
}

export interface ProductInput {
  pokedexNumber: number;
  name: string;
  price: number;
  stock: number;
  imageUrl: string;
  categoryIds: number[];
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface OrderItem {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userName: string;
  userEmail: string;
  total: number;
  createdAt: string;
  items: OrderItem[];
}

import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { authApi, setUnauthorizedHandler, tokenStorage } from '../api';
import type { AuthResponse, User } from '../types';
import { readStorage } from '../utils';

const USER_KEY = 'pokestore_user';

interface AuthValue {
  user: User | null;
  isAdmin: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (name: string, email: string, password: string) => Promise<void>;
  signOut: () => void;
}

const AuthContext = createContext<AuthValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() =>
    tokenStorage.get() ? readStorage<User | null>(USER_KEY, null) : null,
  );

  const signOut = useCallback(() => {
    tokenStorage.clear();
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  function saveSession({ token, user }: AuthResponse) {
    tokenStorage.set(token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    setUser(user);
  }

  // Al cargar la app se confirma el usuario con el backend: si alguien editó
  // localStorage para ponerse role "ADMIN", aquí se corrige (y el backend igual lo rechaza).
  useEffect(() => {
    setUnauthorizedHandler(signOut);
    if (tokenStorage.get()) {
      authApi
        .me()
        .then((u) => {
          setUser(u);
          localStorage.setItem(USER_KEY, JSON.stringify(u));
        })
        .catch(signOut);
    }
    return () => setUnauthorizedHandler(null);
  }, [signOut]);

  const signIn = async (email: string, password: string) => saveSession(await authApi.login(email, password));
  const signUp = async (name: string, email: string, password: string) =>
    saveSession(await authApi.register(name, email, password));

  return (
    <AuthContext.Provider value={{ user, isAdmin: user?.role === 'ADMIN', signIn, signUp, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  return ctx;
}

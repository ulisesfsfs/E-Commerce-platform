'use client';
import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { authApi } from '@/lib/api';

interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName?: string;
}

interface AuthContextValue {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (firstName: string, lastName: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const data = await authApi.login({ email, password });
    localStorage.setItem('token', data.token);
    const u: User = { userId: String(data.userId), email: data.email, firstName: data.firstName || email.split('@')[0] };
    localStorage.setItem('user', JSON.stringify(u));
    setToken(data.token);
    setUser(u);
  };

  const register = async (firstName: string, lastName: string, email: string, password: string) => {
    const data = await authApi.register({ firstName, lastName, email, password });
    localStorage.setItem('token', data.token);
    const u: User = { userId: String(data.userId), email: data.email, firstName: data.firstName || firstName, lastName };
    localStorage.setItem('user', JSON.stringify(u));
    setToken(data.token);
    setUser(u);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

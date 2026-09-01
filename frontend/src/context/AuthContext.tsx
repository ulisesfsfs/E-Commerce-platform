'use client';
import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { authApi } from '@/lib/api';

interface User {
  userId: string;
  email: string;
  firstName: string;
  lastName?: string;
  roles: string[];
}

function parseRolesFromToken(token: string): string[] {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (Array.isArray(payload.roles)) return payload.roles;
    if (typeof payload.roles === 'string') return payload.roles.split(',').map((r: string) => r.trim());
  } catch {}
  return ['ROLE_USER'];
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
      const parsedUser: User = JSON.parse(savedUser);
      // Refresh roles from token if missing
      parsedUser.roles = parseRolesFromToken(savedToken);
      setUser(parsedUser);
    }
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const data = await authApi.login({ email, password });
    localStorage.setItem('token', data.token);
    const roles = parseRolesFromToken(data.token);
    const u: User = {
      userId: String(data.userId),
      email: data.email,
      firstName: data.firstName || email.split('@')[0],
      roles,
    };
    localStorage.setItem('user', JSON.stringify(u));
    setToken(data.token);
    setUser(u);
  };

  const register = async (firstName: string, lastName: string, email: string, password: string) => {
    const data = await authApi.register({ firstName, lastName, email, password });
    localStorage.setItem('token', data.token);
    const roles = parseRolesFromToken(data.token);
    const u: User = {
      userId: String(data.userId),
      email: data.email,
      firstName: data.firstName || firstName,
      lastName,
      roles,
    };
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

import { createContext, useContext, useEffect, useState } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('reclaim_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      localStorage.setItem('reclaim_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('reclaim_user');
    }
  }, [user]);

  const persistSession = (authResponse) => {
    localStorage.setItem('reclaim_token', authResponse.token);
    const sessionUser = {
      userId: authResponse.userId,
      fullName: authResponse.fullName,
      email: authResponse.email,
      role: authResponse.role,
    };
    setUser(sessionUser);
    return sessionUser;
  };

  const login = async (credentials) => {
    setLoading(true);
    try {
      const res = await authApi.login(credentials);
      return persistSession(res);
    } finally {
      setLoading(false);
    }
  };

  const register = async (details) => {
    setLoading(true);
    try {
      const res = await authApi.register(details);
      return persistSession(res);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('reclaim_token');
    localStorage.removeItem('reclaim_user');
    setUser(null);
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, setUser, loading, login, register, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

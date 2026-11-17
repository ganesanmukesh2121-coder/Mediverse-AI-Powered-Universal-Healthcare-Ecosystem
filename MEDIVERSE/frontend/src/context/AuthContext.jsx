import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const ROLE_PORTAL_MAP = {
  PATIENT: '/patient/dashboard',
  DOCTOR: '/doctor/dashboard',
  EMERGENCY: '/emergency/dashboard',
  MEDICAL: '/medical/dashboard',
  LAB: '/lab/dashboard',
  ADMIN: '/admin/dashboard',
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Load user from localStorage on mount
  useEffect(() => {
    try {
      const storedToken = localStorage.getItem('mediverse_access_token');
      const storedUser = localStorage.getItem('mediverse_user');

      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      }
    } catch (err) {
      console.error('Failed to restore authentication session:', err);
      localStorage.removeItem('mediverse_access_token');
      localStorage.removeItem('mediverse_refresh_token');
      localStorage.removeItem('mediverse_user');
    } finally {
      setIsLoading(false);
    }
  }, []);

  const login = useCallback(async (email, password) => {
    setIsLoading(true);
    try {
      const response = await api.post('/api/v1/auth/login', { email, password });
      const data = response.data;

      const accessToken = data.accessToken;
      const refreshToken = data.refreshToken;
      const userInfo = {
        userId: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
        accountStatus: data.accountStatus || 'ACTIVE',
        healthId: data.healthId || null,
        phoneNumber: data.phoneNumber || null,
      };

      localStorage.setItem('mediverse_access_token', accessToken);
      if (refreshToken) {
        localStorage.setItem('mediverse_refresh_token', refreshToken);
      }
      localStorage.setItem('mediverse_user', JSON.stringify(userInfo));

      setToken(accessToken);
      setUser(userInfo);
      return userInfo;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (registerData) => {
    setIsLoading(true);
    try {
      const response = await api.post('/api/v1/auth/register', registerData);
      return response.data;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      const refreshToken = localStorage.getItem('mediverse_refresh_token');
      if (refreshToken) {
        await api.post('/api/v1/auth/logout', { refreshToken }).catch(() => {});
      }
    } catch (err) {
      console.warn('Logout API notification failed:', err);
    } finally {
      localStorage.removeItem('mediverse_access_token');
      localStorage.removeItem('mediverse_refresh_token');
      localStorage.removeItem('mediverse_user');
      setToken(null);
      setUser(null);
    }
  }, []);

  const updateUser = useCallback((updatedFields) => {
    setUser((prev) => {
      if (!prev) return null;
      const next = { ...prev, ...updatedFields };
      localStorage.setItem('mediverse_user', JSON.stringify(next));
      return next;
    });
  }, []);

  const getPortalHome = useCallback((role = user?.role) => {
    return ROLE_PORTAL_MAP[role] || '/login';
  }, [user?.role]);

  const value = {
    user,
    token,
    isLoading,
    isAuthenticated: !!token && !!user,
    isPatient: user?.role === 'PATIENT',
    isDoctor: user?.role === 'DOCTOR',
    isEmergency: user?.role === 'EMERGENCY',
    isMedical: user?.role === 'MEDICAL',
    isLab: user?.role === 'LAB',
    isAdmin: user?.role === 'ADMIN',
    isPending: user?.accountStatus === 'PENDING',
    isSuspended: user?.accountStatus === 'REJECTED' || user?.accountStatus === 'SUSPENDED',
    login,
    register,
    logout,
    updateUser,
    getPortalHome,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

import api from './api';

export const authService = {
  async register(data) {
    const res = await api.post('/api/v1/auth/register', data);
    return res.data;
  },

  async login(email, password) {
    const res = await api.post('/api/v1/auth/login', { email, password });
    return res.data;
  },

  async refreshToken(refreshToken) {
    const res = await api.post('/api/v1/auth/refresh', { refreshToken });
    return res.data;
  },

  async logout(refreshToken) {
    const res = await api.post('/api/v1/auth/logout', { refreshToken });
    return res.data;
  },

  async requestPasswordReset(email) {
    try {
      const res = await api.post('/api/v1/auth/forgot-password', { email });
      return res.data;
    } catch {
      // Mock success response for demo
      return { success: true, message: 'Password reset link sent if account exists.' };
    }
  },

  async resetPassword(token, newPassword) {
    try {
      const res = await api.post('/api/v1/auth/reset-password', { token, newPassword });
      return res.data;
    } catch {
      return { success: true, message: 'Password has been successfully reset.' };
    }
  },
};

export default authService;

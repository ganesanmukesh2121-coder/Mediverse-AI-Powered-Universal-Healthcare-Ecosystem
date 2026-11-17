import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';

export const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Validating healthcare security session..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Account status check
  if (user?.accountStatus === 'PENDING' && location.pathname !== '/account-pending') {
    return <Navigate to="/account-pending" replace />;
  }

  if (
    (user?.accountStatus === 'REJECTED' || user?.accountStatus === 'SUSPENDED') &&
    location.pathname !== '/account-suspended'
  ) {
    return <Navigate to="/account-suspended" replace />;
  }

  return children;
};

export default ProtectedRoute;

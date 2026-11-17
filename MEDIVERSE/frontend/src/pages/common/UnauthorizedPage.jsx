import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/common/Button';

export const UnauthorizedPage = () => {
  const { user, getPortalHome } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl border border-rose-200 shadow-xl p-8 text-center">
        <div className="w-16 h-16 rounded-2xl bg-rose-100 text-rose-600 mx-auto flex items-center justify-center mb-4">
          <ShieldAlert className="w-8 h-8" />
        </div>
        <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">403 — Unauthorized</h1>
        <h2 className="text-sm font-bold text-rose-700 mt-2 uppercase tracking-wider">
          Access Boundary Enforcement
        </h2>
        <p className="text-xs text-slate-600 mt-3 leading-relaxed">
          Your active role (<strong className="text-slate-900">{user?.role || 'Guest'}</strong>) is not authorized to access this restricted clinical or administrative section.
        </p>
        <div className="mt-6 flex flex-col gap-2">
          <Button
            variant="primary"
            icon={ArrowLeft}
            onClick={() => navigate(getPortalHome())}
            className="w-full"
          >
            Return to Authorized Dashboard
          </Button>
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;

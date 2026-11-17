import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Clock, ShieldAlert, LogOut, CheckCircle, Mail } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/common/Button';

export const AccountPendingPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-lg w-full bg-white rounded-2xl border border-slate-200 shadow-xl p-8 text-center">
        <div className="w-16 h-16 rounded-2xl bg-amber-100 border border-amber-200 text-amber-600 mx-auto flex items-center justify-center mb-5 shadow-xs">
          <Clock className="w-8 h-8 animate-pulse" />
        </div>

        <span className="text-[11px] font-bold uppercase tracking-wider text-amber-700 bg-amber-50 px-3 py-1 rounded-full border border-amber-200">
          Account Status: Pending Verification
        </span>

        <h2 className="mt-4 text-2xl font-bold text-slate-900 tracking-tight">
          Credential Verification in Progress
        </h2>

        <p className="mt-3 text-sm text-slate-600 leading-relaxed">
          Hello <span className="font-semibold text-slate-900">{user?.fullName || 'Healthcare Provider'}</span>, your registration application as a <span className="font-semibold text-slate-900">{user?.role || 'Provider'}</span> has been received and queued for administrative compliance review.
        </p>

        <div className="mt-6 text-left p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-2.5 text-xs text-slate-600">
          <div className="flex items-center gap-2 text-slate-800 font-semibold">
            <ShieldAlert className="w-4 h-4 text-mediverse-600" />
            <span>Verification Checklist:</span>
          </div>
          <div className="flex items-center gap-2">
            <CheckCircle className="w-3.5 h-3.5 text-emerald-500" />
            <span>Application submitted & account provisioned</span>
          </div>
          <div className="flex items-center gap-2">
            <Clock className="w-3.5 h-3.5 text-amber-500" />
            <span>State board medical / laboratory license lookup</span>
          </div>
          <div className="flex items-center gap-2">
            <Clock className="w-3.5 h-3.5 text-amber-500" />
            <span>Institutional affiliation & identity confirmation</span>
          </div>
        </div>

        <p className="mt-5 text-xs text-slate-500">
          Standard review takes between 1-2 business days. If you require expedited verification, please contact <a href="mailto:credentialing@mediverse.org" className="text-mediverse-600 font-medium underline">credentialing@mediverse.org</a>.
        </p>

        <div className="mt-8 pt-6 border-t border-slate-100 flex items-center justify-center gap-4">
          <Button variant="secondary" icon={LogOut} onClick={handleLogout}>
            Sign Out
          </Button>
          <Button variant="outline" onClick={() => window.location.reload()}>
            Check Status
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AccountPendingPage;

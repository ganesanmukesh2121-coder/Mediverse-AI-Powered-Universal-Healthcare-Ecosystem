import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldX, LogOut, Mail } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/common/Button';

export const AccountSuspendedPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isRejected = user?.accountStatus === 'REJECTED';

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-2xl border border-red-200 shadow-xl p-8 text-center">
        <div className="w-16 h-16 rounded-2xl bg-red-100 border border-red-200 text-red-600 mx-auto flex items-center justify-center mb-5 shadow-xs">
          <ShieldX className="w-8 h-8" />
        </div>

        <span className="text-[11px] font-bold uppercase tracking-wider text-red-700 bg-red-50 px-3 py-1 rounded-full border border-red-200">
          Account Status: {user?.accountStatus || 'INACTIVE'}
        </span>

        <h2 className="mt-4 text-2xl font-bold text-slate-900 tracking-tight">
          {isRejected ? 'Application Declined' : 'Account Access Suspended'}
        </h2>

        <p className="mt-3 text-sm text-slate-600 leading-relaxed">
          {isRejected
            ? 'Your provider application could not be verified by our administrative team. This may occur if provided licensing details could not be corroborated with official regulatory registers.'
            : 'Access to this Mediverse account has been temporarily restricted by system administrators due to security policies or compliance review.'}
        </p>

        <div className="mt-6 p-4 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-600">
          <p className="font-semibold text-slate-800">Support Inquiries:</p>
          <p className="mt-1">
            Please contact Mediverse Governance & Compliance at{' '}
            <a href="mailto:support@mediverse.org" className="text-mediverse-600 underline font-medium">
              support@mediverse.org
            </a>{' '}
            with reference to your account email.
          </p>
        </div>

        <div className="mt-8 pt-6 border-t border-slate-100">
          <Button variant="danger" icon={LogOut} onClick={handleLogout} className="w-full">
            Log Out of Session
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AccountSuspendedPage;

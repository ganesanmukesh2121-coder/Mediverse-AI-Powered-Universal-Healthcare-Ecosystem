import React from 'react';
import { Menu, Shield } from 'lucide-react';
import UserProfileMenu from './UserProfileMenu';
import NotificationPanel from './NotificationPanel';
import { useAuth } from '../../context/AuthContext';

export const TopNavbar = ({ onOpenSidebar }) => {
  const { user } = useAuth();

  const getPortalTitle = () => {
    switch (user?.role) {
      case 'PATIENT':
        return 'Patient Health Portal';
      case 'DOCTOR':
        return 'Physician Clinical Portal';
      case 'EMERGENCY':
        return 'Emergency Triage Console';
      case 'MEDICAL':
        return 'Hospital Management Center';
      case 'LAB':
        return 'Laboratory Information Portal';
      case 'ADMIN':
        return 'System Administration Portal';
      default:
        return 'Universal Healthcare Ecosystem';
    }
  };

  return (
    <header className="h-16 bg-white border-b border-slate-200/80 sticky top-0 z-30 flex items-center justify-between px-4 sm:px-6 shadow-2xs">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onOpenSidebar}
          className="p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 lg:hidden focus:outline-none"
          aria-label="Open sidebar"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div>
          <h1 className="text-base font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <span>{getPortalTitle()}</span>
          </h1>
          <p className="text-[11px] text-slate-400 hidden sm:block">
            Connected to Mediverse High-Trust Data Fabric
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2 sm:gap-4">
        {/* Protected HIPAA / PHI Compliance Pill */}
        <div className="hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-100 border border-slate-200 text-[11px] font-semibold text-slate-600">
          <Shield className="w-3.5 h-3.5 text-mediverse-600" />
          <span>Role-Gated & Audited</span>
        </div>

        {/* Notifications */}
        <NotificationPanel />

        {/* Profile */}
        <UserProfileMenu />
      </div>
    </header>
  );
};

export default TopNavbar;

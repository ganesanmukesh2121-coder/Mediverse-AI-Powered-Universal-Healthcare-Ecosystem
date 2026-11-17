import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  CreditCard,
  User,
  FileText,
  Calendar,
  Pill,
  FlaskConical,
  ShieldCheck,
  AlertCircle,
  Settings,
  Search,
  Users,
  Clock,
  Building2,
  Bed,
  LogOut,
  Stethoscope,
  Activity,
  FileCheck2,
  CheckCircle,
  KeyRound,
  ShieldAlert,
  ChevronRight,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const ROLE_NAVIGATION = {
  PATIENT: [
    { name: 'Dashboard', path: '/patient/dashboard', icon: LayoutDashboard },
    { name: 'My Health ID', path: '/patient/health-id', icon: CreditCard },
    { name: 'Medical Profile', path: '/patient/profile', icon: User },
    { name: 'Medical Records', path: '/patient/records', icon: FileText },
    { name: 'Appointments', path: '/patient/appointments', icon: Calendar },
    { name: 'Prescriptions', path: '/patient/prescriptions', icon: Pill },
    { name: 'Lab Reports', path: '/patient/lab-reports', icon: FlaskConical },
    { name: 'Consent Manager', path: '/patient/consents', icon: ShieldCheck },
    { name: 'Emergency ICE', path: '/patient/emergency', icon: AlertCircle },
    { name: 'Account Settings', path: '/patient/settings', icon: Settings },
  ],
  EMERGENCY: [
    { name: 'Emergency Station', path: '/emergency/dashboard', icon: LayoutDashboard },
    { name: 'Emergency Triage Lookup', path: '/emergency/lookup', icon: Search },
    { name: 'Emergency Access Audit', path: '/emergency/history', icon: Clock },
    { name: 'Responder Settings', path: '/emergency/settings', icon: Settings },
  ],
  DOCTOR: [
    { name: 'Doctor Console', path: '/doctor/dashboard', icon: LayoutDashboard },
    { name: 'Appointment Schedule', path: '/doctor/appointments', icon: Calendar },
    { name: 'Patient Lookup', path: '/doctor/lookup', icon: Search },
    { name: 'Authorized Patients', path: '/doctor/patients', icon: Users },
    { name: 'Clinical Encounters', path: '/doctor/consultations', icon: Stethoscope },
    { name: 'Prescription Desk', path: '/doctor/prescriptions', icon: Pill },
    { name: 'Timeline Explorer', path: '/doctor/timeline', icon: Activity },
    { name: 'Clinical Settings', path: '/doctor/settings', icon: Settings },
  ],
  MEDICAL: [
    { name: 'Hospital Center', path: '/medical/dashboard', icon: Building2 },
    { name: 'Patient Search', path: '/medical/lookup', icon: Search },
    { name: 'Inpatient Tracking', path: '/medical/workflow', icon: Activity },
    { name: 'Admissions Desk', path: '/medical/admissions', icon: Bed },
    { name: 'Discharge Summaries', path: '/medical/discharges', icon: FileCheck2 },
    { name: 'Facility Settings', path: '/medical/settings', icon: Settings },
  ],
  LAB: [
    { name: 'Diagnostics Dashboard', path: '/lab/dashboard', icon: LayoutDashboard },
    { name: 'Order Requisitions', path: '/lab/orders', icon: FileText },
    { name: 'Search Specimen/Patient', path: '/lab/lookup', icon: Search },
    { name: 'In-Analysis Worklist', path: '/lab/in-progress', icon: FlaskConical },
    { name: 'Completed Tests', path: '/lab/completed', icon: CheckCircle },
    { name: 'Report Generator', path: '/lab/reports', icon: FileCheck2 },
    { name: 'Lab Settings', path: '/lab/settings', icon: Settings },
  ],
  ADMIN: [
    { name: 'Platform Executive', path: '/admin/dashboard', icon: LayoutDashboard },
    { name: 'Provider Approvals', path: '/admin/verifications', icon: ShieldCheck },
    { name: 'User Management', path: '/admin/users', icon: Users },
    { name: 'Security & Access', path: '/admin/accounts', icon: KeyRound },
    { name: 'System Audit Trail', path: '/admin/audit-logs', icon: ShieldAlert },
    { name: 'System Settings', path: '/admin/settings', icon: Settings },
  ],
};

const ROLE_BADGE_STYLE = {
  PATIENT: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
  DOCTOR: 'bg-blue-500/10 text-blue-600 border-blue-500/20',
  EMERGENCY: 'bg-red-500/10 text-red-600 border-red-500/20',
  MEDICAL: 'bg-purple-500/10 text-purple-600 border-purple-500/20',
  LAB: 'bg-amber-500/10 text-amber-600 border-amber-500/20',
  ADMIN: 'bg-indigo-500/10 text-indigo-600 border-indigo-500/20',
};

export const Sidebar = ({ isOpen, onClose }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const role = user?.role || 'PATIENT';
  const navItems = ROLE_NAVIGATION[role] || ROLE_NAVIGATION.PATIENT;

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 w-64 bg-navy-950 text-slate-300 flex flex-col transition-transform duration-300 ease-in-out border-r border-navy-800 lg:static lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="h-16 flex items-center px-6 border-b border-navy-800 bg-navy-900/60">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-mediverse-600 to-emerald-400 flex items-center justify-center text-white font-black text-lg shadow-md shadow-mediverse-600/30">
              M
            </div>
            <div>
              <span className="font-bold text-white text-base tracking-tight flex items-center gap-1.5">
                MEDIVERSE
                <span className="text-[10px] uppercase font-bold px-1.5 py-0.5 rounded-sm bg-mediverse-500/20 text-mediverse-400">
                  AI
                </span>
              </span>
              <p className="text-[10px] text-slate-400 tracking-wider uppercase font-semibold">
                Universal Health
              </p>
            </div>
          </div>
        </div>

        {/* User Role Badge Card */}
        <div className="p-4 mx-3 my-3 rounded-xl bg-navy-900/80 border border-navy-800">
          <div className="flex items-center justify-between">
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              Active Portal
            </span>
            <span
              className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${
                ROLE_BADGE_STYLE[role] || ROLE_BADGE_STYLE.PATIENT
              }`}
            >
              {role}
            </span>
          </div>
          <div className="mt-2 flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-mediverse-600/20 border border-mediverse-500/30 flex items-center justify-center text-mediverse-400 font-bold text-xs uppercase">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="min-w-0 flex-1">
              <h5 className="text-xs font-semibold text-white truncate">{user?.fullName || 'User'}</h5>
              <p className="text-[11px] text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="flex-1 px-3 py-2 space-y-1 overflow-y-auto custom-scrollbar">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={() => onClose && onClose()}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all ${
                    isActive
                      ? 'bg-mediverse-600 text-white font-semibold shadow-sm shadow-mediverse-600/40'
                      : 'text-slate-300 hover:bg-navy-900 hover:text-white'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon className="w-4 h-4 shrink-0" />
                  <span>{item.name}</span>
                </div>
                <ChevronRight className="w-3.5 h-3.5 opacity-40 shrink-0" />
              </NavLink>
            );
          })}
        </nav>

        {/* Footer Logout Button */}
        <div className="p-4 border-t border-navy-800 bg-navy-900/40">
          <button
            type="button"
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-3.5 py-2 rounded-lg text-xs font-medium text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 transition-colors"
          >
            <LogOut className="w-4 h-4 shrink-0" />
            <span>Sign Out Session</span>
          </button>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;

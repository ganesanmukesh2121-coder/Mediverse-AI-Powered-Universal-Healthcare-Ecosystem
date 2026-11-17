import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Settings, LogOut, ChevronDown, Shield } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const UserProfileMenu = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const getSettingsPath = () => {
    switch (user?.role) {
      case 'PATIENT':
        return '/patient/settings';
      case 'DOCTOR':
        return '/doctor/settings';
      case 'EMERGENCY':
        return '/emergency/settings';
      case 'MEDICAL':
        return '/medical/settings';
      case 'LAB':
        return '/lab/settings';
      case 'ADMIN':
        return '/admin/settings';
      default:
        return '/login';
    }
  };

  return (
    <div className="relative" ref={menuRef}>
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2.5 p-1.5 rounded-lg hover:bg-slate-100 transition-colors focus:outline-none"
      >
        <div className="w-8 h-8 rounded-full bg-mediverse-600 text-white font-bold text-xs flex items-center justify-center shadow-xs">
          {user?.fullName?.charAt(0) || 'U'}
        </div>
        <div className="hidden sm:block text-left">
          <p className="text-xs font-semibold text-slate-800 leading-tight truncate max-w-[120px]">
            {user?.fullName || 'User'}
          </p>
          <span className="text-[10px] font-bold text-mediverse-600 tracking-wider">
            {user?.role}
          </span>
        </div>
        <ChevronDown className="w-4 h-4 text-slate-400 hidden sm:block" />
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-56 rounded-xl bg-white shadow-xl border border-slate-200 py-1.5 z-50 animate-in fade-in zoom-in-95 duration-100">
          <div className="px-4 py-2.5 border-b border-slate-100">
            <p className="text-xs font-semibold text-slate-900 truncate">{user?.fullName}</p>
            <p className="text-[11px] text-slate-500 truncate">{user?.email}</p>
            {user?.healthId && (
              <p className="text-[10px] font-mono font-bold text-mediverse-600 mt-1">
                ID: {user?.healthId}
              </p>
            )}
          </div>

          <div className="py-1">
            <button
              onClick={() => {
                setIsOpen(false);
                navigate(getSettingsPath());
              }}
              className="w-full flex items-center gap-2.5 px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition-colors"
            >
              <Settings className="w-4 h-4 text-slate-400" />
              <span>Portal Settings</span>
            </button>
          </div>

          <div className="border-t border-slate-100 pt-1">
            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-2.5 px-4 py-2 text-xs text-rose-600 hover:bg-rose-50 transition-colors"
            >
              <LogOut className="w-4 h-4 text-rose-500" />
              <span>Sign Out</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfileMenu;

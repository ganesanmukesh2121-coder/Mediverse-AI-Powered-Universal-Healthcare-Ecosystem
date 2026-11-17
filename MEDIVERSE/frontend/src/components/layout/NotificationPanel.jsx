import React, { useState, useRef, useEffect } from 'react';
import { Bell, CheckCheck, Info, ShieldAlert } from 'lucide-react';

export const NotificationPanel = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(2);
  const panelRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (panelRef.current && !panelRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const notifications = [
    {
      id: 1,
      title: 'Consent Authorization Active',
      message: 'Dr. Marcus Sterling was granted 30-day FULL_TIMELINE access.',
      time: '15m ago',
      type: 'info',
    },
    {
      id: 2,
      title: 'Lab Report Ready',
      message: 'Comprehensive Metabolic Panel results have been uploaded.',
      time: '2h ago',
      type: 'success',
    },
    {
      id: 3,
      title: 'Security Verification Stamp',
      message: 'Dual-token JWT session active with automated rotation.',
      time: '1d ago',
      type: 'security',
    },
  ];

  return (
    <div className="relative" ref={panelRef}>
      <button
        type="button"
        onClick={() => {
          setIsOpen(!isOpen);
          if (!isOpen) setUnreadCount(0);
        }}
        className="relative p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition-colors"
        aria-label="Open notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-mediverse-600 ring-2 ring-white" />
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 rounded-xl bg-white shadow-xl border border-slate-200 py-2 z-50 animate-in fade-in zoom-in-95 duration-100">
          <div className="flex items-center justify-between px-4 py-2 border-b border-slate-100">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-700">
              System Notifications
            </span>
            <button
              onClick={() => setUnreadCount(0)}
              className="text-[11px] text-mediverse-600 hover:underline flex items-center gap-1"
            >
              <CheckCheck className="w-3.5 h-3.5" />
              <span>Mark all read</span>
            </button>
          </div>

          <div className="divide-y divide-slate-100 max-h-72 overflow-y-auto">
            {notifications.map((item) => (
              <div key={item.id} className="p-3.5 hover:bg-slate-50 transition-colors">
                <div className="flex items-start gap-2.5">
                  <div className="mt-0.5 text-mediverse-600 shrink-0">
                    {item.type === 'security' ? (
                      <ShieldAlert className="w-4 h-4 text-indigo-500" />
                    ) : (
                      <Info className="w-4 h-4 text-mediverse-500" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-xs font-semibold text-slate-800">{item.title}</p>
                    <p className="text-[11px] text-slate-500 mt-0.5 leading-relaxed">{item.message}</p>
                    <span className="text-[10px] text-slate-400 mt-1 block">{item.time}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationPanel;

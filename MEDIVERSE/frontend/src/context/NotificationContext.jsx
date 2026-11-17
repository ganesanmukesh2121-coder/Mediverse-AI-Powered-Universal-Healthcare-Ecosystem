import React, { createContext, useContext, useState, useCallback } from 'react';
import { CheckCircle2, AlertTriangle, AlertCircle, Info, X } from 'lucide-react';

const NotificationContext = createContext(null);

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState([]);

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  }, []);

  const addNotification = useCallback(
    ({ type = 'info', title, message, duration = 4000 }) => {
      const id = Date.now() + Math.random().toString(36).substring(2, 9);
      const notification = { id, type, title, message };

      setNotifications((prev) => [...prev, notification]);

      if (duration > 0) {
        setTimeout(() => {
          removeNotification(id);
        }, duration);
      }
      return id;
    },
    [removeNotification]
  );

  const notifySuccess = useCallback(
    (message, title = 'Success') => addNotification({ type: 'success', title, message }),
    [addNotification]
  );

  const notifyError = useCallback(
    (message, title = 'Error') => addNotification({ type: 'error', title, message, duration: 6000 }),
    [addNotification]
  );

  const notifyWarning = useCallback(
    (message, title = 'Warning') => addNotification({ type: 'warning', title, message }),
    [addNotification]
  );

  const notifyInfo = useCallback(
    (message, title = 'Information') => addNotification({ type: 'info', title, message }),
    [addNotification]
  );

  const getIcon = (type) => {
    switch (type) {
      case 'success':
        return <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />;
      case 'error':
        return <AlertCircle className="w-5 h-5 text-red-500 shrink-0" />;
      case 'warning':
        return <AlertTriangle className="w-5 h-5 text-amber-500 shrink-0" />;
      default:
        return <Info className="w-5 h-5 text-sky-500 shrink-0" />;
    }
  };

  const getBorderColor = (type) => {
    switch (type) {
      case 'success':
        return 'border-l-4 border-l-emerald-500 bg-emerald-50/90 text-emerald-900';
      case 'error':
        return 'border-l-4 border-l-red-500 bg-red-50/90 text-red-900';
      case 'warning':
        return 'border-l-4 border-l-amber-500 bg-amber-50/90 text-amber-900';
      default:
        return 'border-l-4 border-l-sky-500 bg-sky-50/90 text-sky-900';
    }
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        addNotification,
        removeNotification,
        notifySuccess,
        notifyError,
        notifyWarning,
        notifyInfo,
      }}
    >
      {children}
      {/* Toast Notification Container */}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-md w-full px-4 sm:px-0 pointer-events-none">
        {notifications.map((n) => (
          <div
            key={n.id}
            className={`pointer-events-auto flex items-start gap-3 p-3.5 rounded-lg shadow-lg border border-slate-200 backdrop-blur-sm transition-all transform animate-in slide-in-from-bottom-2 ${getBorderColor(
              n.type
            )}`}
          >
            {getIcon(n.type)}
            <div className="flex-1 min-w-0">
              {n.title && <h4 className="text-xs font-semibold uppercase tracking-wider mb-0.5">{n.title}</h4>}
              <p className="text-sm font-medium leading-relaxed break-words">{n.message}</p>
            </div>
            <button
              onClick={() => removeNotification(n.id)}
              className="text-slate-400 hover:text-slate-600 transition-colors p-1"
              aria-label="Close notification"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ))}
      </div>
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};

import React from 'react';

const STATUS_CONFIGS = {
  // Account & General Statuses
  ACTIVE: { bg: 'bg-emerald-100', text: 'text-emerald-800', border: 'border-emerald-200', label: 'Active' },
  PENDING: { bg: 'bg-amber-100', text: 'text-amber-800', border: 'border-amber-200', label: 'Pending' },
  REJECTED: { bg: 'bg-red-100', text: 'text-red-800', border: 'border-red-200', label: 'Rejected' },
  SUSPENDED: { bg: 'bg-rose-100', text: 'text-rose-800', border: 'border-rose-200', label: 'Suspended' },

  // Clinical & Appointment Statuses
  SCHEDULED: { bg: 'bg-sky-100', text: 'text-sky-800', border: 'border-sky-200', label: 'Scheduled' },
  CONFIRMED: { bg: 'bg-teal-100', text: 'text-teal-800', border: 'border-teal-200', label: 'Confirmed' },
  COMPLETED: { bg: 'bg-slate-100', text: 'text-slate-800', border: 'border-slate-300', label: 'Completed' },
  CANCELLED: { bg: 'bg-zinc-100', text: 'text-zinc-700', border: 'border-zinc-200', label: 'Cancelled' },
  NO_SHOW: { bg: 'bg-orange-100', text: 'text-orange-800', border: 'border-orange-200', label: 'No Show' },

  // Consent Statuses
  REVOKED: { bg: 'bg-zinc-100', text: 'text-zinc-600', border: 'border-zinc-300', label: 'Revoked' },
  FULL_TIMELINE: { bg: 'bg-indigo-100', text: 'text-indigo-800', border: 'border-indigo-200', label: 'Full Timeline' },
  LAB_ONLY: { bg: 'bg-cyan-100', text: 'text-cyan-800', border: 'border-cyan-200', label: 'Lab Only' },
  CONSULT_ONLY: { bg: 'bg-violet-100', text: 'text-violet-800', border: 'border-violet-200', label: 'Consult Only' },

  // Lab & Diagnostic Statuses
  PENDING_COLLECTION: { bg: 'bg-amber-100', text: 'text-amber-800', border: 'border-amber-200', label: 'Pending Collection' },
  IN_PROGRESS: { bg: 'bg-blue-100', text: 'text-blue-800', border: 'border-blue-200', label: 'In Progress' },
  DISPENSED: { bg: 'bg-emerald-100', text: 'text-emerald-800', border: 'border-emerald-200', label: 'Dispensed' },
  NORMAL: { bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200', label: 'Normal' },
  HIGH: { bg: 'bg-rose-50', text: 'text-rose-700', border: 'border-rose-200', label: 'High' },
  LOW: { bg: 'bg-amber-50', text: 'text-amber-700', border: 'border-amber-200', label: 'Low' },

  // Inpatient Statuses
  ADMITTED: { bg: 'bg-indigo-100', text: 'text-indigo-800', border: 'border-indigo-200', label: 'Admitted' },
  DISCHARGED: { bg: 'bg-emerald-100', text: 'text-emerald-800', border: 'border-emerald-200', label: 'Discharged' },

  // Emergency Priority
  STAT_URGENT: { bg: 'bg-red-600', text: 'text-white font-bold', border: 'border-red-700', label: 'STAT Urgent' },
  ROUTINE: { bg: 'bg-slate-100', text: 'text-slate-700', border: 'border-slate-200', label: 'Routine' },
};

export const StatusBadge = ({ status = 'ACTIVE', size = 'sm', className = '' }) => {
  const normalizedKey = (status || '').toUpperCase().replace(/[\s-]/g, '_');
  const config = STATUS_CONFIGS[normalizedKey] || {
    bg: 'bg-slate-100',
    text: 'text-slate-800',
    border: 'border-slate-200',
    label: status,
  };

  const sizeClass = size === 'sm' ? 'px-2.5 py-0.5 text-xs' : 'px-3 py-1 text-sm';

  return (
    <span
      className={`inline-flex items-center font-medium rounded-full border ${config.bg} ${config.text} ${config.border} ${sizeClass} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current opacity-70 mr-1.5 shrink-0" />
      {config.label}
    </span>
  );
};

export default StatusBadge;

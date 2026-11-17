import React, { useState, useEffect } from 'react';
import { ShieldAlert, Search, Download, Printer, Filter, Clock } from 'lucide-react';
import adminService from '../../services/adminService';
import SearchBar from '../../components/dashboard/SearchBar';
import DataTable from '../../components/dashboard/DataTable';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const AuditLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    adminService.getAuditLogs().then((data) => {
      setLogs(data);
      setIsLoading(false);
    });
  }, []);

  const filtered = logs.filter((l) => {
    const term = searchTerm.toLowerCase();
    return (
      l.actorEmail.toLowerCase().includes(term) ||
      l.action.toLowerCase().includes(term) ||
      l.resource.toLowerCase().includes(term) ||
      l.details.toLowerCase().includes(term)
    );
  });

  const getActionBadgeColor = (action) => {
    if (action.includes('REJECT') || action.includes('LOCKOUT')) {
      return 'bg-red-50 text-red-700 border-red-200';
    }
    if (action.includes('EMERGENCY')) {
      return 'bg-amber-50 text-amber-700 border-amber-200';
    }
    return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  };

  const columns = [
    {
      title: 'Timestamp (UTC)',
      key: 'timestamp',
      render: (row) => (
        <span className="font-mono text-xs text-slate-600">
          {new Date(row.timestamp).toLocaleString()}
        </span>
      ),
    },
    {
      title: 'Action Event',
      key: 'action',
      render: (row) => (
        <span className={`text-[11px] font-bold px-2 py-0.5 rounded border ${getActionBadgeColor(row.action)}`}>
          {row.action}
        </span>
      ),
    },
    {
      title: 'Actor Principal',
      key: 'actorEmail',
      render: (row) => <span className="font-medium text-slate-800 text-xs">{row.actorEmail}</span>,
    },
    {
      title: 'Target Resource',
      key: 'resource',
      render: (row) => <span className="font-mono text-xs text-slate-700">{row.resource}</span>,
    },
    {
      title: 'Audit Narrative',
      key: 'details',
      render: (row) => <span className="text-xs text-slate-600 block max-w-xs">{row.details}</span>,
    },
    {
      title: 'Client IP',
      key: 'ipAddress',
      render: (row) => <span className="font-mono text-xs text-slate-400">{row.ipAddress}</span>,
    },
  ];

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading immutable security audit trail..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Security Audit Logs</h2>
          <p className="text-xs text-slate-500 mt-1">
            Forensic, tamper-evident audit trail capturing provider verifications, emergency lookups, and authentication events
          </p>
        </div>

        <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
          Export Audit Trail
        </Button>
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Filter by actor, action type, resource..."
        />
      </div>

      <DataTable
        columns={columns}
        data={filtered}
        emptyTitle="No audit records"
        emptyDescription="No events match your criteria."
      />
    </div>
  );
};

export default AuditLogsPage;

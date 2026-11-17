import React, { useState, useEffect } from 'react';
import { ShieldAlert, Clock, Search, MapPin, CheckCircle2, User } from 'lucide-react';
import emergencyService from '../../services/emergencyService';
import SearchBar from '../../components/dashboard/SearchBar';
import DataTable from '../../components/dashboard/DataTable';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const EmergencyHistoryPage = () => {
  const [logs, setLogs] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    emergencyService.getEmergencyHistory().then((data) => {
      setLogs(data);
      setIsLoading(false);
    });
  }, []);

  const filteredLogs = logs.filter((l) => {
    const term = searchTerm.toLowerCase();
    return (
      l.patientHealthId.toLowerCase().includes(term) ||
      (l.patientName && l.patientName.toLowerCase().includes(term)) ||
      l.justificationReason.toLowerCase().includes(term)
    );
  });

  const columns = [
    {
      title: 'Timestamp (UTC)',
      key: 'timestamp',
      render: (row) => (
        <span className="font-mono text-xs text-slate-700">
          {new Date(row.timestamp).toLocaleString()}
        </span>
      ),
    },
    {
      title: 'Patient Health ID',
      key: 'patientHealthId',
      render: (row) => (
        <span className="font-mono font-bold text-red-600 bg-red-50 px-2 py-0.5 rounded border border-red-200">
          {row.patientHealthId}
        </span>
      ),
    },
    {
      title: 'Patient Name',
      key: 'patientName',
      render: (row) => <span className="font-medium text-slate-900">{row.patientName || 'Triage Patient'}</span>,
    },
    {
      title: 'Emergency Clinical Justification',
      key: 'justificationReason',
      render: (row) => (
        <span className="text-xs text-slate-700 block max-w-sm font-medium">
          {row.justificationReason}
        </span>
      ),
    },
    {
      title: 'Responding Unit / Location',
      key: 'location',
      render: (row) => <span className="text-xs text-slate-500">{row.location || 'Metro Mobile Unit'}</span>,
    },
  ];

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading emergency audit trail..." />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Emergency Access Audit Trail</h2>
        <p className="text-xs text-slate-500 mt-1">
          Forensic immutable logs of emergency bypass accesses performed under Section 3.4.16.3 of the Healthcare Governance Protocol
        </p>
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Search by Health ID, patient name, or justification..."
        />
      </div>

      <DataTable
        columns={columns}
        data={filteredLogs}
        emptyTitle="No emergency access records"
        emptyDescription="No emergency accesses have been performed by this terminal."
      />
    </div>
  );
};

export default EmergencyHistoryPage;

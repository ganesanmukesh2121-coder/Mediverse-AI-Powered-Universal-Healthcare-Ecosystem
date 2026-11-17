import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  AlertTriangle,
  Search,
  Clock,
  ShieldAlert,
  HeartPulse,
  ArrowRight,
  Activity,
  FileCheck2,
} from 'lucide-react';
import emergencyService from '../../services/emergencyService';
import DashboardCard from '../../components/dashboard/DashboardCard';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const EmergencyDashboard = () => {
  const navigate = useNavigate();
  const [quickHealthId, setQuickHealthId] = useState('');
  const [recentLogs, setRecentLogs] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    emergencyService.getEmergencyHistory().then((logs) => {
      setRecentLogs(logs);
      setIsLoading(false);
    });
  }, []);

  const handleQuickLookup = (e) => {
    e.preventDefault();
    if (!quickHealthId.trim()) return;
    navigate(`/emergency/lookup?id=${encodeURIComponent(quickHealthId.trim())}`);
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Initializing emergency responder console..." />;
  }

  return (
    <div className="space-y-6">
      {/* Top Emergency Red Banner */}
      <div className="bg-gradient-to-r from-red-900 via-rose-950 to-navy-950 p-6 rounded-2xl text-white shadow-xl border border-red-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-red-300 flex items-center gap-1.5">
            <HeartPulse className="w-4 h-4 text-red-400" />
            <span>Emergency Medical Services (EMS) Console</span>
          </span>
          <h2 className="text-2xl font-black tracking-tight mt-1">First Responder Triage Station</h2>
          <p className="text-xs text-red-200/80 mt-1">
            Rapid, authenticated access to vital allergy, medication, and ICE data during acute trauma
          </p>
        </div>

        <Link to="/emergency/lookup">
          <Button variant="danger" icon={Search} size="lg" className="shadow-lg shadow-red-900/40">
            Open Emergency Lookup
          </Button>
        </Link>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard
          title="Triage Lookups"
          value={recentLogs.length}
          subtitle="All queries audited"
          icon={Activity}
          colorScheme="red"
        />
        <DashboardCard
          title="Average Latency"
          value="140 ms"
          subtitle="Sub-second retrieval"
          icon={Clock}
          colorScheme="emerald"
        />
        <DashboardCard
          title="Audit Compliance"
          value="100%"
          subtitle="Mandatory justification"
          icon={ShieldAlert}
          colorScheme="blue"
        />
        <DashboardCard
          title="Station Status"
          value="DISPATCHED"
          subtitle="Unit EMS-7 Active"
          icon={HeartPulse}
          colorScheme="purple"
        />
      </div>

      {/* Quick Triage Search Box */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center gap-2 mb-3">
          <Search className="w-5 h-5 text-red-600" />
          <h3 className="text-base font-bold text-slate-900">Rapid Patient Health ID Lookup</h3>
        </div>
        <p className="text-xs text-slate-500 mb-4">
          Enter the patient's Universal Health ID or scan their digital card QR to retrieve life-saving triage parameters.
        </p>

        <form onSubmit={handleQuickLookup} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={quickHealthId}
            onChange={(e) => setQuickHealthId(e.target.value)}
            placeholder="e.g. MED-10293847-P or MED-99887766-P"
            className="flex-1 px-4 py-2.5 rounded-xl border border-slate-300 text-sm font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-500"
          />
          <Button type="submit" variant="danger" icon={Search}>
            Initiate Triage Access
          </Button>
        </form>

        <div className="mt-3 flex items-center gap-2 text-xs text-slate-500">
          <span>Quick demo IDs:</span>
          <button
            type="button"
            onClick={() => setQuickHealthId('MED-10293847-P')}
            className="font-mono text-red-600 font-bold hover:underline"
          >
            MED-10293847-P
          </button>
          <span>•</span>
          <button
            type="button"
            onClick={() => setQuickHealthId('MED-99887766-P')}
            className="font-mono text-red-600 font-bold hover:underline"
          >
            MED-99887766-P
          </button>
        </div>
      </div>

      {/* Recent Access History Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div>
            <h3 className="text-base font-bold text-slate-900">Recent Emergency Audit Trail</h3>
            <p className="text-xs text-slate-500">Legal audit trail of emergency data lookups conducted by this terminal</p>
          </div>
          <Link to="/emergency/history">
            <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
              Full Audit Log
            </Button>
          </Link>
        </div>

        <div className="divide-y divide-slate-100 mt-2">
          {recentLogs.slice(0, 3).map((log) => (
            <div key={log.id} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-mono font-bold text-red-700 bg-red-50 px-2 py-0.5 rounded border border-red-200">
                    {log.patientHealthId}
                  </span>
                  <span className="font-semibold text-slate-900">{log.patientName}</span>
                </div>
                <p className="text-slate-600 mt-1 leading-relaxed">
                  <strong>Justification:</strong> {log.justificationReason}
                </p>
              </div>

              <div className="text-right text-slate-400 shrink-0">
                <span className="block font-medium text-slate-600">{new Date(log.timestamp).toLocaleTimeString()}</span>
                <span className="block text-[11px]">{new Date(log.timestamp).toLocaleDateString()}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default EmergencyDashboard;

import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FlaskConical, FileText, CheckCircle2, Clock, Plus, Search, Activity, ArrowRight } from 'lucide-react';
import labService from '../../services/labService';
import DashboardCard from '../../components/dashboard/DashboardCard';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const LabDashboard = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [searchHealthId, setSearchHealthId] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    labService.getTestOrders().then((data) => {
      setOrders(data);
      setIsLoading(false);
    });
  }, []);

  const handleLookup = (e) => {
    e.preventDefault();
    if (!searchHealthId.trim()) return;
    navigate(`/lab/lookup?id=${encodeURIComponent(searchHealthId.trim())}`);
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Initializing laboratory information workstation..." />;
  }

  const pending = orders.filter((o) => o.status === 'PENDING_COLLECTION');
  const inProgress = orders.filter((o) => o.status === 'IN_PROGRESS');
  const completed = orders.filter((o) => o.status === 'COMPLETED');

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-cyan-900 via-sky-950 to-navy-950 p-6 rounded-2xl text-white shadow-lg border border-cyan-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs uppercase font-bold tracking-wider text-cyan-300">
              Laboratory Information System (LIS) • Station 1
            </span>
            <DemoDataBadge label="LIS Processing Preview" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight mt-1">BioTech Clinical Diagnostics Lab</h2>
          <p className="text-xs text-cyan-200/80 mt-1">
            CLIA-88D1092834 • CAP Accredited • Clinical Chemistry & Molecular Pathology
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link to="/lab/reports">
            <Button variant="primary" icon={Plus} size="sm" className="bg-cyan-600 hover:bg-cyan-500">
              Enter Lab Report
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard
          title="Pending Collection"
          value={pending.length}
          subtitle="Awaiting specimen"
          icon={Clock}
          colorScheme="amber"
        />
        <DashboardCard
          title="In Analysis"
          value={inProgress.length}
          subtitle="Analyzers active"
          icon={FlaskConical}
          colorScheme="blue"
        />
        <DashboardCard
          title="Completed Today"
          value={completed.length}
          subtitle="Results published"
          icon={CheckCircle2}
          colorScheme="emerald"
        />
        <DashboardCard
          title="Quality Control (QC)"
          value="PASSED"
          subtitle="Analyzers calibrated"
          icon={Activity}
          colorScheme="purple"
        />
      </div>

      {/* Patient Specimen Search */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center gap-2 mb-2">
          <Search className="w-4 h-4 text-cyan-600" />
          <h3 className="text-sm font-bold text-slate-900">Specimen Accessioning & Order Search</h3>
        </div>
        <p className="text-xs text-slate-500 mb-3">
          Query test requisitions by Universal Health ID or Specimen Requisition Order ID
        </p>

        <form onSubmit={handleLookup} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={searchHealthId}
            onChange={(e) => setSearchHealthId(e.target.value)}
            placeholder="e.g. MED-10293847-P or ORD-99120"
            className="flex-1 px-3.5 py-2 rounded-xl border border-slate-300 text-xs font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-cyan-500/20 focus:border-cyan-500"
          />
          <Button type="submit" variant="primary" icon={Search} size="sm" className="bg-cyan-600 hover:bg-cyan-500">
            Accession Specimen
          </Button>
        </form>
      </div>

      {/* Test Orders Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div>
            <h3 className="text-base font-bold text-slate-900">Active Test Requisitions</h3>
            <p className="text-xs text-slate-500">Incoming laboratory tests requested by physicians</p>
          </div>
          <Link to="/lab/orders">
            <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
              All Orders
            </Button>
          </Link>
        </div>

        <div className="divide-y divide-slate-100 mt-2">
          {orders.map((ord) => (
            <div key={ord.id} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-xl bg-cyan-50 text-cyan-600 flex items-center justify-center shrink-0 border border-cyan-100">
                  <FlaskConical className="w-5 h-5" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h5 className="font-bold text-slate-900 text-sm">{ord.testType}</h5>
                    <span className="font-mono text-[11px] text-cyan-700 bg-cyan-50 px-2 py-0.5 rounded font-bold">
                      {ord.orderNumber}
                    </span>
                    <StatusBadge status={ord.status} size="sm" />
                    <StatusBadge status={ord.priority} size="sm" />
                  </div>
                  <p className="text-slate-500 mt-0.5">
                    Patient: <strong className="text-slate-800">{ord.patientName}</strong> ({ord.healthId}) • Ordered by: {ord.orderedBy}
                  </p>
                  <span className="text-slate-400 mt-1 block">
                    Sample: {ord.sampleType} • Date: {new Date(ord.orderedDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Link to={`/lab/reports?orderId=${ord.orderNumber}&healthId=${ord.healthId}&testName=${encodeURIComponent(ord.testType)}`}>
                  <Button variant="primary" size="sm" className="bg-cyan-600 hover:bg-cyan-500 text-xs">
                    Input Results
                  </Button>
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LabDashboard;

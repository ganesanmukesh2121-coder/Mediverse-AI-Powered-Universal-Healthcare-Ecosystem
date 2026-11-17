import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Shield, Users, ShieldCheck, ShieldAlert, KeyRound, ArrowRight, HeartPulse, Stethoscope, Building2, FlaskConical, Server, Database } from 'lucide-react';
import adminService from '../../services/adminService';
import DashboardCard from '../../components/dashboard/DashboardCard';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const AdminDashboard = () => {
  const [metrics, setMetrics] = useState(null);
  const [pendingProviders, setPendingProviders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    Promise.all([adminService.getPlatformMetrics(), adminService.getPendingProviders()])
      .then(([m, p]) => {
        setMetrics(m);
        setPendingProviders(p);
      })
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading system administration console..." />;
  }

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-indigo-950 via-navy-950 to-slate-900 p-6 rounded-2xl text-white shadow-lg border border-indigo-900/60 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase font-bold tracking-wider text-indigo-300 flex items-center gap-1.5">
            <Shield className="w-4 h-4 text-indigo-400" />
            <span>Platform Governance & Security Administration</span>
          </span>
          <h2 className="text-2xl font-bold tracking-tight mt-1">Mediverse Central Executive Portal</h2>
          <p className="text-xs text-slate-300 mt-1">
            System uptime: <span className="font-semibold text-emerald-400">{metrics?.systemUptime || '99.98%'}</span> • Database Cluster: <span className="font-mono text-indigo-300">Synchronized</span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link to="/admin/verifications">
            <Button variant="primary" icon={ShieldCheck} size="sm" className="bg-indigo-600 hover:bg-indigo-500">
              Review Provider Queue ({pendingProviders.length})
            </Button>
          </Link>
          <Link to="/admin/audit-logs">
            <Button variant="secondary" icon={ShieldAlert} size="sm" className="bg-white/10 text-white hover:bg-white/20 border-white/20">
              Audit Logs
            </Button>
          </Link>
        </div>
      </div>

      {/* Strict Privacy Governance Notice */}
      <div className="p-4 bg-amber-50/70 border border-amber-300 rounded-2xl flex items-start gap-3 text-xs text-amber-950">
        <ShieldAlert className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
        <div>
          <h4 className="font-bold text-amber-900 uppercase tracking-wider">
            Strict Zero-Clinical-Access Privacy Boundary Enforced
          </h4>
          <p className="mt-0.5 leading-relaxed text-amber-900/90">
            By system architectural mandate, System Administrators possess <strong>ZERO access to private clinical data</strong> (no consultation notes, diagnoses, or patient medical histories). This portal governs user lifecycle, provider credential verification, security policies, and forensic audit logs only.
          </p>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard
          title="Total Registered Users"
          value={metrics?.totalUsers || '1,420'}
          subtitle="Across all 6 ecosystem roles"
          icon={Users}
          colorScheme="blue"
        />
        <DashboardCard
          title="Pending Approvals"
          value={pendingProviders.length}
          subtitle="Licensing verification queue"
          icon={ShieldCheck}
          colorScheme="amber"
        />
        <DashboardCard
          title="Verified Physicians"
          value={metrics?.verifiedDoctors || '94'}
          subtitle="State licensed & active"
          icon={Stethoscope}
          colorScheme="emerald"
        />
        <DashboardCard
          title="Connected Hospitals"
          value={metrics?.registeredHospitals || '26'}
          subtitle="Inpatient acute centers"
          icon={Building2}
          colorScheme="purple"
        />
      </div>

      {/* Queue & Ecosystem Snapshot */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Pending Providers Queue */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <h3 className="text-base font-bold text-slate-900">Pending Provider Verification Queue</h3>
              <p className="text-xs text-slate-500">Doctors, hospitals, and diagnostic labs requiring credential review</p>
            </div>
            <Link to="/admin/verifications">
              <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
                Review All
              </Button>
            </Link>
          </div>

          <div className="divide-y divide-slate-100 mt-2">
            {pendingProviders.map((provider) => (
              <div key={provider.userId} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
                <div>
                  <div className="flex items-center gap-2">
                    <h5 className="font-bold text-slate-900 text-sm">{provider.fullName}</h5>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-50 text-amber-700 border border-amber-200">
                      {provider.role}
                    </span>
                    <span className="font-mono text-slate-500">
                      Lic: {provider.licenseNumber || provider.facilityLicenseNumber || provider.labLicenseNumber}
                    </span>
                  </div>
                  <p className="text-slate-500 mt-0.5">
                    {provider.email} • {provider.specialization || provider.organizationType || provider.accreditationBody}
                  </p>
                  <span className="text-slate-400 mt-1 block">
                    Submitted on: {new Date(provider.submittedAt).toLocaleDateString()}
                  </span>
                </div>

                <Link to="/admin/verifications">
                  <Button variant="outline" size="sm" className="text-xs">
                    Inspect Application
                  </Button>
                </Link>
              </div>
            ))}

            {pendingProviders.length === 0 && (
              <div className="py-12 text-center text-xs text-slate-400">
                All provider applications have been verified.
              </div>
            )}
          </div>
        </div>

        {/* System Health Architecture Card */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
          <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
            <Server className="w-4 h-4 text-indigo-600" />
            <span>Infrastructure Health</span>
          </h3>

          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 flex justify-between items-center">
              <div>
                <span className="font-semibold text-slate-800 block">Spring Boot 4.1.1</span>
                <span className="text-slate-400 text-[11px]">Java 21 Virtual Threads</span>
              </div>
              <span className="text-emerald-600 font-bold">ONLINE</span>
            </div>

            <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 flex justify-between items-center">
              <div>
                <span className="font-semibold text-slate-800 block">MySQL 8.x Cluster</span>
                <span className="text-slate-400 text-[11px]">Primary + Read Replicas</span>
              </div>
              <span className="text-emerald-600 font-bold">HEALTHY</span>
            </div>

            <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 flex justify-between items-center">
              <div>
                <span className="font-semibold text-slate-800 block">Dual-Token JWT Gateway</span>
                <span className="text-slate-400 text-[11px]">Automated Token Rotation</span>
              </div>
              <span className="text-emerald-600 font-bold">ACTIVE</span>
            </div>

            <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 flex justify-between items-center">
              <div>
                <span className="font-semibold text-slate-800 block">Immutable Audit Log</span>
                <span className="text-slate-400 text-[11px]">HIPAA & GDPR Compliant</span>
              </div>
              <span className="text-emerald-600 font-bold">SECURE</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;

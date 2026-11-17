import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Building2, Bed, Users, FileCheck2, Plus, Search, Activity, ArrowRight } from 'lucide-react';
import medicalOrgService from '../../services/medicalOrgService';
import DashboardCard from '../../components/dashboard/DashboardCard';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const MedicalDashboard = () => {
  const navigate = useNavigate();
  const [admissions, setAdmissions] = useState([]);
  const [searchHealthId, setSearchHealthId] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    medicalOrgService.getAdmissions().then((data) => {
      setAdmissions(data);
      setIsLoading(false);
    });
  }, []);

  const handleLookup = (e) => {
    e.preventDefault();
    if (!searchHealthId.trim()) return;
    navigate(`/medical/lookup?id=${encodeURIComponent(searchHealthId.trim())}`);
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading hospital command center..." />;
  }

  const activeAdmissions = admissions.filter((a) => a.status === 'ADMITTED');

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-purple-900 via-indigo-950 to-navy-950 p-6 rounded-2xl text-white shadow-lg border border-purple-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs uppercase font-bold tracking-wider text-purple-300">
              Hospital Management Center • Inpatient Floor
            </span>
            <DemoDataBadge label="Inpatient Workflow Preview" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight mt-1">Metro General Hospital</h2>
          <p className="text-xs text-purple-200/80 mt-1">
            License: HOSP-TX-44021 • 350 Bed Capacity • Acute Care Facility
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link to="/medical/admissions">
            <Button variant="primary" icon={Plus} size="sm" className="bg-purple-600 hover:bg-purple-500">
              New Inpatient Admission
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard
          title="Occupied Beds"
          value="242 / 350"
          subtitle="69% Bed Occupancy"
          icon={Bed}
          colorScheme="purple"
        />
        <DashboardCard
          title="Active Admissions"
          value={activeAdmissions.length}
          subtitle="Inpatient wards"
          icon={Users}
          colorScheme="blue"
        />
        <DashboardCard
          title="Today's Discharges"
          value="14"
          subtitle="Summaries recorded"
          icon={FileCheck2}
          colorScheme="emerald"
        />
        <DashboardCard
          title="ICU Critical Beds"
          value="18 / 24"
          subtitle="75% Critical capacity"
          icon={Activity}
          colorScheme="red"
        />
      </div>

      {/* Patient Search for Admission */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center gap-2 mb-2">
          <Search className="w-4 h-4 text-purple-600" />
          <h3 className="text-sm font-bold text-slate-900">Hospital Patient Admission Search</h3>
        </div>
        <p className="text-xs text-slate-500 mb-3">
          Search patient by Universal Health ID to initiate inpatient admission, ward assignment, and physician allocation
        </p>

        <form onSubmit={handleLookup} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={searchHealthId}
            onChange={(e) => setSearchHealthId(e.target.value)}
            placeholder="e.g. MED-99887766-P"
            className="flex-1 px-3.5 py-2 rounded-xl border border-slate-300 text-xs font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500"
          />
          <Button type="submit" variant="primary" icon={Search} size="sm" className="bg-purple-600 hover:bg-purple-500">
            Search Patient
          </Button>
        </form>
      </div>

      {/* Current Inpatient Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div>
            <h3 className="text-base font-bold text-slate-900">Current Inpatient Census</h3>
            <p className="text-xs text-slate-500">Patients currently admitted across wards and clinical wings</p>
          </div>
          <Link to="/medical/admissions">
            <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
              All Admissions
            </Button>
          </Link>
        </div>

        <div className="divide-y divide-slate-100 mt-2">
          {admissions.map((adm) => (
            <div key={adm.id} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shrink-0 border border-purple-100">
                  <Bed className="w-5 h-5" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h5 className="font-bold text-slate-900 text-sm">{adm.patientName}</h5>
                    <span className="font-mono text-[11px] text-purple-700 bg-purple-50 px-2 py-0.5 rounded">
                      {adm.healthId}
                    </span>
                    <StatusBadge status={adm.status} size="sm" />
                  </div>
                  <p className="text-slate-500 mt-0.5">
                    {adm.ward} • <strong className="text-slate-800">{adm.bedNumber}</strong> • {adm.department}
                  </p>
                  <span className="text-slate-400 mt-1 block">
                    Admitted: {new Date(adm.admissionDate).toLocaleDateString()} • Attending: {adm.admittingDoctor}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Link to={`/medical/discharges?id=${adm.id}`}>
                  <Button variant="outline" size="sm">
                    Discharge Process
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

export default MedicalDashboard;

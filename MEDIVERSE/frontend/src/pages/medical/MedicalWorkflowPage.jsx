import React from 'react';
import { Activity, Bed, Users, ShieldAlert, CheckCircle2 } from 'lucide-react';
import DemoDataBadge from '../../components/common/DemoDataBadge';

export const MedicalWorkflowPage = () => {
  const wards = [
    { name: 'Ward 4B (Pulmonology & Respiratory)', occupied: 28, total: 32, chiefNurse: 'Nurse Sarah O.' },
    { name: 'Ward 2A (Hematology & Oncology)', occupied: 18, total: 20, chiefNurse: 'Nurse David K.' },
    { name: 'Ward 3C (Cardiovascular Care Unit)', occupied: 24, total: 24, chiefNurse: 'Nurse Elena M.' },
    { name: 'Intensive Care Unit (ICU)', occupied: 18, total: 24, chiefNurse: 'Dr. T. Vance, MD' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Inpatient Floor Workflow Tracker</h2>
          <p className="text-xs text-slate-500 mt-1">
            Real-time department occupancy, nursing handoffs, and bed management
          </p>
        </div>
        <DemoDataBadge label="Inpatient Workflow Preview" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {wards.map((ward, idx) => (
          <div key={idx} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs space-y-4">
            <div className="flex items-start justify-between">
              <div>
                <h4 className="text-base font-bold text-slate-900">{ward.name}</h4>
                <p className="text-xs text-slate-500 mt-0.5">Floor Coordinator: {ward.chiefNurse}</p>
              </div>
              <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${ward.occupied === ward.total ? 'bg-red-100 text-red-800' : 'bg-emerald-100 text-emerald-800'}`}>
                {ward.occupied} / {ward.total} Beds
              </span>
            </div>

            {/* Progress Bar */}
            <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
              <div
                className={`h-2.5 rounded-full ${ward.occupied === ward.total ? 'bg-red-500' : 'bg-purple-600'}`}
                style={{ width: `${(ward.occupied / ward.total) * 100}%` }}
              />
            </div>

            <div className="flex justify-between text-xs text-slate-500 pt-2 border-t border-slate-100">
              <span>Telemetry: Active</span>
              <span className="font-semibold text-slate-800">{ward.total - ward.occupied} Available Beds</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MedicalWorkflowPage;

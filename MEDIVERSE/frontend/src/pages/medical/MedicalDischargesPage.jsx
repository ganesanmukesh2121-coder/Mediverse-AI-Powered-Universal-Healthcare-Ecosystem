import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { FileCheck2, User, Clock, CheckCircle2, Bed, Save } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import medicalOrgService from '../../services/medicalOrgService';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const MedicalDischargesPage = () => {
  const [searchParams] = useSearchParams();
  const { notifySuccess } = useNotification();
  const [admissions, setAdmissions] = useState([]);
  const [selectedAdmission, setSelectedAdmission] = useState(null);
  const [dischargeSummary, setDischargeSummary] = useState('');
  const [isDischarging, setIsDischarging] = useState(false);

  useEffect(() => {
    medicalOrgService.getAdmissions().then((data) => {
      setAdmissions(data);
      const targetId = searchParams.get('id');
      if (targetId) {
        const found = data.find((a) => a.id === targetId);
        if (found) setSelectedAdmission(found);
      }
    });
  }, [searchParams]);

  const handleDischarge = async (e) => {
    e.preventDefault();
    if (!selectedAdmission) return;
    setIsDischarging(true);
    try {
      await medicalOrgService.dischargePatient(selectedAdmission.id, { dischargeSummary });
      setAdmissions((prev) =>
        prev.map((a) =>
          a.id === selectedAdmission.id
            ? { ...a, status: 'DISCHARGED', dischargeSummary, dischargeDate: new Date().toISOString() }
            : a
        )
      );
      notifySuccess(`Patient ${selectedAdmission.patientName} discharged successfully.`);
      setSelectedAdmission(null);
    } finally {
      setIsDischarging(false);
    }
  };

  const dischargedList = admissions.filter((a) => a.status === 'DISCHARGED');

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Discharge Summaries & Orders</h2>
          <p className="text-xs text-slate-500 mt-1">
            Generate formal clinical discharge summaries, post-acute directives, and bed release
          </p>
        </div>
        <DemoDataBadge label="Inpatient Workflow Preview" />
      </div>

      {selectedAdmission && (
        <form onSubmit={handleDischarge} className="bg-white rounded-2xl border border-purple-300 p-6 shadow-md space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h3 className="text-base font-bold text-slate-900">
                Discharge Execution: {selectedAdmission.patientName}
              </h3>
              <p className="text-xs text-slate-500">
                UHID: {selectedAdmission.healthId} • Bed: {selectedAdmission.bedNumber} ({selectedAdmission.ward})
              </p>
            </div>
            <StatusBadge status="ADMITTED" />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
              Comprehensive Clinical Discharge Summary & Post-Hospital Care Instructions
            </label>
            <textarea
              rows={4}
              value={dischargeSummary}
              onChange={(e) => setDischargeSummary(e.target.value)}
              placeholder="Document resolved clinical status, vital stability upon discharge, activity restrictions, and follow-up appointment date..."
              className="w-full rounded-xl border border-slate-300 text-xs p-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500"
              required
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" onClick={() => setSelectedAdmission(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" icon={FileCheck2} isLoading={isDischarging} className="bg-purple-600 hover:bg-purple-500">
              Sign Discharge & Release Bed
            </Button>
          </div>
        </form>
      )}

      {/* Discharged Patients History */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900">Discharged Patients Archive</h3>
        <div className="divide-y divide-slate-100">
          {dischargedList.map((item) => (
            <div key={item.id} className="py-3.5 space-y-1.5 text-xs">
              <div className="flex items-center justify-between">
                <span className="font-bold text-slate-900 text-sm">{item.patientName} ({item.healthId})</span>
                <span className="text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded font-bold">DISCHARGED</span>
              </div>
              <p className="text-slate-600">
                <strong>Discharge Summary:</strong> {item.dischargeSummary}
              </p>
              <span className="text-[11px] text-slate-400 block">
                Discharged on: {item.dischargeDate ? new Date(item.dischargeDate).toLocaleDateString() : 'Recent'}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default MedicalDischargesPage;

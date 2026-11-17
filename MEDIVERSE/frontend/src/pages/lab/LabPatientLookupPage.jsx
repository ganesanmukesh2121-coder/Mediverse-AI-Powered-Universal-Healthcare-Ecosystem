import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, FlaskConical, User, CheckCircle2 } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import doctorService from '../../services/doctorService';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Button from '../../components/common/Button';

export const LabPatientLookupPage = () => {
  const navigate = useNavigate();
  const { notifyError } = useNotification();
  const [healthId, setHealthId] = useState('');
  const [patient, setPatient] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!healthId.trim()) return;
    setIsLoading(true);
    try {
      const data = await doctorService.lookupPatientByHealthId(healthId.trim());
      setPatient(data);
    } catch {
      notifyError('No patient record found.');
      setPatient(null);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Diagnostic Specimen & Patient Lookup</h2>
          <p className="text-xs text-slate-500 mt-1">
            Search patient records by Health ID to link specimens or generate manual lab test orders
          </p>
        </div>
        <DemoDataBadge label="LIS Processing Preview" />
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={healthId}
            onChange={(e) => setHealthId(e.target.value)}
            placeholder="Enter Patient Health ID (e.g. MED-10293847-P)"
            className="flex-1 px-4 py-2.5 rounded-xl border border-slate-300 text-sm font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-cyan-500/20 focus:border-cyan-500"
          />
          <Button
            type="submit"
            variant="primary"
            icon={Search}
            className="bg-cyan-600 hover:bg-cyan-500"
            isLoading={isLoading}
          >
            Find Patient
          </Button>
        </form>
      </div>

      {patient && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xl p-6 space-y-6">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <h3 className="text-xl font-bold text-slate-900">{patient.fullName}</h3>
              <p className="text-xs text-slate-500">
                UHID: <span className="font-mono font-bold text-cyan-700">{patient.healthId}</span> • DOB: {patient.dateOfBirth}
              </p>
            </div>
            <Button
              variant="primary"
              icon={FlaskConical}
              size="sm"
              className="bg-cyan-600 hover:bg-cyan-500"
              onClick={() => navigate(`/lab/reports?healthId=${patient.healthId}&patientName=${encodeURIComponent(patient.fullName)}`)}
            >
              Order / Input Lab Results
            </Button>
          </div>

          <div className="grid grid-cols-2 gap-4 text-xs">
            <div className="p-3.5 bg-slate-50 rounded-xl">
              <span className="text-slate-400 font-bold block uppercase text-[10px]">Blood Group</span>
              <span className="font-bold text-slate-900 mt-1 block">{patient.bloodGroup}</span>
            </div>
            <div className="p-3.5 bg-slate-50 rounded-xl">
              <span className="text-slate-400 font-bold block uppercase text-[10px]">Active Consent</span>
              <span className="font-bold text-emerald-600 mt-1 block">YES (LAB_ONLY or FULL)</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LabPatientLookupPage;

import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Search, Bed, User, Building2, Stethoscope } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import doctorService from '../../services/doctorService';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Button from '../../components/common/Button';

export const MedicalPatientLookupPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useNotification();

  const [healthId, setHealthId] = useState(searchParams.get('id') || '');
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
      notifyError('Patient not found.');
      setPatient(null);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Hospital Inpatient Admission Search</h2>
          <p className="text-xs text-slate-500 mt-1">
            Query patients by Universal Health ID to process admission, assign department and bed
          </p>
        </div>
        <DemoDataBadge label="Inpatient Workflow Preview" />
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={healthId}
            onChange={(e) => setHealthId(e.target.value)}
            placeholder="Enter Patient Health ID (e.g. MED-99887766-P)"
            className="flex-1 px-4 py-2.5 rounded-xl border border-slate-300 text-sm font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500"
          />
          <Button
            type="submit"
            variant="primary"
            icon={Search}
            className="bg-purple-600 hover:bg-purple-500"
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
                UHID: <span className="font-mono font-bold text-purple-700">{patient.healthId}</span> • DOB: {patient.dateOfBirth}
              </p>
            </div>
            <Button
              variant="primary"
              icon={Bed}
              size="sm"
              className="bg-purple-600 hover:bg-purple-500"
              onClick={() => navigate(`/medical/admissions?healthId=${patient.healthId}&name=${encodeURIComponent(patient.fullName)}`)}
            >
              Admit to Inpatient Floor
            </Button>
          </div>

          <div className="grid grid-cols-2 gap-4 text-xs">
            <div className="p-3.5 bg-slate-50 rounded-xl">
              <span className="text-slate-400 font-bold block uppercase text-[10px]">Blood Group</span>
              <span className="font-bold text-slate-900 mt-1 block">{patient.bloodGroup}</span>
            </div>
            <div className="p-3.5 bg-slate-50 rounded-xl">
              <span className="text-slate-400 font-bold block uppercase text-[10px]">Allergies</span>
              <span className="font-bold text-red-600 mt-1 block">{(patient.allergies || []).join(', ') || 'None'}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MedicalPatientLookupPage;

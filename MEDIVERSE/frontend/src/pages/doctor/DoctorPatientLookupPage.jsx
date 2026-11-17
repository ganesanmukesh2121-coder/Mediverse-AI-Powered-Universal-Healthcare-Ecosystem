import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { Search, User, ShieldCheck, Stethoscope, Pill, Activity, AlertTriangle, FileText, Clock } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import doctorService from '../../services/doctorService';
import StatusBadge from '../../components/common/StatusBadge';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const DoctorPatientLookupPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { notifySuccess, notifyError } = useNotification();

  const [healthId, setHealthId] = useState(searchParams.get('id') || '');
  const [patient, setPatient] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const id = searchParams.get('id');
    if (id) {
      setHealthId(id);
      performLookup(id);
    }
  }, [searchParams]);

  const performLookup = async (idToSearch) => {
    setIsLoading(true);
    try {
      const data = await doctorService.lookupPatientByHealthId(idToSearch);
      setPatient(data);
    } catch (err) {
      notifyError(err.message || 'Patient not found');
      setPatient(null);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (!healthId.trim()) return;
    performLookup(healthId.trim());
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Clinical Patient Lookup</h2>
        <p className="text-xs text-slate-500 mt-1">
          Search patient records by Universal Health ID, verify active consent scopes, or initiate a documented walk-in encounter
        </p>
      </div>

      {/* Search Input Box */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={healthId}
            onChange={(e) => setHealthId(e.target.value)}
            placeholder="Enter Patient Health ID (e.g. MED-10293847-P)"
            className="flex-1 px-4 py-2.5 rounded-xl border border-slate-300 text-sm font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
          />
          <Button
            type="submit"
            variant="primary"
            icon={Search}
            className="bg-blue-600 hover:bg-blue-500"
            isLoading={isLoading}
          >
            Verify Patient
          </Button>
        </form>

        <div className="mt-3 flex items-center gap-2 text-xs text-slate-500">
          <span>Available test patients:</span>
          <button
            type="button"
            onClick={() => {
              setHealthId('MED-10293847-P');
              performLookup('MED-10293847-P');
            }}
            className="font-mono text-blue-600 font-bold hover:underline"
          >
            MED-10293847-P (Sarah Jenkins)
          </button>
          <span>•</span>
          <button
            type="button"
            onClick={() => {
              setHealthId('MED-99887766-P');
              performLookup('MED-99887766-P');
            }}
            className="font-mono text-blue-600 font-bold hover:underline"
          >
            MED-99887766-P (David Vance)
          </button>
        </div>
      </div>

      {/* Patient Result Card */}
      {patient && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xl p-6 space-y-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-xl font-bold text-slate-900">{patient.fullName}</h3>
                <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2.5 py-1 rounded-md border border-blue-200">
                  {patient.healthId}
                </span>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                DOB: {patient.dateOfBirth} • Gender: {patient.gender} • Blood Group: {patient.bloodGroup}
              </p>
            </div>

            <div className="flex items-center gap-2">
              <StatusBadge status="ACTIVE" />
              <StatusBadge status={patient.consentCategory || 'FULL_TIMELINE'} />
            </div>
          </div>

          {/* Quick Demographics & Allergies Overview */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
              <span className="text-[10px] uppercase font-bold text-slate-400 block">
                Allergies & Sensitivities
              </span>
              <div className="flex flex-wrap gap-1.5">
                {(patient.allergies || []).map((alg, idx) => (
                  <span key={idx} className="px-2 py-0.5 rounded bg-red-100 text-red-800 font-semibold">
                    {alg}
                  </span>
                ))}
              </div>
            </div>

            <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
              <span className="text-[10px] uppercase font-bold text-slate-400 block">
                Chronic Medical Conditions
              </span>
              <div className="flex flex-wrap gap-1.5">
                {(patient.chronicConditions || []).map((cnd, idx) => (
                  <span key={idx} className="px-2 py-0.5 rounded bg-amber-100 text-amber-800 font-semibold">
                    {cnd}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Action Launchpad */}
          <div className="p-4 bg-blue-50/60 rounded-xl border border-blue-200 space-y-3">
            <div className="flex items-center gap-2 text-xs font-bold text-blue-900 uppercase tracking-wider">
              <Stethoscope className="w-4 h-4 text-blue-600" />
              <span>Initiate Clinical Care Actions</span>
            </div>

            <div className="flex flex-wrap gap-3">
              <Link
                to={`/doctor/consultations?healthId=${patient.healthId}&patientName=${encodeURIComponent(
                  patient.fullName
                )}&context=DOCUMENTED_WALK_IN`}
              >
                <Button variant="primary" icon={Stethoscope} size="sm" className="bg-blue-600 hover:bg-blue-500">
                  Document Walk-In Encounter
                </Button>
              </Link>

              <Link
                to={`/doctor/prescriptions?healthId=${patient.healthId}&patientName=${encodeURIComponent(
                  patient.fullName
                )}`}
              >
                <Button variant="outline" icon={Pill} size="sm">
                  Write Digital Prescription
                </Button>
              </Link>

              <Link
                to={`/doctor/timeline?healthId=${patient.healthId}&category=${patient.consentCategory || 'FULL_TIMELINE'}`}
              >
                <Button variant="secondary" icon={Activity} size="sm">
                  Explore Patient Timeline
                </Button>
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorPatientLookupPage;

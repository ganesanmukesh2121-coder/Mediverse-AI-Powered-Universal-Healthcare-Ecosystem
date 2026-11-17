import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Users, Search, Activity, Stethoscope, Pill, ShieldCheck, Clock, Calendar } from 'lucide-react';
import doctorService from '../../services/doctorService';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/dashboard/SearchBar';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const DoctorAuthorizedPatientsPage = () => {
  const [patients, setPatients] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    doctorService.getAuthorizedPatients().then((data) => {
      setPatients(data);
      setIsLoading(false);
    });
  }, []);

  const filteredPatients = patients.filter((p) => {
    const term = searchTerm.toLowerCase();
    return (
      p.fullName.toLowerCase().includes(term) ||
      p.healthId.toLowerCase().includes(term) ||
      p.bloodGroup.toLowerCase().includes(term)
    );
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading authorized patient panel..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Authorized Patient Panel</h2>
          <p className="text-xs text-slate-500 mt-1">
            Patients who have active cryptographic consent granted to your clinical physician account
          </p>
        </div>
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Filter by patient name, Health ID, blood group..."
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filteredPatients.map((patient) => (
          <div
            key={patient.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs hover:border-blue-300 transition-all flex flex-col justify-between"
          >
            <div>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h4 className="text-base font-bold text-slate-900">{patient.fullName}</h4>
                  <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-200 mt-0.5 inline-block">
                    {patient.healthId}
                  </span>
                </div>
                <StatusBadge status={patient.consentCategory || 'FULL_TIMELINE'} size="sm" />
              </div>

              <div className="mt-3 space-y-1.5 text-xs text-slate-500 border-t border-slate-100 pt-3">
                <div className="flex justify-between">
                  <span>Demographics:</span>
                  <span className="font-semibold text-slate-700">
                    {patient.gender} • DOB {patient.dateOfBirth}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span>Blood Group:</span>
                  <span className="font-semibold text-rose-600">{patient.bloodGroup}</span>
                </div>
                <div className="flex justify-between">
                  <span>Consent Expires:</span>
                  <span className="font-medium text-slate-700">
                    {new Date(patient.consentExpiresAt).toLocaleDateString()}
                  </span>
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
              <Link
                to={`/doctor/timeline?healthId=${patient.healthId}&category=${patient.consentCategory}`}
                className="flex-1"
              >
                <Button variant="secondary" size="sm" icon={Activity} className="w-full text-xs">
                  Timeline
                </Button>
              </Link>
              <Link
                to={`/doctor/consultations?healthId=${patient.healthId}&patientName=${encodeURIComponent(
                  patient.fullName
                )}`}
                className="flex-1"
              >
                <Button variant="primary" size="sm" icon={Stethoscope} className="w-full text-xs bg-blue-600 hover:bg-blue-500">
                  Encounter
                </Button>
              </Link>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DoctorAuthorizedPatientsPage;

import React, { useState, useEffect } from 'react';
import { FileText, Calendar, User, Activity, Stethoscope, Eye, Search, Filter } from 'lucide-react';
import patientService from '../../services/patientService';
import SearchBar from '../../components/dashboard/SearchBar';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const MedicalRecordsPage = () => {
  const [records, setRecords] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    patientService.getMedicalRecords().then((data) => {
      setRecords(data);
      setIsLoading(false);
    });
  }, []);

  const filteredRecords = records.filter((r) => {
    const term = searchTerm.toLowerCase();
    return (
      r.doctorName.toLowerCase().includes(term) ||
      r.specialty.toLowerCase().includes(term) ||
      r.diagnosis.toLowerCase().includes(term) ||
      r.chiefComplaint.toLowerCase().includes(term)
    );
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading medical records..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Lifetime Medical Records</h2>
          <p className="text-xs text-slate-500 mt-1">
            Verified physician consultation notes, diagnoses, and outpatient clinical documentation
          </p>
        </div>
      </div>

      {/* Search Bar */}
      <div className="max-w-md">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Search by physician, diagnosis, ICD-10, or condition..."
        />
      </div>

      {/* Chronological Clinical Timeline */}
      <div className="space-y-4">
        {filteredRecords.map((record) => (
          <div
            key={record.id}
            className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs hover:border-mediverse-300 transition-all"
          >
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center border border-blue-100 shrink-0">
                  <Stethoscope className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-base font-bold text-slate-900">{record.diagnosis}</h4>
                  <p className="text-xs text-slate-500">
                    {record.doctorName} • {record.specialty}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-xs text-slate-500 flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-slate-400" />
                  {new Date(record.encounterDate).toLocaleDateString([], { dateStyle: 'long' })}
                </span>
                <span className="text-[11px] font-bold px-2 py-0.5 rounded-md bg-slate-100 text-slate-700 uppercase">
                  {record.encounterType}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  icon={Eye}
                  onClick={() => setSelectedRecord(record)}
                >
                  View Details
                </Button>
              </div>
            </div>

            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">
                  Chief Complaint
                </span>
                <p className="text-slate-800 font-medium mt-1">{record.chiefComplaint}</p>
              </div>

              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400 block">
                  ICD-10 Code & Status
                </span>
                <p className="text-slate-800 font-mono font-bold mt-1">
                  {record.icd10} — Clinical Verification Confirmed
                </p>
              </div>
            </div>

            <div className="mt-3">
              <p className="text-xs text-slate-600 line-clamp-2 leading-relaxed">
                <strong className="text-slate-800">Doctor Note: </strong>
                {record.clinicalNotes}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Record Details Modal */}
      <Modal
        isOpen={!!selectedRecord}
        onClose={() => setSelectedRecord(null)}
        title={selectedRecord?.diagnosis}
        subtitle={`${selectedRecord?.doctorName} • ${selectedRecord?.specialty}`}
        maxWidth="max-w-2xl"
      >
        {selectedRecord && (
          <div className="space-y-4 text-xs">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <span className="text-slate-500">
                Encounter Date: {new Date(selectedRecord.encounterDate).toLocaleString()}
              </span>
              <span className="font-mono font-bold text-mediverse-700 bg-mediverse-50 px-2 py-0.5 rounded">
                ICD: {selectedRecord.icd10}
              </span>
            </div>

            <div>
              <h5 className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                Vital Signs Recorded
              </h5>
              <div className="grid grid-cols-3 gap-2 mt-2">
                {Object.entries(selectedRecord.vitalSigns || {}).map(([key, val]) => (
                  <div key={key} className="p-2 rounded-lg bg-slate-50 border border-slate-100">
                    <span className="text-[10px] text-slate-400 uppercase block">{key}</span>
                    <span className="font-semibold text-slate-800">{val}</span>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <h5 className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                Clinical Evaluation & Physician Notes
              </h5>
              <p className="mt-1.5 p-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-700 leading-relaxed">
                {selectedRecord.clinicalNotes}
              </p>
            </div>

            <div>
              <h5 className="font-bold text-slate-900 uppercase tracking-wider text-[11px]">
                Treatment Plan & Directives
              </h5>
              <p className="mt-1.5 p-3 rounded-xl bg-emerald-50/60 border border-emerald-200 text-emerald-950 font-medium leading-relaxed">
                {selectedRecord.treatmentPlan}
              </p>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default MedicalRecordsPage;

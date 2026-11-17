import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Activity, ShieldCheck, Stethoscope, Pill, FlaskConical, Calendar, Clock, Filter } from 'lucide-react';
import doctorService from '../../services/doctorService';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const DoctorPatientTimelinePage = () => {
  const [searchParams] = useSearchParams();
  const [healthId, setHealthId] = useState(searchParams.get('healthId') || 'MED-10293847-P');
  const [category, setCategory] = useState(searchParams.get('category') || 'FULL_TIMELINE');
  const [timelineData, setTimelineData] = useState({ records: [], prescriptions: [] });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setIsLoading(true);
    doctorService.getPatientTimeline(healthId, category).then((data) => {
      setTimelineData(data);
      setIsLoading(false);
    });
  }, [healthId, category]);

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Clinical Timeline Explorer</h2>
          <p className="text-xs text-slate-500 mt-1">
            Historical outpatient encounters, medications, and clinical notes filtered by patient consent category
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-slate-500">Active Scope:</span>
          <StatusBadge status={category} size="md" />
        </div>
      </div>

      {/* Scope Filter Tabs */}
      <div className="p-4 bg-white rounded-2xl border border-slate-200 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-slate-700">Enforced Consent Filter:</span>
          <div className="flex items-center gap-1.5 text-xs">
            {['FULL_TIMELINE', 'CONSULT_ONLY', 'LAB_ONLY'].map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setCategory(cat)}
                className={`px-3 py-1.5 rounded-lg font-semibold transition-all ${
                  category === cat
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-slate-600 bg-slate-100 hover:bg-slate-200'
                }`}
              >
                {cat.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        <span className="text-xs font-mono text-blue-700 font-bold bg-blue-50 px-2.5 py-1 rounded-md border border-blue-200">
          Target: {healthId}
        </span>
      </div>

      {/* Timeline Stream */}
      {isLoading ? (
        <LoadingSpinner message="Filtering records under cryptographic consent rules..." />
      ) : (
        <div className="space-y-4">
          {category !== 'LAB_ONLY' &&
            timelineData.records.map((rec) => (
              <div
                key={rec.id}
                className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-3"
              >
                <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                  <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
                      <Stethoscope className="w-4 h-4" />
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-slate-900">{rec.diagnosis}</h4>
                      <p className="text-[11px] text-slate-500">{rec.doctorName} • {rec.specialty}</p>
                    </div>
                  </div>
                  <span className="text-xs text-slate-500 font-medium">
                    {new Date(rec.encounterDate).toLocaleDateString([], { dateStyle: 'long' })}
                  </span>
                </div>

                <div className="text-xs space-y-2">
                  <p className="text-slate-600">
                    <strong className="text-slate-800">Chief Complaint: </strong>
                    {rec.chiefComplaint}
                  </p>
                  <p className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-slate-700 leading-relaxed">
                    <strong className="text-slate-900 block mb-1">Clinical Notes:</strong>
                    {rec.clinicalNotes}
                  </p>
                </div>
              </div>
            ))}

          {category === 'LAB_ONLY' && (
            <div className="p-8 text-center bg-white rounded-2xl border border-slate-200 text-xs text-slate-500">
              Under <strong>LAB_ONLY</strong> consent, physician consultations and prescriptions are filtered out by security policy. Only laboratory diagnostic files are accessible.
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DoctorPatientTimelinePage;

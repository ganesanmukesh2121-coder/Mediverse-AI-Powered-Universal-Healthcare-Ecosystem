import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  AlertTriangle,
  Search,
  ShieldAlert,
  Droplet,
  HeartPulse,
  Phone,
  User,
  ShieldCheck,
  Clock,
  Printer,
  CheckCircle2,
} from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import emergencyService from '../../services/emergencyService';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const EmergencyLookupPage = () => {
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotification();

  const [healthId, setHealthId] = useState(searchParams.get('id') || '');
  const [justificationReason, setJustificationReason] = useState(
    'Unresponsive patient in acute trauma / suspected allergic shock during transit'
  );
  const [certified, setCertified] = useState(false);
  const [profile, setProfile] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [auditTimestamp, setAuditTimestamp] = useState(null);

  useEffect(() => {
    const idParam = searchParams.get('id');
    if (idParam) {
      setHealthId(idParam);
    }
  }, [searchParams]);

  const handleLookup = async (e) => {
    e.preventDefault();
    if (!healthId.trim()) {
      notifyError('Please enter a valid Patient Health ID.');
      return;
    }
    if (!justificationReason.trim() || justificationReason.trim().length < 10) {
      notifyError('Mandatory emergency clinical justification must be at least 10 characters.');
      return;
    }
    if (!certified) {
      notifyError('You must certify active emergency responder intervention before proceeding.');
      return;
    }

    setIsLoading(true);
    try {
      const data = await emergencyService.lookupPatientEmergencyInfo(healthId, justificationReason);
      setProfile(data);
      setAuditTimestamp(new Date().toISOString());
      notifySuccess('Emergency triage profile retrieved and access permanently recorded in the system audit log.');
    } catch (err) {
      notifyError(err.message || 'Emergency lookup failed.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">
          Emergency Triage Record Lookup
        </h2>
        <p className="text-xs text-slate-500 mt-1">
          Strictly gated triage access for licensed emergency responders and trauma bay clinicians
        </p>
      </div>

      {/* Lookup Form */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <form onSubmit={handleLookup} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="sm:col-span-1">
              <FormInput
                label="Patient Universal Health ID"
                name="healthId"
                placeholder="e.g. MED-10293847-P"
                value={healthId}
                onChange={(e) => setHealthId(e.target.value)}
                required
              />
              <div className="mt-1 flex items-center gap-1.5 text-[10px] text-slate-400">
                <span>Demo IDs:</span>
                <button
                  type="button"
                  onClick={() => setHealthId('MED-10293847-P')}
                  className="text-red-600 font-mono font-bold hover:underline"
                >
                  MED-10293847-P
                </button>
                <span>•</span>
                <button
                  type="button"
                  onClick={() => setHealthId('MED-99887766-P')}
                  className="text-red-600 font-mono font-bold hover:underline"
                >
                  MED-99887766-P
                </button>
              </div>
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-700 tracking-wide mb-1.5">
                Mandatory Emergency Justification <span className="text-red-500">*</span>
              </label>
              <textarea
                rows={2}
                value={justificationReason}
                onChange={(e) => setJustificationReason(e.target.value)}
                placeholder="Specific clinical condition warranting emergency bypass (e.g. Unconscious patient with anaphylaxis)"
                className="w-full rounded-lg border border-slate-300 text-xs p-2.5 text-slate-900 focus:outline-none focus:ring-2 focus:ring-red-500/20 focus:border-red-500"
                required
              />
            </div>
          </div>

          <div className="p-3 bg-red-50/80 rounded-xl border border-red-200 flex items-start gap-3">
            <input
              type="checkbox"
              id="certify"
              checked={certified}
              onChange={(e) => setCertified(e.target.checked)}
              className="mt-0.5 rounded border-red-300 text-red-600 focus:ring-red-500 cursor-pointer"
            />
            <label htmlFor="certify" className="text-xs text-red-950 font-medium cursor-pointer">
              <strong>STAT Emergency Responder Declaration:</strong> I hereby certify under penalty of law and healthcare compliance regulations that I am an actively responding licensed clinician/first-responder in an urgent care emergency context. This query is permanently bound to my user credential and logged in the immutable security audit log.
            </label>
          </div>

          <div className="flex justify-end pt-2">
            <Button
              type="submit"
              variant="danger"
              icon={Search}
              size="lg"
              isLoading={isLoading}
              disabled={!certified || !healthId.trim()}
            >
              Access Emergency Triage Data
            </Button>
          </div>
        </form>
      </div>

      {/* Result Triage Card */}
      {profile && (
        <div className="bg-white rounded-2xl border-2 border-red-500 shadow-xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
          {/* Header Banner */}
          <div className="bg-gradient-to-r from-red-700 via-rose-700 to-red-800 p-5 text-white flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-black uppercase tracking-wider bg-white/20 px-2.5 py-0.5 rounded-full">
                  STAT EMERGENCY TRIAGE CARD
                </span>
                <span className="text-xs text-red-200 font-mono">
                  LOGGED: {new Date(auditTimestamp).toLocaleTimeString()}
                </span>
              </div>
              <h3 className="text-2xl font-black mt-1 tracking-tight">{profile.fullName}</h3>
              <p className="text-xs text-red-100 font-mono">UHID: {profile.healthId}</p>
            </div>

            <Button
              variant="secondary"
              size="sm"
              icon={Printer}
              onClick={() => window.print()}
              className="bg-white/90 text-red-900 hover:bg-white"
            >
              Print Triage Sheet
            </Button>
          </div>

          {/* Strict Notice regarding data boundary */}
          <div className="px-6 py-2.5 bg-slate-900 text-white flex items-center justify-between text-[11px]">
            <span className="flex items-center gap-1.5 text-red-300 font-bold uppercase tracking-wider">
              <ShieldAlert className="w-3.5 h-3.5" />
              <span>Restricted Emergency Boundary Enforced</span>
            </span>
            <span className="text-slate-400">
              Historical consultation notes & general outpatient records are strictly withheld
            </span>
          </div>

          {/* Vital Emergency Parameters */}
          <div className="p-6 space-y-6">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              <div className="p-4 rounded-xl bg-red-50 border border-red-200">
                <span className="text-[10px] font-bold uppercase tracking-wider text-red-600 block">Blood Group</span>
                <span className="text-2xl font-black text-red-900 mt-1 flex items-center gap-1.5">
                  <Droplet className="w-6 h-6 text-red-600" />
                  {profile.bloodGroup}
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">Date of Birth</span>
                <span className="text-base font-bold text-slate-800 mt-1 block">{profile.dateOfBirth}</span>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">Organ Donor</span>
                <span className="text-base font-bold text-slate-800 mt-1 block">
                  {profile.organDonor ? 'YES (Donor)' : 'NO'}
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
                <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">DNR Order</span>
                <span className="text-base font-bold text-slate-800 mt-1 block">
                  {profile.dnrStatus ? 'YES (Do Not Resuscitate)' : 'NO (Full Resuscitation)'}
                </span>
              </div>
            </div>

            {/* Allergies - Critical Red Box */}
            <div className="p-4 rounded-xl bg-red-50/90 border border-red-300">
              <h4 className="text-xs font-black uppercase tracking-wider text-red-900 flex items-center gap-1.5 mb-2">
                <AlertTriangle className="w-4 h-4 text-red-600" />
                <span>Life-Critical Drug & Food Allergies</span>
              </h4>
              <div className="flex flex-wrap gap-2">
                {profile.allergies?.map((alg, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1.5 rounded-lg bg-red-600 text-white font-bold text-xs shadow-xs"
                  >
                    {alg}
                  </span>
                ))}
              </div>
            </div>

            {/* Critical Conditions */}
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-600 mb-2">
                Underlying Chronic Conditions
              </h4>
              <div className="flex flex-wrap gap-2">
                {profile.criticalConditions?.map((cond, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1.5 rounded-lg bg-amber-50 border border-amber-200 text-amber-900 font-semibold text-xs"
                  >
                    {cond}
                  </span>
                ))}
              </div>
            </div>

            {/* Current Active Medications */}
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-600 mb-2">
                Active Emergency Medications
              </h4>
              <div className="flex flex-wrap gap-2">
                {profile.activeMedications?.map((med, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1.5 rounded-lg bg-slate-100 border border-slate-200 text-slate-800 font-semibold text-xs"
                  >
                    {med}
                  </span>
                ))}
              </div>
            </div>

            {/* Special Instructions & ICE Contact */}
            <div className="pt-4 border-t border-slate-100 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
              <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Emergency Contact (ICE)</span>
                <span className="font-bold text-slate-900 text-sm mt-1 block">{profile.emergencyContactName}</span>
                <a
                  href={`tel:${profile.emergencyContactPhone}`}
                  className="text-red-600 font-bold text-sm mt-0.5 inline-flex items-center gap-1 hover:underline"
                >
                  <Phone className="w-3.5 h-3.5" />
                  {profile.emergencyContactPhone}
                </a>
              </div>

              <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Advance Directives</span>
                <p className="text-slate-800 font-medium mt-1 leading-relaxed">
                  {profile.specialInstructions}
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EmergencyLookupPage;

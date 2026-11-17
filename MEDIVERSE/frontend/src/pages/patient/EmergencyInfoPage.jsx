import React, { useState, useEffect } from 'react';
import { AlertTriangle, Droplet, HeartPulse, ShieldAlert, Phone, User, Edit3, Save, CheckCircle } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import patientService from '../../services/patientService';
import Button from '../../components/common/Button';
import FormInput from '../../components/common/FormInput';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const EmergencyInfoPage = () => {
  const { notifySuccess } = useNotification();
  const [profile, setProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const [formData, setFormData] = useState({
    allergies: '',
    criticalConditions: '',
    activeMedications: '',
    specialInstructions: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
  });

  useEffect(() => {
    patientService.getEmergencyProfile().then((data) => {
      setProfile(data);
      setFormData({
        allergies: (data.allergies || []).join(', '),
        criticalConditions: (data.criticalConditions || []).join(', '),
        activeMedications: (data.activeMedications || []).join(', '),
        specialInstructions: data.specialInstructions || '',
        emergencyContactName: data.emergencyContactName || '',
        emergencyContactPhone: data.emergencyContactPhone || '',
      });
      setIsLoading(false);
    });
  }, []);

  const handleSave = (e) => {
    e.preventDefault();
    setProfile((prev) => ({
      ...prev,
      allergies: formData.allergies.split(',').map((s) => s.trim()).filter(Boolean),
      criticalConditions: formData.criticalConditions.split(',').map((s) => s.trim()).filter(Boolean),
      activeMedications: formData.activeMedications.split(',').map((s) => s.trim()).filter(Boolean),
      specialInstructions: formData.specialInstructions,
      emergencyContactName: formData.emergencyContactName,
      emergencyContactPhone: formData.emergencyContactPhone,
    }));
    setIsEditing(false);
    notifySuccess('Emergency triage profile updated.');
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading emergency profile..." />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Emergency Triage (ICE) Profile</h2>
          <p className="text-xs text-slate-500 mt-1">
            Life-critical information instantly made accessible to licensed emergency responders during trauma or unconsciousness
          </p>
        </div>

        <Button
          variant={isEditing ? 'secondary' : 'primary'}
          icon={isEditing ? CheckCircle : Edit3}
          onClick={() => setIsEditing(!isEditing)}
        >
          {isEditing ? 'Cancel Editing' : 'Edit Emergency Profile'}
        </Button>
      </div>

      {/* Warning Box */}
      <div className="p-4 bg-red-50/70 border border-red-200 rounded-2xl flex items-start gap-3 text-xs text-red-950">
        <AlertTriangle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
        <div>
          <h4 className="font-bold text-red-900">Life-Safety Notice</h4>
          <p className="mt-0.5 leading-relaxed text-red-900/80">
            Keep your critical drug allergies and current medications accurate. First responders, EMTs, and ER trauma physicians rely on this dataset to prevent adverse anaphylactic events during emergency intervention.
          </p>
        </div>
      </div>

      {isEditing ? (
        <form onSubmit={handleSave} className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
          <FormInput
            label="Severe Allergies (Comma separated)"
            name="allergies"
            value={formData.allergies}
            onChange={(e) => setFormData((prev) => ({ ...prev, allergies: e.target.value }))}
            helperText="e.g. Penicillin (Anaphylaxis), Peanuts (Severe)"
          />

          <FormInput
            label="Critical Medical Conditions (Comma separated)"
            name="criticalConditions"
            value={formData.criticalConditions}
            onChange={(e) => setFormData((prev) => ({ ...prev, criticalConditions: e.target.value }))}
            helperText="e.g. Type 2 Diabetes, Severe Asthma, Epilepsy"
          />

          <FormInput
            label="Active Emergency Medications (Comma separated)"
            name="activeMedications"
            value={formData.activeMedications}
            onChange={(e) => setFormData((prev) => ({ ...prev, activeMedications: e.target.value }))}
            helperText="e.g. Insulin Glargine 20u nightly, Metformin 500mg BID"
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Primary Emergency Contact Full Name"
              name="emergencyContactName"
              value={formData.emergencyContactName}
              onChange={(e) => setFormData((prev) => ({ ...prev, emergencyContactName: e.target.value }))}
            />
            <FormInput
              label="Primary Emergency Contact Phone"
              name="emergencyContactPhone"
              value={formData.emergencyContactPhone}
              onChange={(e) => setFormData((prev) => ({ ...prev, emergencyContactPhone: e.target.value }))}
            />
          </div>

          <FormInput
            label="Special Emergency Instructions / Advance Directives"
            name="specialInstructions"
            value={formData.specialInstructions}
            onChange={(e) => setFormData((prev) => ({ ...prev, specialInstructions: e.target.value }))}
          />

          <div className="flex justify-end pt-3">
            <Button type="submit" variant="primary" icon={Save}>
              Save Emergency Updates
            </Button>
          </div>
        </form>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="p-4 rounded-xl bg-red-50 border border-red-200">
              <span className="text-[10px] font-bold uppercase tracking-wider text-red-600 block">Blood Group</span>
              <span className="text-xl font-black text-red-900 mt-1 flex items-center gap-1.5">
                <Droplet className="w-5 h-5 text-red-600" />
                {profile?.bloodGroup || 'O+'}
              </span>
            </div>

            <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200">
              <span className="text-[10px] font-bold uppercase tracking-wider text-emerald-600 block">Organ Donor</span>
              <span className="text-base font-bold text-emerald-900 mt-1 block">
                {profile?.organDonor ? 'Registered Donor' : 'Not Registered'}
              </span>
            </div>

            <div className="p-4 rounded-xl bg-slate-50 border border-slate-200">
              <span className="text-[10px] font-bold uppercase tracking-wider text-slate-500 block">DNR Directive</span>
              <span className="text-base font-bold text-slate-900 mt-1 block">
                {profile?.dnrStatus ? 'DNR Enforced' : 'Full Resuscitation'}
              </span>
            </div>
          </div>

          <div className="space-y-4 text-xs">
            <div>
              <h4 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] mb-2 flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-red-500" />
                <span>Documented Severe Allergies</span>
              </h4>
              <div className="flex flex-wrap gap-2">
                {profile?.allergies?.map((alg, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1.5 rounded-lg bg-red-50 border border-red-200 font-semibold text-red-800"
                  >
                    {alg}
                  </span>
                ))}
              </div>
            </div>

            <div>
              <h4 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] mb-2 flex items-center gap-1.5">
                <HeartPulse className="w-4 h-4 text-amber-500" />
                <span>Chronic / Critical Conditions</span>
              </h4>
              <div className="flex flex-wrap gap-2">
                {profile?.criticalConditions?.map((cond, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1.5 rounded-lg bg-amber-50 border border-amber-200 font-semibold text-amber-800"
                  >
                    {cond}
                  </span>
                ))}
              </div>
            </div>

            <div>
              <h4 className="font-bold text-slate-900 uppercase tracking-wider text-[11px] mb-2">
                Active Medications for Responders
              </h4>
              <div className="flex flex-wrap gap-2">
                {profile?.activeMedications?.map((med, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1.5 rounded-lg bg-slate-100 border border-slate-200 font-semibold text-slate-800"
                  >
                    {med}
                  </span>
                ))}
              </div>
            </div>

            <div className="pt-4 border-t border-slate-100 grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <span className="text-slate-400 text-[10px] uppercase block">Emergency ICE Contact</span>
                <span className="text-sm font-bold text-slate-900 block mt-0.5">
                  {profile?.emergencyContactName}
                </span>
                <span className="text-xs text-mediverse-700 font-semibold block mt-0.5">
                  {profile?.emergencyContactPhone}
                </span>
              </div>

              <div>
                <span className="text-slate-400 text-[10px] uppercase block">Special Protocol</span>
                <p className="text-xs text-slate-700 mt-0.5 leading-relaxed">
                  {profile?.specialInstructions}
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EmergencyInfoPage;

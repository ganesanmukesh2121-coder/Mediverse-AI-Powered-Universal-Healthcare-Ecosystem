import React, { useState } from 'react';
import { Award, Building, Clock, Save, User, Stethoscope } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const DoctorSettingsPage = () => {
  const { user } = useAuth();
  const { notifySuccess } = useNotification();

  const [formData, setFormData] = useState({
    licenseNumber: 'MD-CAL-884920',
    specialization: 'Cardiology & Internal Medicine',
    hospitalAffiliation: 'Metro General Hospital / University Medical Center',
    clinicHours: 'Mon - Thu, 09:00 AM - 04:30 PM',
    consultationFee: '150',
  });

  const handleSave = (e) => {
    e.preventDefault();
    notifySuccess('Physician clinical profile updated.');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Physician Practice Settings</h2>
        <p className="text-xs text-slate-500 mt-1">
          Manage your medical license information, clinic hours, and institutional affiliations
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <Stethoscope className="w-4 h-4 text-blue-600" />
          <span>Clinical Practice Profile</span>
        </h3>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Medical License Number"
              name="licenseNumber"
              value={formData.licenseNumber}
              disabled
              helperText="Verified by State Medical Licensing Board"
            />
            <FormInput
              label="Clinical Specialty"
              name="specialization"
              value={formData.specialization}
              onChange={(e) => setFormData((prev) => ({ ...prev, specialization: e.target.value }))}
            />
          </div>

          <FormInput
            label="Primary Hospital / Medical Center Affiliation"
            name="hospitalAffiliation"
            value={formData.hospitalAffiliation}
            onChange={(e) => setFormData((prev) => ({ ...prev, hospitalAffiliation: e.target.value }))}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Clinic Consultation Hours"
              name="clinicHours"
              value={formData.clinicHours}
              onChange={(e) => setFormData((prev) => ({ ...prev, clinicHours: e.target.value }))}
            />
            <FormInput
              label="Standard Consultation Fee (USD)"
              name="consultationFee"
              value={formData.consultationFee}
              onChange={(e) => setFormData((prev) => ({ ...prev, consultationFee: e.target.value }))}
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" icon={Save} className="bg-blue-600 hover:bg-blue-500">
              Save Practice Settings
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DoctorSettingsPage;

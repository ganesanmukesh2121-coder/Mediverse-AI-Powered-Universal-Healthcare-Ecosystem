import React, { useState, useEffect } from 'react';
import { User, Mail, Phone, Calendar, Droplet, MapPin, HeartPulse, Save } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import patientService from '../../services/patientService';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const PatientProfilePage = () => {
  const { user } = useAuth();
  const { notifySuccess } = useNotification();
  const [profile, setProfile] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phoneNumber: '',
    dateOfBirth: '',
    gender: 'FEMALE',
    bloodGroup: 'O_POSITIVE',
    address: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    emergencyRelationship: '',
  });

  useEffect(() => {
    patientService.getProfile().then((data) => {
      setProfile(data);
      setFormData({
        fullName: data.fullName || '',
        email: data.email || '',
        phoneNumber: data.phoneNumber || '',
        dateOfBirth: data.dateOfBirth || '',
        gender: data.gender || 'FEMALE',
        bloodGroup: data.bloodGroup || 'O_POSITIVE',
        address: data.address || '',
        emergencyContactName: data.emergencyContact?.name || '',
        emergencyContactPhone: data.emergencyContact?.phone || '',
        emergencyRelationship: data.emergencyContact?.relationship || '',
      });
      setIsLoading(false);
    });
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      await patientService.updateProfile({
        ...formData,
        emergencyContact: {
          name: formData.emergencyContactName,
          phone: formData.emergencyContactPhone,
          relationship: formData.emergencyRelationship,
        },
      });
      notifySuccess('Patient profile and emergency contact information updated successfully.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading patient profile..." />;
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Patient Clinical Profile</h2>
        <p className="text-xs text-slate-500 mt-1">
          Manage your personal demographics, health identifiers, and emergency ICE contacts
        </p>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        {/* Personal Details Section */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
          <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
            <User className="w-4 h-4 text-mediverse-600" />
            <span>Demographic Information</span>
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Full Legal Name"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              required
            />
            <FormInput
              label="Universal Health ID"
              name="healthId"
              value={profile?.healthId || 'MED-10293847-P'}
              disabled
              helperText="Managed by Mediverse Central Health Authority"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Email Address"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              disabled
              helperText="Primary authentication email cannot be modified directly"
            />
            <FormInput
              label="Mobile Phone Number"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <FormInput
              label="Date of Birth"
              name="dateOfBirth"
              type="date"
              value={formData.dateOfBirth}
              onChange={handleChange}
              required
            />
            <SelectInput
              label="Gender"
              name="gender"
              value={formData.gender}
              onChange={handleChange}
              options={[
                { value: 'MALE', label: 'Male' },
                { value: 'FEMALE', label: 'Female' },
                { value: 'OTHER', label: 'Other' },
              ]}
            />
            <SelectInput
              label="Blood Group"
              name="bloodGroup"
              value={formData.bloodGroup}
              onChange={handleChange}
              options={[
                { value: 'A_POSITIVE', label: 'A+' },
                { value: 'A_NEGATIVE', label: 'A-' },
                { value: 'B_POSITIVE', label: 'B+' },
                { value: 'B_NEGATIVE', label: 'B-' },
                { value: 'AB_POSITIVE', label: 'AB+' },
                { value: 'AB_NEGATIVE', label: 'AB-' },
                { value: 'O_POSITIVE', label: 'O+' },
                { value: 'O_NEGATIVE', label: 'O-' },
              ]}
            />
          </div>

          <FormInput
            label="Home / Residential Address"
            name="address"
            value={formData.address}
            onChange={handleChange}
          />
        </div>

        {/* Emergency ICE Contact Section */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
              <HeartPulse className="w-4 h-4 text-red-500" />
              <span>In Case of Emergency (ICE) Contact</span>
            </h3>
            <span className="text-[11px] font-semibold text-red-600 bg-red-50 border border-red-200 px-2 py-0.5 rounded-full">
              Used in Triage
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <FormInput
              label="Contact Full Name"
              name="emergencyContactName"
              placeholder="e.g. Robert Jenkins"
              value={formData.emergencyContactName}
              onChange={handleChange}
            />
            <FormInput
              label="Relationship"
              name="emergencyRelationship"
              placeholder="e.g. Spouse, Parent, Sibling"
              value={formData.emergencyRelationship}
              onChange={handleChange}
            />
            <FormInput
              label="Emergency Phone"
              name="emergencyContactPhone"
              placeholder="+1 555-019-2835"
              value={formData.emergencyContactPhone}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="flex justify-end gap-3">
          <Button type="submit" variant="primary" icon={Save} isLoading={isSaving}>
            Save Profile Updates
          </Button>
        </div>
      </form>
    </div>
  );
};

export default PatientProfilePage;

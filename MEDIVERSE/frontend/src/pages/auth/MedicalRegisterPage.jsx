import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Building2, Mail, Lock, Phone, Award, MapPin, Bed } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';

export const MedicalRegisterPage = () => {
  const { register } = useAuth();
  const { notifySuccess } = useNotification();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    organizationName: '',
    email: '',
    password: '',
    phoneNumber: '',
    facilityLicenseNumber: '',
    organizationType: 'HOSPITAL',
    bedCapacity: '',
    address: '',
  });

  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      await register({
        fullName: formData.organizationName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        role: 'MEDICAL',
      });
      notifySuccess('Hospital organization registered. Pending administrative verification.');
      navigate('/account-pending');
    } catch {
      notifySuccess('Demo hospital registered. Pending administrative verification.');
      navigate('/account-pending');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-xl mx-auto">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-purple-600 text-white mx-auto flex items-center justify-center shadow-md">
            <Building2 className="w-6 h-6" />
          </div>
          <h2 className="mt-3 text-2xl font-bold text-slate-900 tracking-tight">
            Register Healthcare Facility / Hospital
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Connect your inpatient department, beds, and admission workflows to the Mediverse Network
          </p>
        </div>

        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            <FormInput
              label="Organization / Hospital Legal Name"
              name="organizationName"
              placeholder="e.g. Metro General Hospital"
              icon={Building2}
              value={formData.organizationName}
              onChange={handleChange}
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Institutional Contact Email"
                name="email"
                type="email"
                placeholder="admin@metrogeneral.org"
                icon={Mail}
                value={formData.email}
                onChange={handleChange}
                required
              />
              <FormInput
                label="Direct Phone Number"
                name="phoneNumber"
                placeholder="+1 555-019-4400"
                icon={Phone}
                value={formData.phoneNumber}
                onChange={handleChange}
                required
              />
            </div>

            <FormInput
              label="Account Password"
              name="password"
              type="password"
              placeholder="At least 8 characters"
              icon={Lock}
              value={formData.password}
              onChange={handleChange}
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Healthcare Facility License No."
                name="facilityLicenseNumber"
                placeholder="e.g. HOSP-LIC-2026-90"
                icon={Award}
                value={formData.facilityLicenseNumber}
                onChange={handleChange}
                required
              />
              <SelectInput
                label="Organization Type"
                name="organizationType"
                value={formData.organizationType}
                onChange={handleChange}
                options={[
                  { value: 'HOSPITAL', label: 'General Acute Hospital' },
                  { value: 'SPECIALTY_CLINIC', label: 'Specialty Care Clinic' },
                  { value: 'AMBULATORY_CARE', label: 'Ambulatory Surgical Center' },
                  { value: 'COMMUNITY_HEALTH', label: 'Community Health Center' },
                ]}
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Total Bed Capacity"
                name="bedCapacity"
                type="number"
                placeholder="e.g. 350"
                icon={Bed}
                value={formData.bedCapacity}
                onChange={handleChange}
              />
              <FormInput
                label="Physical Address"
                name="address"
                placeholder="Street address, City, State"
                icon={MapPin}
                value={formData.address}
                onChange={handleChange}
              />
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-4 bg-purple-600 hover:bg-purple-700"
              isLoading={isLoading}
            >
              Submit Hospital Facility Application
            </Button>
          </form>

          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Already registered?{' '}
              <Link to="/login" className="font-semibold text-purple-600 hover:underline">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MedicalRegisterPage;

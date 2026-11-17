import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FlaskConical, Mail, Lock, Phone, Award, ShieldCheck, MapPin } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';

export const LabRegisterPage = () => {
  const { register } = useAuth();
  const { notifySuccess } = useNotification();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    labName: '',
    email: '',
    password: '',
    phoneNumber: '',
    licenseNumber: '',
    accreditationBody: 'CLIA_CAP',
    specialties: '',
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
        fullName: formData.labName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        role: 'LAB',
      });
      notifySuccess('Diagnostic Laboratory registered. Pending administrative verification.');
      navigate('/account-pending');
    } catch {
      notifySuccess('Demo Laboratory registered. Pending administrative verification.');
      navigate('/account-pending');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-xl mx-auto">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-cyan-600 text-white mx-auto flex items-center justify-center shadow-md">
            <FlaskConical className="w-6 h-6" />
          </div>
          <h2 className="mt-3 text-2xl font-bold text-slate-900 tracking-tight">
            Register Diagnostic Laboratory
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Receive test requisitions, process specimens, and publish verified digital lab reports
          </p>
        </div>

        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            <FormInput
              label="Laboratory Name"
              name="labName"
              placeholder="e.g. BioTech Clinical Diagnostics"
              icon={FlaskConical}
              value={formData.labName}
              onChange={handleChange}
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Laboratory Email"
                name="email"
                type="email"
                placeholder="lab@biotechlabs.org"
                icon={Mail}
                value={formData.email}
                onChange={handleChange}
                required
              />
              <FormInput
                label="Telephone Contact"
                name="phoneNumber"
                placeholder="+1 555-019-7700"
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
                label="CLIA / Laboratory License No."
                name="licenseNumber"
                placeholder="e.g. CLIA-88D1092834"
                icon={Award}
                value={formData.licenseNumber}
                onChange={handleChange}
                required
              />
              <SelectInput
                label="Accreditation Body"
                name="accreditationBody"
                value={formData.accreditationBody}
                onChange={handleChange}
                options={[
                  { value: 'CLIA_CAP', label: 'CLIA & CAP Accredited' },
                  { value: 'ISO_15189', label: 'ISO 15189 Medical Lab' },
                  { value: 'COLA', label: 'COLA Laboratory Certification' },
                  { value: 'STATE_CERT', label: 'State Certified Diagnostic Lab' },
                ]}
              />
            </div>

            <FormInput
              label="Primary Diagnostic Specialties"
              name="specialties"
              placeholder="e.g. Clinical Chemistry, Hematology, Molecular Genetics"
              icon={ShieldCheck}
              value={formData.specialties}
              onChange={handleChange}
            />

            <FormInput
              label="Laboratory Physical Address"
              name="address"
              placeholder="Facility address, City, State"
              icon={MapPin}
              value={formData.address}
              onChange={handleChange}
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-4 bg-cyan-600 hover:bg-cyan-700"
              isLoading={isLoading}
            >
              Submit Laboratory Registration
            </Button>
          </form>

          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Already registered?{' '}
              <Link to="/login" className="font-semibold text-cyan-600 hover:underline">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LabRegisterPage;

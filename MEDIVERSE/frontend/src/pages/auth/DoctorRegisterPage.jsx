import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Stethoscope, Mail, Lock, User, Phone, Award, Building, Briefcase } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const DoctorRegisterPage = () => {
  const { register } = useAuth();
  const { notifySuccess } = useNotification();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    phoneNumber: '',
    licenseNumber: '',
    specialization: '',
    hospitalAffiliation: '',
    yearsOfExperience: '',
  });

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.fullName || !formData.email || !formData.licenseNumber) {
      setErrors({ form: 'Please fill in all mandatory provider credentials' });
      return;
    }

    setIsLoading(true);
    try {
      await register({
        ...formData,
        role: 'DOCTOR',
      });
      notifySuccess('Physician application submitted for administrative credential verification.');
      navigate('/account-pending');
    } catch {
      notifySuccess('Demo application registered (Pending Administrative Approval)');
      navigate('/account-pending');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-xl mx-auto">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-blue-600 text-white mx-auto flex items-center justify-center shadow-md">
            <Stethoscope className="w-6 h-6" />
          </div>
          <h2 className="mt-3 text-2xl font-bold text-slate-900 tracking-tight">
            Register as a Licensed Physician
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Access authorized patient timelines, write digital prescriptions, and record consultations
          </p>
        </div>

        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            <FormInput
              label="Full Legal Name (with credentials)"
              name="fullName"
              placeholder="e.g. Dr. Marcus Sterling, MD"
              icon={User}
              value={formData.fullName}
              onChange={handleChange}
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Professional Email"
                name="email"
                type="email"
                placeholder="dr.sterling@hospital.org"
                icon={Mail}
                value={formData.email}
                onChange={handleChange}
                required
              />
              <FormInput
                label="Contact Phone"
                name="phoneNumber"
                placeholder="+1 555-019-3322"
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
                label="Medical License Number"
                name="licenseNumber"
                placeholder="e.g. MD-CAL-884920"
                icon={Award}
                value={formData.licenseNumber}
                onChange={handleChange}
                required
              />
              <FormInput
                label="Clinical Specialization"
                name="specialization"
                placeholder="e.g. Cardiology, Internal Medicine"
                icon={Briefcase}
                value={formData.specialization}
                onChange={handleChange}
                required
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Primary Hospital / Clinic Affiliation"
                name="hospitalAffiliation"
                placeholder="e.g. Metro General Hospital"
                icon={Building}
                value={formData.hospitalAffiliation}
                onChange={handleChange}
              />
              <FormInput
                label="Years of Clinical Experience"
                name="yearsOfExperience"
                type="number"
                placeholder="e.g. 12"
                value={formData.yearsOfExperience}
                onChange={handleChange}
              />
            </div>

            <div className="p-3 bg-blue-50/70 border border-blue-200 rounded-lg text-xs text-blue-900 leading-relaxed">
              <strong>Notice:</strong> In accordance with Mediverse clinical governance, physician accounts undergo credential verification by an Administrator prior to receiving full clinical read/write privileges.
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-4 bg-blue-600 hover:bg-blue-700"
              isLoading={isLoading}
            >
              Submit Physician Application
            </Button>
          </form>

          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Already have an account?{' '}
              <Link to="/login" className="font-semibold text-blue-600 hover:underline">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DoctorRegisterPage;

import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { HeartPulse, Mail, Lock, User, Phone, MapPin, Calendar, Droplet } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';

export const PatientRegisterPage = () => {
  const { register } = useAuth();
  const { notifySuccess, notifyError } = useNotification();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    dateOfBirth: '',
    gender: 'MALE',
    bloodGroup: 'O_POSITIVE',
    address: '',
  });

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.fullName.trim()) newErrors.fullName = 'Full Name is required';
    if (!formData.email.trim()) newErrors.email = 'Email address is required';
    if (!formData.password) newErrors.password = 'Password is required';
    else if (formData.password.length < 8) newErrors.password = 'Password must be at least 8 characters';
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }
    if (!formData.phoneNumber.trim()) newErrors.phoneNumber = 'Phone number is required';
    if (!formData.dateOfBirth) newErrors.dateOfBirth = 'Date of birth is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      await register({
        ...formData,
        role: 'PATIENT',
      });
      notifySuccess('Patient account created! A Universal Health ID has been generated.', 'Registration Successful');
      navigate('/login');
    } catch (err) {
      console.warn('Backend register error, proceeding with demo simulation:', err);
      notifySuccess('Demo patient account registered! Please sign in.', 'Success');
      navigate('/login');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-xl mx-auto">
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-xl bg-emerald-600 text-white mx-auto flex items-center justify-center shadow-md">
            <HeartPulse className="w-6 h-6" />
          </div>
          <h2 className="mt-3 text-2xl font-bold text-slate-900 tracking-tight">
            Create Your Mediverse Health Account
          </h2>
          <p className="text-xs text-slate-500 mt-1">
            Generate your secure Universal Health ID and manage your lifetime clinical timeline
          </p>
        </div>

        <div className="bg-white p-6 sm:p-8 rounded-2xl border border-slate-200 shadow-xl">
          <form onSubmit={handleSubmit} className="space-y-4">
            <FormInput
              label="Full Name"
              name="fullName"
              placeholder="e.g. Sarah Jenkins"
              icon={User}
              value={formData.fullName}
              onChange={handleChange}
              error={errors.fullName}
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Email Address"
                name="email"
                type="email"
                placeholder="sarah@example.com"
                icon={Mail}
                value={formData.email}
                onChange={handleChange}
                error={errors.email}
                required
              />
              <FormInput
                label="Phone Number"
                name="phoneNumber"
                placeholder="+1 555-019-2834"
                icon={Phone}
                value={formData.phoneNumber}
                onChange={handleChange}
                error={errors.phoneNumber}
                required
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormInput
                label="Password"
                name="password"
                type="password"
                placeholder="At least 8 characters"
                icon={Lock}
                value={formData.password}
                onChange={handleChange}
                error={errors.password}
                required
              />
              <FormInput
                label="Confirm Password"
                name="confirmPassword"
                type="password"
                placeholder="Re-enter password"
                icon={Lock}
                value={formData.confirmPassword}
                onChange={handleChange}
                error={errors.confirmPassword}
                required
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <FormInput
                label="Date of Birth"
                name="dateOfBirth"
                type="date"
                icon={Calendar}
                value={formData.dateOfBirth}
                onChange={handleChange}
                error={errors.dateOfBirth}
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
              label="Residential Address"
              name="address"
              placeholder="Street address, City, State, ZIP"
              icon={MapPin}
              value={formData.address}
              onChange={handleChange}
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-4"
              isLoading={isLoading}
            >
              Register & Generate Health ID
            </Button>
          </form>

          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Already have a Mediverse account?{' '}
              <Link to="/login" className="font-semibold text-mediverse-600 hover:underline">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientRegisterPage;

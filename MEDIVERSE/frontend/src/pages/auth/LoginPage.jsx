import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Mail, Lock, ShieldCheck, HeartPulse, Stethoscope, AlertTriangle, Building2, FlaskConical, Shield } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const LoginPage = () => {
  const { login, getPortalHome } = useAuth();
  const { notifySuccess, notifyError } = useNotification();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const from = location.state?.from?.pathname;

  const validate = () => {
    const newErrors = {};
    if (!email) newErrors.email = 'Email address is required';
    else if (!/\S+@\S+\.\S+/.test(email)) newErrors.email = 'Please provide a valid email format';
    if (!password) newErrors.password = 'Password is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e?.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      const user = await login(email, password);
      notifySuccess(`Welcome back, ${user.fullName}!`, 'Authenticated');
      const target = from || getPortalHome(user.role);
      navigate(target, { replace: true });
    } catch (err) {
      console.warn('Real API login failed, checking fallback credentials:', err);
      // If backend is offline or credentials don't match, provide demo fallback
      handleDemoFallback(email);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDemoFallback = (targetEmail) => {
    // Generate fallback demo profile based on role
    let role = 'PATIENT';
    let fullName = 'Sarah Jenkins';
    let healthId = 'MED-10293847-P';

    if (targetEmail.includes('doc')) {
      role = 'DOCTOR';
      fullName = 'Dr. Marcus Sterling, MD';
      healthId = null;
    } else if (targetEmail.includes('emg') || targetEmail.includes('ems')) {
      role = 'EMERGENCY';
      fullName = 'Paramedic Alex Morgan';
      healthId = null;
    } else if (targetEmail.includes('hosp') || targetEmail.includes('med')) {
      role = 'MEDICAL';
      fullName = 'Metro General Hospital Admin';
      healthId = null;
    } else if (targetEmail.includes('lab')) {
      role = 'LAB';
      fullName = 'BioTech Clinical Laboratory';
      healthId = null;
    } else if (targetEmail.includes('admin')) {
      role = 'ADMIN';
      fullName = 'System Security Admin';
      healthId = null;
    }

    const demoUser = {
      userId: `usr-${Date.now()}`,
      email: targetEmail || 'demo.user@mediverse.org',
      fullName,
      role,
      accountStatus: 'ACTIVE',
      healthId,
    };

    localStorage.setItem('mediverse_access_token', 'demo-mock-jwt-token');
    localStorage.setItem('mediverse_user', JSON.stringify(demoUser));
    notifySuccess(`Session started in Interactive Demo Mode as ${role}`);
    window.location.href = getPortalHome(role);
  };

  const selectDemoRole = (roleType, demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setErrors({});
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-mediverse-700 to-mediverse-500 mx-auto flex items-center justify-center text-white text-2xl font-black shadow-lg shadow-mediverse-600/30">
          M
        </div>
        <h2 className="mt-4 text-3xl font-extrabold text-slate-900 tracking-tight">MEDIVERSE</h2>
        <p className="mt-1 text-sm text-slate-600">AI-Powered Universal Healthcare Ecosystem</p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-white py-8 px-6 shadow-xl border border-slate-200/80 rounded-2xl sm:px-10">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <FormInput
              label="Email Address"
              name="email"
              type="email"
              placeholder="name@example.com"
              icon={Mail}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
              required
            />

            <FormInput
              label="Account Password"
              name="password"
              type="password"
              placeholder="••••••••"
              icon={Lock}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              required
            />

            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center text-slate-600">
                <input
                  type="checkbox"
                  defaultChecked
                  className="rounded border-slate-300 text-mediverse-600 focus:ring-mediverse-500 mr-2"
                />
                Remember this device
              </label>
              <Link
                to="/forgot-password"
                className="font-medium text-mediverse-600 hover:text-mediverse-700"
              >
                Forgot password?
              </Link>
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-2"
              isLoading={isLoading}
            >
              Sign In to Portal
            </Button>
          </form>

          {/* Quick Demo Role Fillers for Rapid Testing */}
          <div className="mt-6 pt-5 border-t border-slate-100">
            <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider text-center mb-2.5">
              Quick Role Switcher (One-Click Demo Fill)
            </p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => selectDemoRole('PATIENT', 'sarah.jenkins@example.com', 'Patient@1234')}
                className="flex items-center gap-1.5 p-1.5 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-emerald-50 hover:border-emerald-300 hover:text-emerald-800 transition-colors justify-center"
              >
                <HeartPulse className="w-3.5 h-3.5 text-emerald-500" />
                <span>Patient</span>
              </button>

              <button
                type="button"
                onClick={() => selectDemoRole('DOCTOR', 'dr.sterling@hospital.org', 'Doctor@1234')}
                className="flex items-center gap-1.5 p-1.5 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-blue-50 hover:border-blue-300 hover:text-blue-800 transition-colors justify-center"
              >
                <Stethoscope className="w-3.5 h-3.5 text-blue-500" />
                <span>Doctor</span>
              </button>

              <button
                type="button"
                onClick={() => selectDemoRole('EMERGENCY', 'ems.alex@paramedic.org', 'Emergency@1234')}
                className="flex items-center gap-1.5 p-1.5 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-red-50 hover:border-red-300 hover:text-red-800 transition-colors justify-center"
              >
                <AlertTriangle className="w-3.5 h-3.5 text-red-500" />
                <span>Emergency</span>
              </button>

              <button
                type="button"
                onClick={() => selectDemoRole('MEDICAL', 'admin@metrogeneral.org', 'Medical@1234')}
                className="flex items-center gap-1.5 p-1.5 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-purple-50 hover:border-purple-300 hover:text-purple-800 transition-colors justify-center"
              >
                <Building2 className="w-3.5 h-3.5 text-purple-500" />
                <span>Medical Org</span>
              </button>

              <button
                type="button"
                onClick={() => selectDemoRole('LAB', 'lab@biotechlabs.org', 'Lab@1234')}
                className="flex items-center gap-1.5 p-1.5 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-cyan-50 hover:border-cyan-300 hover:text-cyan-800 transition-colors justify-center"
              >
                <FlaskConical className="w-3.5 h-3.5 text-cyan-500" />
                <span>Laboratory</span>
              </button>

              <button
                type="button"
                onClick={() => selectDemoRole('ADMIN', 'admin@mediverse.org', 'Admin@1234')}
                className="flex items-center gap-1.5 p-1.5 rounded-lg border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-indigo-50 hover:border-indigo-300 hover:text-indigo-800 transition-colors justify-center"
              >
                <Shield className="w-3.5 h-3.5 text-indigo-500" />
                <span>Admin</span>
              </button>
            </div>
          </div>

          {/* Registration Links */}
          <div className="mt-6 pt-5 border-t border-slate-100 text-center">
            <p className="text-xs text-slate-500">
              Need a new Mediverse account?
            </p>
            <div className="mt-2 flex flex-wrap items-center justify-center gap-2 text-xs font-semibold text-mediverse-600">
              <Link to="/register/patient" className="hover:underline">
                Register as Patient
              </Link>
              <span className="text-slate-300">•</span>
              <Link to="/register/doctor" className="hover:underline">
                Doctor
              </Link>
              <span className="text-slate-300">•</span>
              <Link to="/register/medical" className="hover:underline">
                Hospital
              </Link>
              <span className="text-slate-300">•</span>
              <Link to="/register/lab" className="hover:underline">
                Lab
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

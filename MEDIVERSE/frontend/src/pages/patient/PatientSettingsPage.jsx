import React, { useState } from 'react';
import { Lock, Bell, Shield, Save, CheckCircle2 } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const PatientSettingsPage = () => {
  const { notifySuccess } = useNotification();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isUpdatingPass, setIsUpdatingPass] = useState(false);

  const [notifications, setNotifications] = useState({
    smsAppointments: true,
    emailLabResults: true,
    consentAlerts: true,
    emergencyAccessAlerts: true,
  });

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      alert('Password must be at least 8 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      alert('Passwords do not match');
      return;
    }
    setIsUpdatingPass(true);
    setTimeout(() => {
      setIsUpdatingPass(false);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      notifySuccess('Account password updated securely.');
    }, 600);
  };

  const handleToggle = (key) => {
    setNotifications((prev) => {
      const next = { ...prev, [key]: !prev[key] };
      notifySuccess('Notification preferences saved.');
      return next;
    });
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Patient Portal Settings</h2>
        <p className="text-xs text-slate-500 mt-1">
          Configure authentication credentials, security notifications, and data privacy alerts
        </p>
      </div>

      {/* Security Credentials */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <Lock className="w-4 h-4 text-mediverse-600" />
          <span>Change Account Password</span>
        </h3>

        <form onSubmit={handlePasswordSubmit} className="space-y-4">
          <FormInput
            label="Current Password"
            name="currentPassword"
            type="password"
            placeholder="••••••••"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="New Password"
              name="newPassword"
              type="password"
              placeholder="At least 8 characters"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <FormInput
              label="Confirm New Password"
              name="confirmPassword"
              type="password"
              placeholder="Re-enter new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" isLoading={isUpdatingPass}>
              Update Password
            </Button>
          </div>
        </form>
      </div>

      {/* Notification Preferences */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <Bell className="w-4 h-4 text-mediverse-600" />
          <span>Security & Clinical Notifications</span>
        </h3>

        <div className="divide-y divide-slate-100 text-xs">
          <div className="py-3 flex items-center justify-between">
            <div>
              <p className="font-semibold text-slate-800">Emergency ICE Access Alerts</p>
              <p className="text-slate-500">Receive immediate SMS when a first responder accesses your triage data</p>
            </div>
            <input
              type="checkbox"
              checked={notifications.emergencyAccessAlerts}
              onChange={() => handleToggle('emergencyAccessAlerts')}
              className="w-4 h-4 text-mediverse-600 rounded border-slate-300 focus:ring-mediverse-500 cursor-pointer"
            />
          </div>

          <div className="py-3 flex items-center justify-between">
            <div>
              <p className="font-semibold text-slate-800">Provider Consent Request Notifications</p>
              <p className="text-slate-500">Get notified when an authorized physician requests record renewal</p>
            </div>
            <input
              type="checkbox"
              checked={notifications.consentAlerts}
              onChange={() => handleToggle('consentAlerts')}
              className="w-4 h-4 text-mediverse-600 rounded border-slate-300 focus:ring-mediverse-500 cursor-pointer"
            />
          </div>

          <div className="py-3 flex items-center justify-between">
            <div>
              <p className="font-semibold text-slate-800">Diagnostic Lab Report Ready Alerts</p>
              <p className="text-slate-500">Receive alert when certified laboratory tests are finalized</p>
            </div>
            <input
              type="checkbox"
              checked={notifications.emailLabResults}
              onChange={() => handleToggle('emailLabResults')}
              className="w-4 h-4 text-mediverse-600 rounded border-slate-300 focus:ring-mediverse-500 cursor-pointer"
            />
          </div>

          <div className="py-3 flex items-center justify-between">
            <div>
              <p className="font-semibold text-slate-800">Appointment Reminders</p>
              <p className="text-slate-500">SMS alert 24 hours prior to scheduled clinic consultations</p>
            </div>
            <input
              type="checkbox"
              checked={notifications.smsAppointments}
              onChange={() => handleToggle('smsAppointments')}
              className="w-4 h-4 text-mediverse-600 rounded border-slate-300 focus:ring-mediverse-500 cursor-pointer"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientSettingsPage;

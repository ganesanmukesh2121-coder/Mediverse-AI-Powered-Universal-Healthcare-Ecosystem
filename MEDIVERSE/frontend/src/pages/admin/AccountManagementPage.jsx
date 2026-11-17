import React, { useState } from 'react';
import { KeyRound, Shield, Clock, Lock, Save, CheckCircle2 } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const AccountManagementPage = () => {
  const { notifySuccess } = useNotification();
  const [policies, setPolicies] = useState({
    accessTokenLifetimeMin: '15',
    refreshTokenLifetimeDays: '7',
    failedAttemptLockout: '5',
    lockoutDurationMinutes: '30',
    idleSessionTimeoutMin: '15',
    requireMfaForProviders: true,
  });

  const handleSave = (e) => {
    e.preventDefault();
    notifySuccess('System security & session policies updated successfully.');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Security & Access Management</h2>
        <p className="text-xs text-slate-500 mt-1">
          Configure authentication token rotations, idle timeouts, and cryptographic lockout parameters
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <KeyRound className="w-4 h-4 text-indigo-600" />
          <span>Dual-Token JWT & Session Policies</span>
        </h3>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Access Token Expiry (Minutes)"
              name="accessTokenLifetimeMin"
              type="number"
              value={policies.accessTokenLifetimeMin}
              onChange={(e) => setPolicies((prev) => ({ ...prev, accessTokenLifetimeMin: e.target.value }))}
              helperText="Enforced short-lived JWT token"
            />
            <FormInput
              label="Refresh Token Expiry (Days)"
              name="refreshTokenLifetimeDays"
              type="number"
              value={policies.refreshTokenLifetimeDays}
              onChange={(e) => setPolicies((prev) => ({ ...prev, refreshTokenLifetimeDays: e.target.value }))}
              helperText="Stored securely in encrypted vault"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Failed Login Lockout Threshold"
              name="failedAttemptLockout"
              type="number"
              value={policies.failedAttemptLockout}
              onChange={(e) => setPolicies((prev) => ({ ...prev, failedAttemptLockout: e.target.value }))}
              helperText="Consecutive invalid password attempts"
            />
            <FormInput
              label="Lockout Duration (Minutes)"
              name="lockoutDurationMinutes"
              type="number"
              value={policies.lockoutDurationMinutes}
              onChange={(e) => setPolicies((prev) => ({ ...prev, lockoutDurationMinutes: e.target.value }))}
            />
          </div>

          <div className="p-3.5 bg-indigo-50/60 rounded-xl border border-indigo-200 text-xs text-indigo-950 flex items-center justify-between">
            <div>
              <span className="font-bold block">HIPAA Mandatory Provider MFA</span>
              <span className="text-indigo-900/80">Require multi-factor authentication for Doctor, Medical, and Lab roles</span>
            </div>
            <input
              type="checkbox"
              checked={policies.requireMfaForProviders}
              onChange={(e) => setPolicies((prev) => ({ ...prev, requireMfaForProviders: e.target.checked }))}
              className="w-4 h-4 text-indigo-600 rounded border-slate-300"
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" icon={Save} className="bg-indigo-600 hover:bg-indigo-500">
              Apply Security Policies
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AccountManagementPage;

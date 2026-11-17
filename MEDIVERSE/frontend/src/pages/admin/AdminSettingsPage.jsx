import React, { useState } from 'react';
import { Settings, Server, Shield, Save, Database, HardDrive } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const AdminSettingsPage = () => {
  const { notifySuccess } = useNotification();
  const [systemConfig, setSystemConfig] = useState({
    environment: 'PRODUCTION (HIGH-TRUST)',
    systemVersion: 'MEDIVERSE v2.4-RELEASE',
    backendApiBase: 'http://localhost:8080',
    maintenanceMode: false,
    auditRetentionDays: '2555', // 7 years for HIPAA
  });

  const handleSave = (e) => {
    e.preventDefault();
    notifySuccess('Global platform settings updated.');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">System Environment Configuration</h2>
        <p className="text-xs text-slate-500 mt-1">
          Global architecture parameters, audit archival retention, and maintenance state
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <Server className="w-4 h-4 text-indigo-600" />
          <span>Core System Metadata</span>
        </h3>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Operating Environment"
              name="environment"
              value={systemConfig.environment}
              disabled
            />
            <FormInput
              label="Mediverse Platform Release"
              name="systemVersion"
              value={systemConfig.systemVersion}
              disabled
            />
          </div>

          <FormInput
            label="Backend Core API Gateway URI"
            name="backendApiBase"
            value={systemConfig.backendApiBase}
            onChange={(e) => setSystemConfig((prev) => ({ ...prev, backendApiBase: e.target.value }))}
          />

          <FormInput
            label="Audit Trail Retention (Days — HIPAA standard: 2555 days)"
            name="auditRetentionDays"
            value={systemConfig.auditRetentionDays}
            onChange={(e) => setSystemConfig((prev) => ({ ...prev, auditRetentionDays: e.target.value }))}
          />

          <div className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between text-xs">
            <div>
              <span className="font-bold text-slate-800 block">System Maintenance Mode</span>
              <span className="text-slate-500">Temporarily restrict non-admin access for scheduled cluster upgrades</span>
            </div>
            <input
              type="checkbox"
              checked={systemConfig.maintenanceMode}
              onChange={(e) => setSystemConfig((prev) => ({ ...prev, maintenanceMode: e.target.checked }))}
              className="w-4 h-4 text-indigo-600 rounded border-slate-300"
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" icon={Save} className="bg-indigo-600 hover:bg-indigo-500">
              Save Platform Configuration
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AdminSettingsPage;

import React, { useState } from 'react';
import { ShieldCheck, User, Radio, Award, Phone, Save } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const EmergencySettingsPage = () => {
  const { user } = useAuth();
  const { notifySuccess } = useNotification();

  const [stationInfo, setStationInfo] = useState({
    responderUnit: 'EMS Mobile Unit 7',
    stationId: 'STA-METRO-04',
    dispatchRadioId: 'FREQ-911-TX-04',
    supervisorContact: '+1-555-911-0022',
  });

  const handleSave = (e) => {
    e.preventDefault();
    notifySuccess('Responder unit configuration updated.');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Responder Station Settings</h2>
        <p className="text-xs text-slate-500 mt-1">
          Authorized telemetry station identifier and emergency credentials
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <Radio className="w-4 h-4 text-red-600" />
          <span>Dispatch & Station Telemetry</span>
        </h3>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Assigned Responder Unit"
              name="responderUnit"
              value={stationInfo.responderUnit}
              onChange={(e) => setStationInfo((prev) => ({ ...prev, responderUnit: e.target.value }))}
            />
            <FormInput
              label="Station Base Identifier"
              name="stationId"
              value={stationInfo.stationId}
              onChange={(e) => setStationInfo((prev) => ({ ...prev, stationId: e.target.value }))}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Dispatch Frequency Channel"
              name="dispatchRadioId"
              value={stationInfo.dispatchRadioId}
              onChange={(e) => setStationInfo((prev) => ({ ...prev, dispatchRadioId: e.target.value }))}
            />
            <FormInput
              label="Supervisor Emergency Line"
              name="supervisorContact"
              value={stationInfo.supervisorContact}
              onChange={(e) => setStationInfo((prev) => ({ ...prev, supervisorContact: e.target.value }))}
            />
          </div>

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" icon={Save}>
              Save Station Configuration
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EmergencySettingsPage;

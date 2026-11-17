import React, { useState } from 'react';
import { Building2, Bed, Save, Phone, MapPin, Award } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const MedicalSettingsPage = () => {
  const { notifySuccess } = useNotification();
  const [facilityInfo, setFacilityInfo] = useState({
    name: 'Metro General Hospital',
    license: 'HOSP-TX-44021',
    bedCapacity: '350',
    emergencyBeds: '30',
    chiefMedicalOfficer: 'Dr. Arthur Sterling, MD',
    phone: '+1-555-019-4400',
    address: '100 Medical Parkway, Austin, TX',
  });

  const handleSave = (e) => {
    e.preventDefault();
    notifySuccess('Hospital facility parameters updated.');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Hospital Facility Settings</h2>
        <p className="text-xs text-slate-500 mt-1">
          Configure inpatient facility bed thresholds, licensing credentials, and contact channels
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <Building2 className="w-4 h-4 text-purple-600" />
          <span>Facility Profile & Bed Inventory</span>
        </h3>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Hospital Name"
              name="name"
              value={facilityInfo.name}
              onChange={(e) => setFacilityInfo((prev) => ({ ...prev, name: e.target.value }))}
            />
            <FormInput
              label="State Healthcare License"
              name="license"
              value={facilityInfo.license}
              disabled
              helperText="Verified State License"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Total Licensed Beds"
              name="bedCapacity"
              value={facilityInfo.bedCapacity}
              onChange={(e) => setFacilityInfo((prev) => ({ ...prev, bedCapacity: e.target.value }))}
            />
            <FormInput
              label="Emergency Trauma Bay Beds"
              name="emergencyBeds"
              value={facilityInfo.emergencyBeds}
              onChange={(e) => setFacilityInfo((prev) => ({ ...prev, emergencyBeds: e.target.value }))}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Chief Medical Officer"
              name="cmo"
              value={facilityInfo.chiefMedicalOfficer}
              onChange={(e) => setFacilityInfo((prev) => ({ ...prev, chiefMedicalOfficer: e.target.value }))}
            />
            <FormInput
              label="Administrative Telephone"
              name="phone"
              value={facilityInfo.phone}
              onChange={(e) => setFacilityInfo((prev) => ({ ...prev, phone: e.target.value }))}
            />
          </div>

          <FormInput
            label="Physical Campus Address"
            name="address"
            value={facilityInfo.address}
            onChange={(e) => setFacilityInfo((prev) => ({ ...prev, address: e.target.value }))}
          />

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" icon={Save} className="bg-purple-600 hover:bg-purple-500">
              Save Facility Profile
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default MedicalSettingsPage;

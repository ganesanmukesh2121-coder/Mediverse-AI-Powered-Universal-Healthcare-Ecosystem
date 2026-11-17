import React, { useState } from 'react';
import { FlaskConical, Award, Save, CheckCircle2 } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const LabSettingsPage = () => {
  const { notifySuccess } = useNotification();
  const [labConfig, setLabConfig] = useState({
    labName: 'BioTech Clinical Diagnostics Lab #4',
    cliaNumber: 'CLIA-88D1092834',
    directorName: 'Dr. Gregory Finch, MD, FCAP',
    accreditation: 'CAP / CLIA Certified Diagnostic Facility',
    lisEndpoint: 'https://lis-api.biotechlabs.org/hl7/v2',
  });

  const handleSave = (e) => {
    e.preventDefault();
    notifySuccess('Laboratory configuration saved.');
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Diagnostic Laboratory Settings</h2>
        <p className="text-xs text-slate-500 mt-1">
          Accreditation records, laboratory director credentials, and HL7 / FHIR data connectivity
        </p>
      </div>

      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-4">
        <h3 className="text-base font-bold text-slate-900 flex items-center gap-2">
          <FlaskConical className="w-4 h-4 text-cyan-600" />
          <span>Accreditation & CLIA Specifications</span>
        </h3>

        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="CLIA Certificate Number"
              name="cliaNumber"
              value={labConfig.cliaNumber}
              disabled
              helperText="Verified Federal Accreditation"
            />
            <FormInput
              label="Laboratory Medical Director"
              name="directorName"
              value={labConfig.directorName}
              onChange={(e) => setLabConfig((prev) => ({ ...prev, directorName: e.target.value }))}
            />
          </div>

          <FormInput
            label="Accreditation Authority"
            name="accreditation"
            value={labConfig.accreditation}
            onChange={(e) => setLabConfig((prev) => ({ ...prev, accreditation: e.target.value }))}
          />

          <FormInput
            label="HL7 / LIS Ingestion API Endpoint"
            name="lisEndpoint"
            value={labConfig.lisEndpoint}
            onChange={(e) => setLabConfig((prev) => ({ ...prev, lisEndpoint: e.target.value }))}
          />

          <div className="flex justify-end pt-2">
            <Button type="submit" variant="primary" icon={Save} className="bg-cyan-600 hover:bg-cyan-500">
              Save Lab Configuration
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LabSettingsPage;

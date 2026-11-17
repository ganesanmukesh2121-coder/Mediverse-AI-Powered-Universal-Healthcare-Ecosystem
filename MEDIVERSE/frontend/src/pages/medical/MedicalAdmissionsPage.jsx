import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Bed, Plus, User, Stethoscope, Search, CheckCircle2, Clock } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import medicalOrgService from '../../services/medicalOrgService';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const MedicalAdmissionsPage = () => {
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotification();
  const [admissions, setAdmissions] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [formData, setFormData] = useState({
    patientName: searchParams.get('name') || '',
    healthId: searchParams.get('healthId') || '',
    department: 'Pulmonology',
    ward: 'Ward 4B (Respiratory)',
    bedNumber: 'Bed 415',
    admittingDoctor: 'Dr. Angela Zhao, MD',
    diagnosis: '',
    dietType: 'Standard Regular',
    isolationPrecautions: 'None',
  });

  useEffect(() => {
    medicalOrgService.getAdmissions().then((data) => {
      setAdmissions(data);
      setIsLoading(false);
    });
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.patientName || !formData.healthId || !formData.diagnosis) {
      notifyError('Please fill in patient name, health ID, and initial diagnosis.');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await medicalOrgService.createAdmission(formData);
      setAdmissions((prev) => [created, ...prev]);
      notifySuccess(`Patient ${formData.patientName} admitted to ${formData.ward}.`);
      setIsModalOpen(false);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading inpatient admissions..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Inpatient Admissions Desk</h2>
            <DemoDataBadge label="Inpatient Workflow Preview" />
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Manage bed allocations, ward assignments, and admission documentation
          </p>
        </div>

        <Button
          variant="primary"
          icon={Plus}
          onClick={() => setIsModalOpen(true)}
          className="bg-purple-600 hover:bg-purple-500"
        >
          Admit Patient
        </Button>
      </div>

      <div className="space-y-3">
        {admissions.map((adm) => (
          <div
            key={adm.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shrink-0 border border-purple-100">
                <Bed className="w-6 h-6" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{adm.patientName}</h4>
                  <span className="font-mono text-xs font-bold text-purple-700 bg-purple-50 px-2 py-0.5 rounded border border-purple-200">
                    {adm.healthId}
                  </span>
                  <StatusBadge status={adm.status} size="sm" />
                </div>
                <p className="text-xs text-slate-600 mt-0.5">
                  Diagnosis: <strong className="text-slate-800">{adm.diagnosis}</strong>
                </p>
                <div className="flex flex-wrap items-center gap-4 mt-2 text-xs text-slate-500">
                  <span>
                    Location: <strong>{adm.ward} ({adm.bedNumber})</strong>
                  </span>
                  <span>Admitting Physician: <strong>{adm.admittingDoctor}</strong></span>
                  <span>Date: <strong>{new Date(adm.admissionDate).toLocaleDateString()}</strong></span>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Admission Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Admit Patient to Hospital Bed"
        subtitle="Record inpatient admission and assign clinical ward"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Patient Full Legal Name"
              name="patientName"
              value={formData.patientName}
              onChange={handleChange}
              required
            />
            <FormInput
              label="Universal Health ID"
              name="healthId"
              value={formData.healthId}
              onChange={handleChange}
              required
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Clinical Ward"
              name="ward"
              value={formData.ward}
              onChange={handleChange}
            />
            <FormInput
              label="Bed Number"
              name="bedNumber"
              value={formData.bedNumber}
              onChange={handleChange}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Admitting Physician"
              name="admittingDoctor"
              value={formData.admittingDoctor}
              onChange={handleChange}
            />
            <SelectInput
              label="Department"
              name="department"
              value={formData.department}
              onChange={handleChange}
              options={[
                { value: 'Pulmonology', label: 'Pulmonology' },
                { value: 'Cardiology', label: 'Cardiology' },
                { value: 'Hematology', label: 'Hematology' },
                { value: 'Orthopedics', label: 'Orthopedics' },
                { value: 'IntensiveCare', label: 'Intensive Care Unit (ICU)' },
              ]}
            />
          </div>

          <FormInput
            label="Admission Diagnosis / Condition"
            name="diagnosis"
            placeholder="e.g. Acute Respiratory Distress, Post-Operative Monitoring"
            value={formData.diagnosis}
            onChange={handleChange}
            required
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-slate-100">
            <Button variant="secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={isSubmitting} className="bg-purple-600 hover:bg-purple-500">
              Confirm Inpatient Admission
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default MedicalAdmissionsPage;

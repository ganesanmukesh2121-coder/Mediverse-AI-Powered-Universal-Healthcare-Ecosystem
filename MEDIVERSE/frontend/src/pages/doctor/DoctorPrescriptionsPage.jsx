import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Pill, Plus, Trash2, CheckCircle2, Save, Printer, User } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import doctorService from '../../services/doctorService';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';

export const DoctorPrescriptionsPage = () => {
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotification();

  const [patientHealthId, setPatientHealthId] = useState(searchParams.get('healthId') || 'MED-10293847-P');
  const [patientName, setPatientName] = useState(searchParams.get('patientName') || 'Sarah Jenkins');

  const [items, setItems] = useState([
    {
      medicationName: 'Amoxicillin Trihydrate',
      dosage: '500 mg',
      frequency: 'Three times daily',
      route: 'Oral',
      duration: '7 Days',
      refillsRemaining: 0,
      instructions: 'Take with food every 8 hours until complete course is finished.',
    },
  ]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [issuedList, setIssuedList] = useState([]);

  const handleAddItem = () => {
    setItems((prev) => [
      ...prev,
      {
        medicationName: '',
        dosage: '',
        frequency: 'Once Daily',
        route: 'Oral',
        duration: '30 Days',
        refillsRemaining: 1,
        instructions: '',
      },
    ]);
  };

  const handleRemoveItem = (index) => {
    if (items.length === 1) return;
    setItems((prev) => prev.filter((_, i) => i !== index));
  };

  const handleItemChange = (index, field, value) => {
    setItems((prev) => {
      const copy = [...prev];
      copy[index][field] = value;
      return copy;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!patientHealthId.trim()) {
      notifyError('Patient Health ID is required.');
      return;
    }
    const invalid = items.some((it) => !it.medicationName.trim() || !it.dosage.trim());
    if (invalid) {
      notifyError('Please fill in drug name and dosage for all prescription items.');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await doctorService.createPrescription({
        healthId: patientHealthId,
        patientName,
        items,
      });
      setIssuedList((prev) => [created, ...prev]);
      notifySuccess('Digital e-Prescription signed and transmitted to pharmacy network.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Prescription Desk</h2>
        <p className="text-xs text-slate-500 mt-1">
          Issue verified digital e-Prescriptions directly bound to the patient's Universal Health ID
        </p>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-6">
        {/* Patient Selection Row */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-4 border-b border-slate-100">
          <FormInput
            label="Patient Universal Health ID"
            name="patientHealthId"
            value={patientHealthId}
            onChange={(e) => setPatientHealthId(e.target.value)}
            required
          />
          <FormInput
            label="Patient Full Legal Name"
            name="patientName"
            value={patientName}
            onChange={(e) => setPatientName(e.target.value)}
            required
          />
        </div>

        {/* Prescription Items */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
              <Pill className="w-4 h-4 text-emerald-600" />
              <span>Prescription Drugs & Regimens</span>
            </h4>
            <Button variant="outline" size="sm" icon={Plus} onClick={handleAddItem}>
              Add Drug Item
            </Button>
          </div>

          {items.map((item, idx) => (
            <div key={idx} className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-3 relative">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-700">Medication Item #{idx + 1}</span>
                {items.length > 1 && (
                  <button
                    type="button"
                    onClick={() => handleRemoveItem(idx)}
                    className="text-slate-400 hover:text-red-600 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <FormInput
                  label="Generic / Brand Drug Name"
                  name={`drug_${idx}`}
                  placeholder="e.g. Lisinopril, Metformin"
                  value={item.medicationName}
                  onChange={(e) => handleItemChange(idx, 'medicationName', e.target.value)}
                  required
                />
                <FormInput
                  label="Dosage Strength"
                  name={`dosage_${idx}`}
                  placeholder="e.g. 10 mg, 500 mg"
                  value={item.dosage}
                  onChange={(e) => handleItemChange(idx, 'dosage', e.target.value)}
                  required
                />
                <FormInput
                  label="Dosing Frequency"
                  name={`freq_${idx}`}
                  placeholder="e.g. Once daily, Twice daily"
                  value={item.frequency}
                  onChange={(e) => handleItemChange(idx, 'frequency', e.target.value)}
                  required
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <SelectInput
                  label="Route of Administration"
                  name={`route_${idx}`}
                  value={item.route}
                  onChange={(e) => handleItemChange(idx, 'route', e.target.value)}
                  options={[
                    { value: 'Oral', label: 'Oral' },
                    { value: 'Inhalation', label: 'Inhalation' },
                    { value: 'Topical', label: 'Topical' },
                    { value: 'Sublingual', label: 'Sublingual' },
                    { value: 'Subcutaneous', label: 'Subcutaneous Injection' },
                  ]}
                />
                <FormInput
                  label="Duration of Therapy"
                  name={`duration_${idx}`}
                  placeholder="e.g. 7 Days, 30 Days"
                  value={item.duration}
                  onChange={(e) => handleItemChange(idx, 'duration', e.target.value)}
                />
                <FormInput
                  label="Authorized Refills"
                  name={`refills_${idx}`}
                  type="number"
                  placeholder="0"
                  value={item.refillsRemaining}
                  onChange={(e) => handleItemChange(idx, 'refillsRemaining', e.target.value)}
                />
              </div>

              <FormInput
                label="Specific Patient Instructions / Cautions"
                name={`inst_${idx}`}
                placeholder="Take in morning with full glass of water. Avoid potassium salts."
                value={item.instructions}
                onChange={(e) => handleItemChange(idx, 'instructions', e.target.value)}
              />
            </div>
          ))}
        </div>

        <div className="flex justify-end pt-3 border-t border-slate-100">
          <Button
            type="submit"
            variant="primary"
            icon={Save}
            size="lg"
            isLoading={isSubmitting}
            className="bg-emerald-600 hover:bg-emerald-500"
          >
            Digitally Sign & Transmit e-Prescription
          </Button>
        </div>
      </form>
    </div>
  );
};

export default DoctorPrescriptionsPage;

import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { FlaskConical, Plus, Trash2, CheckCircle2, Save, FileCheck2, Printer } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import labService from '../../services/labService';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';

export const LabReportsPage = () => {
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotification();

  const [orderId, setOrderId] = useState(searchParams.get('orderId') || 'ORD-99120');
  const [patientHealthId, setPatientHealthId] = useState(searchParams.get('healthId') || 'MED-10293847-P');
  const [testName, setTestName] = useState(searchParams.get('testName') || 'Comprehensive Chemistry Panel');
  const [orderingDoctor, setOrderingDoctor] = useState('Dr. Marcus Sterling, MD');
  const [interpretation, setInterpretation] = useState('All biometric parameters calibrated and verified against CLIA reference ranges.');

  const [parameters, setParameters] = useState([
    { parameter: 'Serum Glucose', value: '94', unit: 'mg/dL', referenceRange: '70 - 99', flag: 'NORMAL' },
    { parameter: 'Serum Creatinine', value: '0.85', unit: 'mg/dL', referenceRange: '0.50 - 1.10', flag: 'NORMAL' },
    { parameter: 'Blood Urea Nitrogen (BUN)', value: '14', unit: 'mg/dL', referenceRange: '7 - 20', flag: 'NORMAL' },
  ]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [published, setPublished] = useState(false);

  const handleAddParam = () => {
    setParameters((prev) => [
      ...prev,
      { parameter: '', value: '', unit: 'mg/dL', referenceRange: '', flag: 'NORMAL' },
    ]);
  };

  const handleRemoveParam = (index) => {
    if (parameters.length === 1) return;
    setParameters((prev) => prev.filter((_, i) => i !== index));
  };

  const handleParamChange = (index, field, val) => {
    setParameters((prev) => {
      const copy = [...prev];
      copy[index][field] = val;
      return copy;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!patientHealthId.trim() || !testName.trim()) {
      notifyError('Please specify patient health ID and test title.');
      return;
    }

    setIsSubmitting(true);
    try {
      await labService.uploadLabReport({
        orderId,
        healthId: patientHealthId,
        testName,
        orderingDoctor,
        clinicalInterpretation: interpretation,
        results: parameters,
      });
      setPublished(true);
      notifySuccess('Official laboratory diagnostic report certified and published to the patient timeline.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Diagnostic Report Certification Desk</h2>
          <p className="text-xs text-slate-500 mt-1">
            Input verified numerical parameters, clinical pathologist notes, and publish official diagnostic reports
          </p>
        </div>
        <DemoDataBadge label="LIS Processing Preview" />
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-6">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pb-4 border-b border-slate-100">
          <FormInput
            label="Requisition Order Number"
            name="orderId"
            value={orderId}
            onChange={(e) => setOrderId(e.target.value)}
            required
          />
          <FormInput
            label="Patient Universal Health ID"
            name="healthId"
            value={patientHealthId}
            onChange={(e) => setPatientHealthId(e.target.value)}
            required
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <FormInput
            label="Diagnostic Test Panel Name"
            name="testName"
            value={testName}
            onChange={(e) => setTestName(e.target.value)}
            required
          />
          <FormInput
            label="Ordering Physician"
            name="orderingDoctor"
            value={orderingDoctor}
            onChange={(e) => setOrderingDoctor(e.target.value)}
          />
        </div>

        {/* Dynamic Parameter Rows */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wider flex items-center gap-1.5">
              <FlaskConical className="w-4 h-4 text-cyan-600" />
              <span>Tested Quantitative Biomarkers</span>
            </h4>
            <Button variant="outline" size="sm" icon={Plus} onClick={handleAddParam}>
              Add Biomarker Row
            </Button>
          </div>

          <div className="space-y-3">
            {parameters.map((p, idx) => (
              <div key={idx} className="p-3.5 bg-slate-50 rounded-xl border border-slate-200 grid grid-cols-1 sm:grid-cols-12 gap-3 items-center text-xs">
                <div className="sm:col-span-4">
                  <FormInput
                    label="Parameter"
                    name={`param_${idx}`}
                    placeholder="e.g. Total Cholesterol"
                    value={p.parameter}
                    onChange={(e) => handleParamChange(idx, 'parameter', e.target.value)}
                    required
                  />
                </div>
                <div className="sm:col-span-2">
                  <FormInput
                    label="Measured"
                    name={`val_${idx}`}
                    placeholder="195"
                    value={p.value}
                    onChange={(e) => handleParamChange(idx, 'value', e.target.value)}
                    required
                  />
                </div>
                <div className="sm:col-span-2">
                  <FormInput
                    label="Unit"
                    name={`unit_${idx}`}
                    placeholder="mg/dL"
                    value={p.unit}
                    onChange={(e) => handleParamChange(idx, 'unit', e.target.value)}
                  />
                </div>
                <div className="sm:col-span-2">
                  <FormInput
                    label="Ref. Range"
                    name={`ref_${idx}`}
                    placeholder="< 200"
                    value={p.referenceRange}
                    onChange={(e) => handleParamChange(idx, 'referenceRange', e.target.value)}
                  />
                </div>
                <div className="sm:col-span-2 flex items-center gap-2 pt-4">
                  <SelectInput
                    label="Flag"
                    name={`flag_${idx}`}
                    value={p.flag}
                    onChange={(e) => handleParamChange(idx, 'flag', e.target.value)}
                    options={[
                      { value: 'NORMAL', label: 'Normal' },
                      { value: 'HIGH', label: 'High' },
                      { value: 'LOW', label: 'Low' },
                    ]}
                  />
                  {parameters.length > 1 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveParam(idx)}
                      className="text-slate-400 hover:text-red-600 transition-colors pt-4"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 tracking-wide mb-1.5">
            Pathologist Clinical Impression & Interpretive Comments
          </label>
          <textarea
            rows={2}
            value={interpretation}
            onChange={(e) => setInterpretation(e.target.value)}
            className="w-full rounded-xl border border-slate-300 text-xs p-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-cyan-500/20 focus:border-cyan-500"
          />
        </div>

        <div className="flex justify-end pt-3 border-t border-slate-100">
          <Button
            type="submit"
            variant="primary"
            icon={Save}
            size="lg"
            isLoading={isSubmitting}
            className="bg-cyan-600 hover:bg-cyan-500"
          >
            Certify & Publish Lab Report
          </Button>
        </div>
      </form>
    </div>
  );
};

export default LabReportsPage;

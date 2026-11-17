import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Stethoscope, Activity, FileText, CheckCircle2, AlertTriangle, Clock, Calendar, Plus, Save } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import doctorService from '../../services/doctorService';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import Button from '../../components/common/Button';

export const DoctorConsultationsPage = () => {
  const [searchParams] = useSearchParams();
  const { notifySuccess, notifyError } = useNotification();

  const [careContext, setCareContext] = useState(
    searchParams.get('context') === 'DOCUMENTED_WALK_IN' ? 'DOCUMENTED_WALK_IN' : 'APPOINTMENT'
  );
  const [appointmentId, setAppointmentId] = useState(searchParams.get('aptId') || '');
  const [patientHealthId, setPatientHealthId] = useState(searchParams.get('healthId') || 'MED-10293847-P');
  const [patientName, setPatientName] = useState(searchParams.get('patientName') || 'Sarah Jenkins');

  const [formData, setFormData] = useState({
    encounterType: 'IN_PERSON',
    chiefComplaint: '',
    walkInJustification: '',
    bloodPressure: '120/80',
    heartRate: '72',
    temperature: '98.6',
    respiratoryRate: '16',
    oxygenSaturation: '99',
    diagnosis: '',
    icd10: '',
    clinicalNotes: '',
    treatmentPlan: '',
  });

  const [recentEncounters, setRecentEncounters] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.chiefComplaint.trim() || !formData.diagnosis.trim()) {
      notifyError('Please fill in Chief Complaint and Diagnosis.');
      return;
    }

    if (careContext === 'DOCUMENTED_WALK_IN' && (!formData.walkInJustification || formData.walkInJustification.length < 10)) {
      notifyError('Documented walk-in consultation requires a clinical justification of at least 10 characters.');
      return;
    }

    setIsSubmitting(true);
    try {
      const result = await doctorService.createConsultation({
        healthId: patientHealthId,
        patientName,
        encounterType: formData.encounterType,
        encounterDate: new Date().toISOString(),
        careContext,
        appointmentId: careContext === 'APPOINTMENT' ? appointmentId : null,
        walkInJustification: careContext === 'DOCUMENTED_WALK_IN' ? formData.walkInJustification : null,
        chiefComplaint: formData.chiefComplaint,
        vitalSigns: {
          bloodPressure: formData.bloodPressure + ' mmHg',
          heartRate: formData.heartRate + ' bpm',
          temperature: formData.temperature + ' F',
          respiratoryRate: formData.respiratoryRate + ' bpm',
          oxygenSaturation: formData.oxygenSaturation + '%',
        },
        diagnosis: formData.diagnosis,
        icd10: formData.icd10 || 'Z00.00',
        clinicalNotes: formData.clinicalNotes,
        treatmentPlan: formData.treatmentPlan,
      });

      setRecentEncounters((prev) => [result, ...prev]);
      notifySuccess('Consultation encounter successfully documented and committed to patient record.');
      setFormData({
        encounterType: 'IN_PERSON',
        chiefComplaint: '',
        walkInJustification: '',
        bloodPressure: '120/80',
        heartRate: '72',
        temperature: '98.6',
        respiratoryRate: '16',
        oxygenSaturation: '99',
        diagnosis: '',
        icd10: '',
        clinicalNotes: '',
        treatmentPlan: '',
      });
    } catch {
      notifyError('Failed to record consultation encounter.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Clinical Encounter Documentation</h2>
        <p className="text-xs text-slate-500 mt-1">
          Record outpatient consultation notes, vital signs, and diagnostic directives under approved care contexts
        </p>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-6">
        {/* Care Context Selector */}
        <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-3">
          <span className="text-xs font-bold text-slate-700 uppercase tracking-wider block">
            Care Context Authorization Mode
          </span>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setCareContext('APPOINTMENT')}
              className={`p-3 rounded-xl border text-left transition-all ${
                careContext === 'APPOINTMENT'
                  ? 'border-blue-500 bg-blue-50 text-blue-900 font-semibold ring-2 ring-blue-500/20'
                  : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              <div className="flex items-center gap-2">
                <Calendar className="w-4 h-4 text-blue-600" />
                <span className="text-xs font-bold">Scheduled Appointment</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                Authorized via scheduled clinic appointment slot.
              </p>
            </button>

            <button
              type="button"
              onClick={() => setCareContext('DOCUMENTED_WALK_IN')}
              className={`p-3 rounded-xl border text-left transition-all ${
                careContext === 'DOCUMENTED_WALK_IN'
                  ? 'border-amber-500 bg-amber-50 text-amber-900 font-semibold ring-2 ring-amber-500/20'
                  : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
              }`}
            >
              <div className="flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 text-amber-600" />
                <span className="text-xs font-bold">Documented Walk-In Consultation</span>
              </div>
              <p className="text-[11px] text-slate-500 mt-1">
                Operation-specific clinical encounter with mandatory audit justification.
              </p>
            </button>
          </div>
        </div>

        {/* Patient Details & Walk-in Justification */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <FormInput
            label="Patient Universal Health ID"
            name="patientHealthId"
            value={patientHealthId}
            onChange={(e) => setPatientHealthId(e.target.value)}
            required
          />
          <FormInput
            label="Patient Full Name"
            name="patientName"
            value={patientName}
            onChange={(e) => setPatientName(e.target.value)}
            required
          />
          <SelectInput
            label="Encounter Type"
            name="encounterType"
            value={formData.encounterType}
            onChange={handleChange}
            options={[
              { value: 'IN_PERSON', label: 'In-Person Consultation' },
              { value: 'TELEHEALTH', label: 'Telehealth Video Encounter' },
            ]}
          />
        </div>

        {careContext === 'DOCUMENTED_WALK_IN' && (
          <div className="p-3.5 bg-amber-50/80 rounded-xl border border-amber-300">
            <FormInput
              label="Mandatory Walk-In Clinical Justification"
              name="walkInJustification"
              placeholder="e.g. Acute exacerbation of throat pain, patient arrived unscheduled at clinic triage"
              value={formData.walkInJustification}
              onChange={handleChange}
              helperText="Recorded permanently in audit logs. Does not grant reusable permanent access."
              required
            />
          </div>
        )}

        <FormInput
          label="Chief Complaint / Reason for Encounter"
          name="chiefComplaint"
          placeholder="e.g. Persistent tension headache for 4 days, mild dizziness"
          value={formData.chiefComplaint}
          onChange={handleChange}
          required
        />

        {/* Vital Signs Grid */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 uppercase tracking-wider mb-2">
            Recorded Vital Signs
          </label>
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
            <FormInput
              label="Blood Pressure"
              name="bloodPressure"
              placeholder="120/80"
              value={formData.bloodPressure}
              onChange={handleChange}
            />
            <FormInput
              label="Heart Rate (bpm)"
              name="heartRate"
              placeholder="72"
              value={formData.heartRate}
              onChange={handleChange}
            />
            <FormInput
              label="Temperature (F)"
              name="temperature"
              placeholder="98.6"
              value={formData.temperature}
              onChange={handleChange}
            />
            <FormInput
              label="Resp. Rate (bpm)"
              name="respiratoryRate"
              placeholder="16"
              value={formData.respiratoryRate}
              onChange={handleChange}
            />
            <FormInput
              label="SpO2 (%)"
              name="oxygenSaturation"
              placeholder="99"
              value={formData.oxygenSaturation}
              onChange={handleChange}
            />
          </div>
        </div>

        {/* Diagnosis & ICD-10 */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="sm:col-span-2">
            <FormInput
              label="Clinical Diagnosis"
              name="diagnosis"
              placeholder="e.g. Tension-Type Headache / Mild Dehydration"
              value={formData.diagnosis}
              onChange={handleChange}
              required
            />
          </div>
          <FormInput
            label="ICD-10 Code"
            name="icd10"
            placeholder="e.g. G44.209"
            value={formData.icd10}
            onChange={handleChange}
          />
        </div>

        {/* Clinical Notes & Plan */}
        <div>
          <label className="block text-xs font-semibold text-slate-700 tracking-wide mb-1.5">
            Objective Clinical Examination Notes & Assessment
          </label>
          <textarea
            name="clinicalNotes"
            rows={3}
            value={formData.clinicalNotes}
            onChange={handleChange}
            placeholder="Document physical exam, cranial nerves intact, pupillary reflex, throat inspection..."
            className="w-full rounded-xl border border-slate-300 text-xs p-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-700 tracking-wide mb-1.5">
            Treatment Plan, Medications & Patient Directives
          </label>
          <textarea
            name="treatmentPlan"
            rows={2}
            value={formData.treatmentPlan}
            onChange={handleChange}
            placeholder="Advised hydration (2.5L daily), oral analgesics PRN, rest, return if pain intensifies..."
            className="w-full rounded-xl border border-slate-300 text-xs p-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
          />
        </div>

        <div className="flex justify-end pt-3 border-t border-slate-100">
          <Button
            type="submit"
            variant="primary"
            icon={Save}
            size="lg"
            isLoading={isSubmitting}
            className="bg-blue-600 hover:bg-blue-500"
          >
            Sign & Commit Consultation Record
          </Button>
        </div>
      </form>

      {/* Recently Recorded Encounters in this Session */}
      {recentEncounters.length > 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-3">
          <h3 className="text-sm font-bold text-slate-900">Encounters Recorded This Session</h3>
          <div className="space-y-2">
            {recentEncounters.map((enc) => (
              <div key={enc.id} className="p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs flex justify-between items-center">
                <div>
                  <span className="font-bold text-slate-900">{enc.diagnosis}</span>
                  <p className="text-slate-500">{enc.patientName} ({enc.healthId}) • {enc.careContext}</p>
                </div>
                <span className="text-emerald-600 font-semibold flex items-center gap-1">
                  <CheckCircle2 className="w-4 h-4" /> Signed & Committed
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorConsultationsPage;

import api from './api';
import {
  MOCK_APPOINTMENTS,
  MOCK_PATIENTS,
  MOCK_MEDICAL_RECORDS,
  MOCK_PRESCRIPTIONS,
  MOCK_CONSENT_RECORDS,
} from '../data/mockData';

export const doctorService = {
  async getDoctorAppointments() {
    try {
      const res = await api.get('/api/v1/appointments/me');
      return res.data;
    } catch {
      return MOCK_APPOINTMENTS;
    }
  },

  async getAuthorizedPatients() {
    try {
      const res = await api.get('/api/v1/doctor/patients/authorized');
      return res.data;
    } catch {
      return MOCK_PATIENTS.map((p, idx) => ({
        ...p,
        consentCategory: idx === 1 ? 'CONSULT_ONLY' : 'FULL_TIMELINE',
        consentExpiresAt: '2026-12-15T23:59:59Z',
        lastEncounterDate: '2026-10-24T10:15:00',
      }));
    }
  },

  async lookupPatientByHealthId(healthId) {
    try {
      const res = await api.get(`/api/v1/doctor/patients/lookup?healthId=${encodeURIComponent(healthId)}`);
      return res.data;
    } catch {
      const patient = MOCK_PATIENTS.find((p) => p.healthId.toUpperCase() === healthId.trim().toUpperCase());
      if (!patient) {
        throw new Error(`No patient found with Health ID "${healthId}"`);
      }
      return {
        ...patient,
        hasActiveConsent: true,
        consentCategory: 'FULL_TIMELINE',
      };
    }
  },

  async createConsultation(consultationData) {
    try {
      const res = await api.post('/api/v1/medical-records/consultations', consultationData);
      return res.data;
    } catch {
      const newRecord = {
        id: `rec-${Date.now()}`,
        patientId: consultationData.patientId || 'pat-1',
        healthId: consultationData.healthId || 'MED-10293847-P',
        encounterType: consultationData.encounterType || 'IN_PERSON',
        encounterDate: consultationData.encounterDate || new Date().toISOString(),
        doctorName: 'Dr. Authenticated Doctor, MD',
        specialty: 'Clinical Consultation',
        chiefComplaint: consultationData.chiefComplaint,
        vitalSigns: consultationData.vitalSigns || {},
        diagnosis: consultationData.diagnosis,
        icd10: consultationData.icd10 || 'Z00.00',
        clinicalNotes: consultationData.clinicalNotes,
        treatmentPlan: consultationData.treatmentPlan,
        careContext: consultationData.careContext || 'APPOINTMENT', // 'APPOINTMENT' or 'DOCUMENTED_WALK_IN'
        walkInJustification: consultationData.walkInJustification || null,
      };
      return newRecord;
    }
  },

  async createPrescription(prescriptionData) {
    try {
      const res = await api.post('/api/v1/prescriptions', prescriptionData);
      return res.data;
    } catch {
      const newPrescription = {
        id: `rx-${Date.now()}`,
        patientId: prescriptionData.patientId || 'pat-1',
        healthId: prescriptionData.healthId || 'MED-10293847-P',
        doctorName: 'Dr. Authenticated Doctor, MD',
        prescribedDate: new Date().toISOString().split('T')[0],
        status: 'ACTIVE',
        items: prescriptionData.items.map((item, idx) => ({
          id: `item-${Date.now()}-${idx}`,
          ...item,
          dispensingStatus: 'PENDING',
        })),
      };
      return newPrescription;
    }
  },

  async getPatientTimeline(healthId, category = 'FULL_TIMELINE') {
    try {
      const res = await api.get(`/api/v1/patients/${healthId}/timeline?category=${category}`);
      return res.data;
    } catch {
      const records = MOCK_MEDICAL_RECORDS.filter((r) => r.healthId === healthId || healthId === 'MED-10293847-P');
      const prescriptions = MOCK_PRESCRIPTIONS.filter((p) => p.healthId === healthId || healthId === 'MED-10293847-P');

      if (category === 'CONSULT_ONLY') {
        return { records, prescriptions: [] };
      }
      return { records, prescriptions };
    }
  },
};

export default doctorService;

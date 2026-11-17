import api from './api';
import {
  MOCK_PATIENTS,
  MOCK_MEDICAL_RECORDS,
  MOCK_PRESCRIPTIONS,
  MOCK_LAB_REPORTS,
  MOCK_CONSENT_RECORDS,
  MOCK_EMERGENCY_PROFILES,
} from '../data/mockData';

export const patientService = {
  async getProfile() {
    try {
      const res = await api.get('/api/v1/patients/me');
      return res.data;
    } catch {
      return MOCK_PATIENTS[0];
    }
  },

  async updateProfile(profileData) {
    try {
      const res = await api.put('/api/v1/patients/me', profileData);
      return res.data;
    } catch {
      return { ...MOCK_PATIENTS[0], ...profileData };
    }
  },

  async getMedicalRecords() {
    try {
      const res = await api.get('/api/v1/medical-records/me');
      return res.data;
    } catch {
      return MOCK_MEDICAL_RECORDS;
    }
  },

  async getPrescriptions() {
    try {
      const res = await api.get('/api/v1/prescriptions/me');
      return res.data;
    } catch {
      return MOCK_PRESCRIPTIONS;
    }
  },

  async getLabReports() {
    try {
      const res = await api.get('/api/v1/lab-reports/me');
      return res.data;
    } catch {
      return MOCK_LAB_REPORTS;
    }
  },

  async getConsents() {
    try {
      const res = await api.get('/api/v1/consents/me');
      return res.data;
    } catch {
      return MOCK_CONSENT_RECORDS;
    }
  },

  async grantConsent(consentData) {
    try {
      const res = await api.post('/api/v1/consents', consentData);
      return res.data;
    } catch {
      const newConsent = {
        id: `cst-${Date.now()}`,
        patientId: 'pat-1',
        providerName: consentData.providerName || 'Dr. Assigned Provider',
        providerRole: consentData.providerRole || 'Clinical Specialist',
        organizationName: consentData.organizationName || 'Affiliated Medical Center',
        consentCategory: consentData.consentCategory,
        grantedAt: new Date().toISOString(),
        expiresAt: consentData.expiresAt || new Date(Date.now() + 30 * 24 * 3600 * 1000).toISOString(),
        status: 'ACTIVE',
      };
      return newConsent;
    }
  },

  async revokeConsent(consentId) {
    try {
      const res = await api.put(`/api/v1/consents/${consentId}/revoke`);
      return res.data;
    } catch {
      return { success: true, message: 'Consent successfully revoked', consentId };
    }
  },

  async getEmergencyProfile(healthId = 'MED-10293847-P') {
    try {
      const res = await api.get('/api/v1/patients/me/emergency-profile');
      return res.data;
    } catch {
      return MOCK_EMERGENCY_PROFILES[healthId] || MOCK_EMERGENCY_PROFILES['MED-10293847-P'];
    }
  },
};

export default patientService;

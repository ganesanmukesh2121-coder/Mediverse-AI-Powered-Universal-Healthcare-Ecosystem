import api from './api';
import { MOCK_ADMISSIONS } from '../data/mockData';

export const medicalOrgService = {
  async getAdmissions() {
    try {
      const res = await api.get('/api/v1/medical/admissions');
      return res.data;
    } catch {
      return MOCK_ADMISSIONS;
    }
  },

  async createAdmission(admissionData) {
    try {
      const res = await api.post('/api/v1/medical/admissions', admissionData);
      return res.data;
    } catch {
      const newAdm = {
        id: `adm-${Date.now()}`,
        admissionNumber: `ADM-${new Date().getFullYear()}-${Math.floor(1000 + Math.random() * 9000)}`,
        patientName: admissionData.patientName,
        healthId: admissionData.healthId,
        gender: admissionData.gender || 'UNKNOWN',
        age: admissionData.age || 40,
        admissionDate: new Date().toISOString(),
        department: admissionData.department,
        ward: admissionData.ward,
        bedNumber: admissionData.bedNumber,
        admittingDoctor: admissionData.admittingDoctor,
        diagnosis: admissionData.diagnosis,
        status: 'ADMITTED',
        dietType: admissionData.dietType || 'Standard Regular',
        isolationPrecautions: admissionData.isolationPrecautions || 'None',
      };
      return newAdm;
    }
  },

  async dischargePatient(admissionId, dischargeData) {
    try {
      const res = await api.post(`/api/v1/medical/admissions/${admissionId}/discharge`, dischargeData);
      return res.data;
    } catch {
      return {
        id: admissionId,
        status: 'DISCHARGED',
        dischargeDate: new Date().toISOString(),
        dischargeSummary: dischargeData.dischargeSummary,
        prescriptionsUponDischarge: dischargeData.prescriptionsUponDischarge || [],
      };
    }
  },
};

export default medicalOrgService;

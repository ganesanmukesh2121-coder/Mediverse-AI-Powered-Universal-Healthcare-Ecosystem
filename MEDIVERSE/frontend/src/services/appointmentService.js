import api from './api';
import { MOCK_APPOINTMENTS } from '../data/mockData';

export const appointmentService = {
  async getMyAppointments() {
    try {
      const res = await api.get('/api/v1/appointments/me');
      return res.data;
    } catch {
      return MOCK_APPOINTMENTS;
    }
  },

  async createAppointment(appointmentRequest) {
    try {
      const res = await api.post('/api/v1/appointments', appointmentRequest);
      return res.data;
    } catch {
      const newApt = {
        id: `apt-${Date.now()}`,
        patientUserId: appointmentRequest.patientUserId || 'user-pat-1',
        patientName: appointmentRequest.patientName || 'Sarah Jenkins',
        healthId: appointmentRequest.healthId || 'MED-10293847-P',
        doctorUserId: appointmentRequest.doctorUserId || 'user-doc-1',
        doctorName: appointmentRequest.doctorName || 'Dr. Marcus Sterling, MD',
        specialization: appointmentRequest.specialization || 'Cardiology',
        appointmentDate: appointmentRequest.appointmentDate,
        appointmentType: appointmentRequest.appointmentType || 'IN_PERSON',
        status: 'CONFIRMED',
        reasonForVisit: appointmentRequest.reasonForVisit || 'Consultation',
        notes: appointmentRequest.notes || '',
      };
      return newApt;
    }
  },

  async updateAppointmentStatus(appointmentId, status) {
    try {
      const res = await api.put(`/api/v1/appointments/${appointmentId}/status`, { status });
      return res.data;
    } catch {
      return {
        id: appointmentId,
        status,
        updatedAt: new Date().toISOString(),
      };
    }
  },
};

export default appointmentService;

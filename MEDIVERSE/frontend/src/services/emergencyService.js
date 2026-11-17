import api from './api';
import { MOCK_EMERGENCY_PROFILES, MOCK_EMERGENCY_AUDIT_LOGS } from '../data/mockData';

export const emergencyService = {
  async lookupPatientEmergencyInfo(healthId, justificationReason) {
    if (!healthId || !healthId.trim()) {
      throw new Error('Patient Health ID is required.');
    }
    if (!justificationReason || justificationReason.trim().length < 10) {
      throw new Error('Mandatory emergency clinical justification must be at least 10 characters long.');
    }

    try {
      const res = await api.post('/api/v1/emergency/lookup', {
        healthId: healthId.trim().toUpperCase(),
        justificationReason: justificationReason.trim(),
      });
      return res.data;
    } catch {
      // Return strictly limited emergency triage data from mock
      const normalizedId = healthId.trim().toUpperCase();
      const profile = MOCK_EMERGENCY_PROFILES[normalizedId] || {
        healthId: normalizedId,
        fullName: 'Unknown Patient (Emergency Registered)',
        bloodGroup: 'UNKNOWN',
        dateOfBirth: 'N/A',
        gender: 'UNKNOWN',
        allergies: ['No documented allergies listed in system'],
        criticalConditions: ['Acute trauma / emergency triage in progress'],
        activeMedications: ['No active home medications recorded'],
        emergencyContactName: 'Pending contact verification',
        emergencyContactPhone: 'N/A',
        organDonor: false,
        dnrStatus: false,
        specialInstructions: 'Initiate standard emergency airway, breathing, circulation (ABC) protocol.',
      };

      // Record access in local session audit
      const accessLog = {
        id: `emg-log-${Date.now()}`,
        timestamp: new Date().toISOString(),
        responderName: 'Current Emergency Responder',
        patientHealthId: normalizedId,
        patientName: profile.fullName,
        justificationReason,
        accessedFields: ['Allergies', 'Conditions', 'Emergency Medications', 'Blood Group', 'Emergency Contact'],
        location: 'Current Field / ER Facility',
      };

      const existingLogs = JSON.parse(sessionStorage.getItem('mediverse_emergency_logs') || '[]');
      sessionStorage.setItem('mediverse_emergency_logs', JSON.stringify([accessLog, ...existingLogs]));

      return profile;
    }
  },

  async getEmergencyHistory() {
    try {
      const res = await api.get('/api/v1/emergency/history');
      return res.data;
    } catch {
      const sessionLogs = JSON.parse(sessionStorage.getItem('mediverse_emergency_logs') || '[]');
      return [...sessionLogs, ...MOCK_EMERGENCY_AUDIT_LOGS];
    }
  },
};

export default emergencyService;

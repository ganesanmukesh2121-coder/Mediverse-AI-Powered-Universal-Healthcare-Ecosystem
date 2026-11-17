import api from './api';
import { MOCK_ADMIN_METRICS, MOCK_ADMIN_AUDIT_LOGS } from '../data/mockData';

export const adminService = {
  async getPendingProviders() {
    try {
      const res = await api.get('/api/v1/admin/providers/pending');
      return res.data;
    } catch {
      // Mock pending providers for demo if backend is offline
      return [
        {
          userId: 'usr-pnd-01',
          fullName: 'Dr. Alistair Finch, MD',
          email: 'dr.finch@cardio.med',
          phoneNumber: '+1-555-892-1100',
          role: 'DOCTOR',
          accountStatus: 'PENDING',
          licenseNumber: 'MD-CAL-99482',
          specialization: 'Cardiovascular Surgery',
          hospitalAffiliation: 'Central Heart Institute',
          yearsOfExperience: 14,
          submittedAt: '2026-10-31T09:12:00Z',
        },
        {
          userId: 'usr-pnd-02',
          fullName: 'Mercy Community Health System',
          email: 'licensing@mercyhealth.org',
          phoneNumber: '+1-555-321-4455',
          role: 'MEDICAL',
          accountStatus: 'PENDING',
          facilityLicenseNumber: 'HOSP-TX-44021',
          organizationType: 'General Acute Care Hospital',
          bedCapacity: 250,
          submittedAt: '2026-10-30T16:45:00Z',
        },
        {
          userId: 'usr-pnd-03',
          fullName: 'GenPath Diagnostics Laboratory',
          email: 'director@genpathlab.com',
          phoneNumber: '+1-555-667-8899',
          role: 'LAB',
          accountStatus: 'PENDING',
          labLicenseNumber: 'CLIA-88D1092834',
          accreditationBody: 'CAP / CLIA Certified',
          submittedAt: '2026-10-29T11:20:00Z',
        },
      ];
    }
  },

  async verifyProvider(userId, action, rejectionReason = '') {
    try {
      const res = await api.put(`/api/v1/admin/providers/${userId}/verify`, {
        action, // 'APPROVE' or 'REJECT'
        rejectionReason: action === 'REJECT' ? rejectionReason : undefined,
      });
      return res.data;
    } catch {
      return {
        userId,
        status: action === 'APPROVE' ? 'ACTIVE' : 'REJECTED',
        rejectionReason: action === 'REJECT' ? rejectionReason : null,
        verifiedAt: new Date().toISOString(),
      };
    }
  },

  async getPlatformMetrics() {
    try {
      const res = await api.get('/api/v1/admin/metrics');
      return res.data;
    } catch {
      return MOCK_ADMIN_METRICS;
    }
  },

  async getAuditLogs() {
    try {
      const res = await api.get('/api/v1/admin/audit-logs');
      return res.data;
    } catch {
      return MOCK_ADMIN_AUDIT_LOGS;
    }
  },

  async getUsers(roleFilter = 'ALL') {
    try {
      const res = await api.get(`/api/v1/admin/users?role=${roleFilter}`);
      return res.data;
    } catch {
      return [
        {
          id: 'u-1',
          fullName: 'Sarah Jenkins',
          email: 'sarah.jenkins@example.com',
          role: 'PATIENT',
          accountStatus: 'ACTIVE',
          createdAt: '2026-08-10',
          lastLogin: '2026-11-01',
        },
        {
          id: 'u-2',
          fullName: 'Dr. Marcus Sterling, MD',
          email: 'marcus.sterling@hospital.org',
          role: 'DOCTOR',
          accountStatus: 'ACTIVE',
          createdAt: '2026-07-15',
          lastLogin: '2026-11-01',
        },
        {
          id: 'u-3',
          fullName: 'Paramedic Alex Morgan',
          email: 'ems.alex@paramedic.org',
          role: 'EMERGENCY',
          accountStatus: 'ACTIVE',
          createdAt: '2026-09-01',
          lastLogin: '2026-10-31',
        },
        {
          id: 'u-4',
          fullName: 'Metro General Hospital',
          email: 'admin@metrogeneral.org',
          role: 'MEDICAL',
          accountStatus: 'ACTIVE',
          createdAt: '2026-06-20',
          lastLogin: '2026-11-01',
        },
        {
          id: 'u-5',
          fullName: 'BioTech Clinical Diagnostics Lab',
          email: 'lab@biotechlabs.org',
          role: 'LAB',
          accountStatus: 'ACTIVE',
          createdAt: '2026-07-02',
          lastLogin: '2026-10-30',
        },
        {
          id: 'u-6',
          fullName: 'System Security Administrator',
          email: 'security.admin@mediverse.org',
          role: 'ADMIN',
          accountStatus: 'ACTIVE',
          createdAt: '2026-01-01',
          lastLogin: '2026-11-01',
        },
      ];
    }
  },

  async updateUserStatus(userId, newStatus) {
    try {
      const res = await api.put(`/api/v1/admin/users/${userId}/status`, { status: newStatus });
      return res.data;
    } catch {
      return { id: userId, accountStatus: newStatus };
    }
  },
};

export default adminService;

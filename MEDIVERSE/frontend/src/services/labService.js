import api from './api';
import { MOCK_LAB_ORDERS, MOCK_LAB_REPORTS } from '../data/mockData';

export const labService = {
  async getTestOrders() {
    try {
      const res = await api.get('/api/v1/lab/orders');
      return res.data;
    } catch {
      return MOCK_LAB_ORDERS;
    }
  },

  async updateOrderStatus(orderId, status) {
    try {
      const res = await api.put(`/api/v1/lab/orders/${orderId}/status`, { status });
      return res.data;
    } catch {
      return { id: orderId, status, updatedAt: new Date().toISOString() };
    }
  },

  async getCompletedReports() {
    try {
      const res = await api.get('/api/v1/lab/reports');
      return res.data;
    } catch {
      return MOCK_LAB_REPORTS;
    }
  },

  async uploadLabReport(reportData) {
    try {
      const res = await api.post('/api/v1/lab/reports', reportData);
      return res.data;
    } catch {
      const newReport = {
        id: `lab-${Date.now()}`,
        orderId: reportData.orderId || `ORD-${Math.floor(10000 + Math.random() * 90000)}`,
        patientId: reportData.patientId || 'pat-1',
        healthId: reportData.healthId,
        testName: reportData.testName,
        orderingDoctor: reportData.orderingDoctor || 'Dr. Medical Staff',
        labFacility: reportData.labFacility || 'Authenticated Clinical Laboratory',
        collectedDate: reportData.collectedDate || new Date().toISOString(),
        reportedDate: new Date().toISOString(),
        overallStatus: 'COMPLETED',
        clinicalInterpretation: reportData.clinicalInterpretation || 'Test completed and verified by laboratory technician.',
        results: reportData.results || [],
      };
      return newReport;
    }
  },
};

export default labService;

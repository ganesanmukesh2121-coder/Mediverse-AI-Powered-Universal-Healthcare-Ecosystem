import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ProtectedRoute from './ProtectedRoute';
import RoleBasedRoute from './RoleBasedRoute';
import MainLayout from '../components/layout/MainLayout';

// Auth Pages
import LoginPage from '../pages/auth/LoginPage';
import PatientRegisterPage from '../pages/auth/PatientRegisterPage';
import DoctorRegisterPage from '../pages/auth/DoctorRegisterPage';
import MedicalRegisterPage from '../pages/auth/MedicalRegisterPage';
import LabRegisterPage from '../pages/auth/LabRegisterPage';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '../pages/auth/ResetPasswordPage';
import AccountPendingPage from '../pages/auth/AccountPendingPage';
import AccountSuspendedPage from '../pages/auth/AccountSuspendedPage';

// Common Pages
import NotFoundPage from '../pages/common/NotFoundPage';
import UnauthorizedPage from '../pages/common/UnauthorizedPage';

// Patient Pages
import PatientDashboard from '../pages/patient/PatientDashboard';
import HealthIdPage from '../pages/patient/HealthIdPage';
import PatientProfilePage from '../pages/patient/PatientProfilePage';
import MedicalRecordsPage from '../pages/patient/MedicalRecordsPage';
import AppointmentsPage from '../pages/patient/AppointmentsPage';
import PrescriptionsPage from '../pages/patient/PrescriptionsPage';
import PatientLabReportsPage from '../pages/patient/LabReportsPage';
import ConsentManagementPage from '../pages/patient/ConsentManagementPage';
import EmergencyInfoPage from '../pages/patient/EmergencyInfoPage';
import PatientSettingsPage from '../pages/patient/PatientSettingsPage';

// Emergency Pages
import EmergencyDashboard from '../pages/emergency/EmergencyDashboard';
import EmergencyLookupPage from '../pages/emergency/EmergencyLookupPage';
import EmergencyHistoryPage from '../pages/emergency/EmergencyHistoryPage';
import EmergencySettingsPage from '../pages/emergency/EmergencySettingsPage';

// Doctor Pages
import DoctorDashboard from '../pages/doctor/DoctorDashboard';
import DoctorAppointmentsPage from '../pages/doctor/DoctorAppointmentsPage';
import DoctorPatientLookupPage from '../pages/doctor/DoctorPatientLookupPage';
import DoctorAuthorizedPatientsPage from '../pages/doctor/DoctorAuthorizedPatientsPage';
import DoctorConsultationsPage from '../pages/doctor/DoctorConsultationsPage';
import DoctorPrescriptionsPage from '../pages/doctor/DoctorPrescriptionsPage';
import DoctorPatientTimelinePage from '../pages/doctor/DoctorPatientTimelinePage';
import DoctorSettingsPage from '../pages/doctor/DoctorSettingsPage';

// Medical Org Pages
import MedicalDashboard from '../pages/medical/MedicalDashboard';
import MedicalPatientLookupPage from '../pages/medical/MedicalPatientLookupPage';
import MedicalWorkflowPage from '../pages/medical/MedicalWorkflowPage';
import MedicalAdmissionsPage from '../pages/medical/MedicalAdmissionsPage';
import MedicalDischargesPage from '../pages/medical/MedicalDischargesPage';
import MedicalSettingsPage from '../pages/medical/MedicalSettingsPage';

// Lab Pages
import LabDashboard from '../pages/lab/LabDashboard';
import LabOrdersPage from '../pages/lab/LabOrdersPage';
import LabPatientLookupPage from '../pages/lab/LabPatientLookupPage';
import LabInProgressPage from '../pages/lab/LabInProgressPage';
import LabCompletedPage from '../pages/lab/LabCompletedPage';
import LabReportsPage from '../pages/lab/LabReportsPage';
import LabSettingsPage from '../pages/lab/LabSettingsPage';

// Admin Pages
import AdminDashboard from '../pages/admin/AdminDashboard';
import ProviderVerificationPage from '../pages/admin/ProviderVerificationPage';
import UserManagementPage from '../pages/admin/UserManagementPage';
import AccountManagementPage from '../pages/admin/AccountManagementPage';
import AuditLogsPage from '../pages/admin/AuditLogsPage';
import AdminSettingsPage from '../pages/admin/AdminSettingsPage';

export const AppRoutes = () => {
  const { isAuthenticated, getPortalHome } = useAuth();

  return (
    <Routes>
      {/* Root Redirection */}
      <Route
        path="/"
        element={
          isAuthenticated ? (
            <Navigate to={getPortalHome()} replace />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      />

      {/* Public Authentication Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register/patient" element={<PatientRegisterPage />} />
      <Route path="/register/doctor" element={<DoctorRegisterPage />} />
      <Route path="/register/medical" element={<MedicalRegisterPage />} />
      <Route path="/register/lab" element={<LabRegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/account-pending" element={<AccountPendingPage />} />
      <Route path="/account-suspended" element={<AccountSuspendedPage />} />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* Protected Role-Based Portals */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* PATIENT PORTAL */}
        <Route
          path="/patient/dashboard"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <PatientDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/health-id"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <HealthIdPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/profile"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <PatientProfilePage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/records"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <MedicalRecordsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/appointments"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <AppointmentsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/prescriptions"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <PrescriptionsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/lab-reports"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <PatientLabReportsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/consents"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <ConsentManagementPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/emergency"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <EmergencyInfoPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/patient/settings"
          element={
            <RoleBasedRoute allowedRoles={['PATIENT']}>
              <PatientSettingsPage />
            </RoleBasedRoute>
          }
        />

        {/* EMERGENCY PORTAL */}
        <Route
          path="/emergency/dashboard"
          element={
            <RoleBasedRoute allowedRoles={['EMERGENCY']}>
              <EmergencyDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/emergency/lookup"
          element={
            <RoleBasedRoute allowedRoles={['EMERGENCY']}>
              <EmergencyLookupPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/emergency/history"
          element={
            <RoleBasedRoute allowedRoles={['EMERGENCY']}>
              <EmergencyHistoryPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/emergency/settings"
          element={
            <RoleBasedRoute allowedRoles={['EMERGENCY']}>
              <EmergencySettingsPage />
            </RoleBasedRoute>
          }
        />

        {/* DOCTOR PORTAL */}
        <Route
          path="/doctor/dashboard"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/appointments"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorAppointmentsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/lookup"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorPatientLookupPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/patients"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorAuthorizedPatientsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/consultations"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorConsultationsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/prescriptions"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorPrescriptionsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/timeline"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorPatientTimelinePage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/doctor/settings"
          element={
            <RoleBasedRoute allowedRoles={['DOCTOR']}>
              <DoctorSettingsPage />
            </RoleBasedRoute>
          }
        />

        {/* MEDICAL PORTAL */}
        <Route
          path="/medical/dashboard"
          element={
            <RoleBasedRoute allowedRoles={['MEDICAL']}>
              <MedicalDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/medical/lookup"
          element={
            <RoleBasedRoute allowedRoles={['MEDICAL']}>
              <MedicalPatientLookupPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/medical/workflow"
          element={
            <RoleBasedRoute allowedRoles={['MEDICAL']}>
              <MedicalWorkflowPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/medical/admissions"
          element={
            <RoleBasedRoute allowedRoles={['MEDICAL']}>
              <MedicalAdmissionsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/medical/discharges"
          element={
            <RoleBasedRoute allowedRoles={['MEDICAL']}>
              <MedicalDischargesPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/medical/settings"
          element={
            <RoleBasedRoute allowedRoles={['MEDICAL']}>
              <MedicalSettingsPage />
            </RoleBasedRoute>
          }
        />

        {/* LAB PORTAL */}
        <Route
          path="/lab/dashboard"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/lab/orders"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabOrdersPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/lab/lookup"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabPatientLookupPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/lab/in-progress"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabInProgressPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/lab/completed"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabCompletedPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/lab/reports"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabReportsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/lab/settings"
          element={
            <RoleBasedRoute allowedRoles={['LAB']}>
              <LabSettingsPage />
            </RoleBasedRoute>
          }
        />

        {/* ADMIN PORTAL */}
        <Route
          path="/admin/dashboard"
          element={
            <RoleBasedRoute allowedRoles={['ADMIN']}>
              <AdminDashboard />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/admin/verifications"
          element={
            <RoleBasedRoute allowedRoles={['ADMIN']}>
              <ProviderVerificationPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/admin/users"
          element={
            <RoleBasedRoute allowedRoles={['ADMIN']}>
              <UserManagementPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/admin/accounts"
          element={
            <RoleBasedRoute allowedRoles={['ADMIN']}>
              <AccountManagementPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/admin/audit-logs"
          element={
            <RoleBasedRoute allowedRoles={['ADMIN']}>
              <AuditLogsPage />
            </RoleBasedRoute>
          }
        />
        <Route
          path="/admin/settings"
          element={
            <RoleBasedRoute allowedRoles={['ADMIN']}>
              <AdminSettingsPage />
            </RoleBasedRoute>
          }
        />
      </Route>

      {/* Catch-All 404 Route */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;

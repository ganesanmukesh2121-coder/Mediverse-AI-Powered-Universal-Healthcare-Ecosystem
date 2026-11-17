import React, { useState, useEffect } from 'react';
import { ShieldCheck, Plus, Clock, AlertTriangle, ShieldX, CheckCircle2, User, Building2 } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import patientService from '../../services/patientService';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import ConfirmationDialog from '../../components/common/ConfirmationDialog';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const ConsentManagementPage = () => {
  const { notifySuccess, notifyError } = useNotification();
  const [consents, setConsents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // Grant Consent Modal State
  const [isGrantModalOpen, setIsGrantModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [newConsent, setNewConsent] = useState({
    providerName: '',
    providerRole: 'Cardiology',
    organizationName: '',
    consentCategory: 'FULL_TIMELINE',
    expiresAt: '',
  });

  // Revoke Dialog State
  const [revokeTarget, setRevokeTarget] = useState(null);
  const [isRevoking, setIsRevoking] = useState(false);

  const fetchConsents = async () => {
    try {
      const data = await patientService.getConsents();
      setConsents(data);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchConsents();
  }, []);

  const handleGrantSubmit = async (e) => {
    e.preventDefault();
    if (!newConsent.providerName.trim() || !newConsent.expiresAt) {
      notifyError('Please specify provider name and consent expiration date.');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await patientService.grantConsent(newConsent);
      setConsents((prev) => [created, ...prev]);
      notifySuccess(`Consent successfully granted to ${created.providerName}!`);
      setIsGrantModalOpen(false);
      setNewConsent({
        providerName: '',
        providerRole: 'Cardiology',
        organizationName: '',
        consentCategory: 'FULL_TIMELINE',
        expiresAt: '',
      });
    } catch {
      notifyError('Failed to record consent authorization.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmRevoke = async () => {
    if (!revokeTarget) return;
    setIsRevoking(true);
    try {
      await patientService.revokeConsent(revokeTarget.id);
      setConsents((prev) =>
        prev.map((c) => (c.id === revokeTarget.id ? { ...c, status: 'REVOKED' } : c))
      );
      notifySuccess(`Access consent revoked for ${revokeTarget.providerName}.`);
      setRevokeTarget(null);
    } finally {
      setIsRevoking(false);
    }
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading consent authorizations..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Consent Governance Manager</h2>
          <p className="text-xs text-slate-500 mt-1">
            You hold total authority over which healthcare providers can access your medical records and for what duration
          </p>
        </div>

        <Button
          variant="primary"
          icon={Plus}
          onClick={() => setIsGrantModalOpen(true)}
        >
          Authorize New Provider
        </Button>
      </div>

      {/* Explanatory Notice */}
      <div className="p-4 bg-indigo-50/60 border border-indigo-200 rounded-2xl flex items-start gap-3 text-xs text-indigo-950">
        <ShieldCheck className="w-5 h-5 text-indigo-600 shrink-0 mt-0.5" />
        <div>
          <h4 className="font-bold text-indigo-900">Patient-Centric Zero Trust Data Architecture</h4>
          <p className="mt-0.5 leading-relaxed text-indigo-900/80">
            Mediverse enforces cryptographic consent filtering. A provider granted <strong>LAB_ONLY</strong> access cannot see your consultation history. You may revoke consent at any moment with immediate effect.
          </p>
        </div>
      </div>

      {/* Active and Historical Consents */}
      <div className="space-y-3">
        {consents.map((consent) => (
          <div
            key={consent.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div
                className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 border ${
                  consent.status === 'ACTIVE'
                    ? 'bg-emerald-50 text-emerald-600 border-emerald-100'
                    : 'bg-slate-100 text-slate-400 border-slate-200'
                }`}
              >
                {consent.status === 'ACTIVE' ? (
                  <ShieldCheck className="w-6 h-6" />
                ) : (
                  <ShieldX className="w-6 h-6" />
                )}
              </div>

              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{consent.providerName}</h4>
                  <StatusBadge status={consent.status} size="sm" />
                  <StatusBadge status={consent.consentCategory} size="sm" />
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  {consent.providerRole} • {consent.organizationName}
                </p>
                <div className="flex flex-wrap items-center gap-4 mt-2 text-xs text-slate-500">
                  <span>
                    Granted:{' '}
                    <strong className="text-slate-700">
                      {new Date(consent.grantedAt).toLocaleDateString()}
                    </strong>
                  </span>
                  <span>
                    Expires:{' '}
                    <strong className="text-slate-700">
                      {new Date(consent.expiresAt).toLocaleDateString()}
                    </strong>
                  </span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full md:w-auto justify-end border-t md:border-t-0 pt-3 md:pt-0">
              {consent.status === 'ACTIVE' && (
                <Button
                  variant="outline"
                  size="sm"
                  className="text-rose-600 border-rose-200 hover:bg-rose-50"
                  onClick={() => setRevokeTarget(consent)}
                >
                  Revoke Consent
                </Button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Grant New Consent Modal */}
      <Modal
        isOpen={isGrantModalOpen}
        onClose={() => setIsGrantModalOpen(false)}
        title="Authorize Provider Access"
        subtitle="Specify grantee provider and permissible medical data scope"
      >
        <form onSubmit={handleGrantSubmit} className="space-y-4">
          <FormInput
            label="Provider Full Legal Name"
            name="providerName"
            placeholder="e.g. Dr. Evelyn Reed, MD"
            value={newConsent.providerName}
            onChange={(e) => setNewConsent((prev) => ({ ...prev, providerName: e.target.value }))}
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Provider Specialty / Role"
              name="providerRole"
              placeholder="e.g. Endocrinology"
              value={newConsent.providerRole}
              onChange={(e) => setNewConsent((prev) => ({ ...prev, providerRole: e.target.value }))}
            />
            <FormInput
              label="Healthcare Facility / Clinic"
              name="organizationName"
              placeholder="e.g. St. Jude Specialty Clinic"
              value={newConsent.organizationName}
              onChange={(e) => setNewConsent((prev) => ({ ...prev, organizationName: e.target.value }))}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <SelectInput
              label="Permissible Scope Category"
              name="consentCategory"
              value={newConsent.consentCategory}
              onChange={(e) => setNewConsent((prev) => ({ ...prev, consentCategory: e.target.value }))}
              options={[
                { value: 'FULL_TIMELINE', label: 'FULL_TIMELINE — All Consultations, Labs & Prescriptions' },
                { value: 'CONSULT_ONLY', label: 'CONSULT_ONLY — Outpatient Encounters & Prescriptions' },
                { value: 'LAB_ONLY', label: 'LAB_ONLY — Diagnostic Lab & Pathology Reports' },
              ]}
            />

            <FormInput
              label="Consent Expiration Date"
              name="expiresAt"
              type="date"
              value={newConsent.expiresAt}
              onChange={(e) => setNewConsent((prev) => ({ ...prev, expiresAt: e.target.value }))}
              required
            />
          </div>

          <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setIsGrantModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              Authorize Provider
            </Button>
          </div>
        </form>
      </Modal>

      {/* Revoke Confirmation Dialog */}
      <ConfirmationDialog
        isOpen={!!revokeTarget}
        onClose={() => setRevokeTarget(null)}
        onConfirm={handleConfirmRevoke}
        title="Revoke Consent Authorization"
        message={`Are you sure you want to revoke data access for ${revokeTarget?.providerName}? They will immediately lose access to your medical records.`}
        confirmLabel="Yes, Revoke Access"
        isDangerous
        isLoading={isRevoking}
      />
    </div>
  );
};

export default ConsentManagementPage;

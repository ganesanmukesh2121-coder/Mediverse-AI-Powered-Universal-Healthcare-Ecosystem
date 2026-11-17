import React, { useState, useEffect } from 'react';
import { ShieldCheck, Check, X, Award, Building, Stethoscope, FlaskConical, Search, AlertTriangle } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import adminService from '../../services/adminService';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/dashboard/SearchBar';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const ProviderVerificationPage = () => {
  const { notifySuccess, notifyError } = useNotification();
  const [providers, setProviders] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  // Rejection Modal State
  const [rejectTarget, setRejectTarget] = useState(null);
  const [rejectionReason, setRejectionReason] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const fetchQueue = async () => {
    try {
      const data = await adminService.getPendingProviders();
      setProviders(data);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchQueue();
  }, []);

  const handleApprove = async (provider) => {
    setIsProcessing(true);
    try {
      await adminService.verifyProvider(provider.userId, 'APPROVE');
      setProviders((prev) => prev.filter((p) => p.userId !== provider.userId));
      notifySuccess(`Provider ${provider.fullName} approved and verified.`);
    } catch {
      notifyError('Failed to approve provider application.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleConfirmReject = async (e) => {
    e.preventDefault();
    if (!rejectionReason.trim()) {
      notifyError('Please provide a specific rejection justification reason.');
      return;
    }

    setIsProcessing(true);
    try {
      await adminService.verifyProvider(rejectTarget.userId, 'REJECT', rejectionReason);
      setProviders((prev) => prev.filter((p) => p.userId !== rejectTarget.userId));
      notifySuccess(`Provider application for ${rejectTarget.fullName} declined.`);
      setRejectTarget(null);
      setRejectionReason('');
    } catch {
      notifyError('Failed to record provider rejection.');
    } finally {
      setIsProcessing(false);
    }
  };

  const filtered = providers.filter((p) => {
    const term = searchTerm.toLowerCase();
    return (
      p.fullName.toLowerCase().includes(term) ||
      p.email.toLowerCase().includes(term) ||
      p.role.toLowerCase().includes(term) ||
      (p.licenseNumber && p.licenseNumber.toLowerCase().includes(term))
    );
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Retrieving pending provider applications..." />;
  }

  const getRoleIcon = (role) => {
    switch (role) {
      case 'DOCTOR':
        return <Stethoscope className="w-5 h-5 text-blue-600" />;
      case 'MEDICAL':
        return <Building className="w-5 h-5 text-purple-600" />;
      case 'LAB':
        return <FlaskConical className="w-5 h-5 text-cyan-600" />;
      default:
        return <ShieldCheck className="w-5 h-5 text-indigo-600" />;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Provider Verification Desk</h2>
          <p className="text-xs text-slate-500 mt-1">
            Validate physician medical licenses, hospital acute facility registrations, and clinical laboratory certifications
          </p>
        </div>
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Filter by name, license number, or role..."
        />
      </div>

      <div className="space-y-4">
        {filtered.map((provider) => (
          <div
            key={provider.userId}
            className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center shrink-0 border border-slate-200">
                {getRoleIcon(provider.role)}
              </div>

              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{provider.fullName}</h4>
                  <StatusBadge status="PENDING" size="sm" />
                  <span className="text-xs font-bold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200">
                    {provider.role}
                  </span>
                </div>
                <p className="text-xs text-slate-600 mt-1">
                  Email: <strong>{provider.email}</strong> • Phone: {provider.phoneNumber}
                </p>

                <div className="mt-2.5 p-3 rounded-xl bg-slate-50 border border-slate-200 grid grid-cols-2 sm:grid-cols-3 gap-3 text-xs">
                  <div>
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Official License</span>
                    <span className="font-mono font-bold text-slate-800">
                      {provider.licenseNumber || provider.facilityLicenseNumber || provider.labLicenseNumber}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Specialty / Scope</span>
                    <span className="font-semibold text-slate-800">
                      {provider.specialization || provider.organizationType || provider.accreditationBody}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">Submitted At</span>
                    <span className="text-slate-600">
                      {new Date(provider.submittedAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full md:w-auto justify-end border-t md:border-t-0 pt-3 md:pt-0">
              <Button
                variant="outline"
                size="sm"
                icon={X}
                className="text-rose-600 border-rose-200 hover:bg-rose-50"
                onClick={() => setRejectTarget(provider)}
                disabled={isProcessing}
              >
                Decline
              </Button>
              <Button
                variant="primary"
                size="sm"
                icon={Check}
                className="bg-emerald-600 hover:bg-emerald-500"
                onClick={() => handleApprove(provider)}
                disabled={isProcessing}
              >
                Approve Provider
              </Button>
            </div>
          </div>
        ))}

        {filtered.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No pending provider verification requests in queue.
          </div>
        )}
      </div>

      {/* Reject Justification Modal */}
      <Modal
        isOpen={!!rejectTarget}
        onClose={() => setRejectTarget(null)}
        title="Decline Provider Application"
        subtitle={`Specify official regulatory grounds for rejecting ${rejectTarget?.fullName}`}
        maxWidth="max-w-md"
      >
        <form onSubmit={handleConfirmReject} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-700 tracking-wide mb-1.5">
              Rejection Reason & Regulatory Grounds <span className="text-red-500">*</span>
            </label>
            <textarea
              rows={3}
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              placeholder="e.g. State medical license expired; facility registration could not be corroborated with health board records"
              className="w-full rounded-xl border border-slate-300 text-xs p-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
              required
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" onClick={() => setRejectTarget(null)}>
              Cancel
            </Button>
            <Button type="submit" variant="danger" isLoading={isProcessing}>
              Confirm Decline
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ProviderVerificationPage;

import React, { useState, useEffect } from 'react';
import { Calendar, Plus, Clock, User, CheckCircle2, XCircle, Stethoscope, Video, MapPin } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import appointmentService from '../../services/appointmentService';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import ConfirmationDialog from '../../components/common/ConfirmationDialog';
import FormInput from '../../components/common/FormInput';
import SelectInput from '../../components/common/SelectInput';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const AppointmentsPage = () => {
  const { user } = useAuth();
  const { notifySuccess, notifyError } = useNotification();
  const [appointments, setAppointments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');

  // Booking Modal State
  const [isBookModalOpen, setIsBookModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [newApt, setNewApt] = useState({
    doctorId: 'user-doc-1',
    doctorName: 'Dr. Marcus Sterling, MD',
    specialization: 'Cardiology',
    appointmentDate: '',
    appointmentType: 'IN_PERSON',
    reasonForVisit: '',
    notes: '',
  });

  // Cancel Dialog State
  const [cancelTarget, setCancelTarget] = useState(null);
  const [isCancelling, setIsCancelling] = useState(false);

  const fetchAppointments = async () => {
    try {
      const data = await appointmentService.getMyAppointments();
      setAppointments(data);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleDoctorChange = (e) => {
    const val = e.target.value;
    if (val === 'user-doc-1') {
      setNewApt((prev) => ({
        ...prev,
        doctorId: val,
        doctorName: 'Dr. Marcus Sterling, MD',
        specialization: 'Cardiology',
      }));
    } else if (val === 'user-doc-2') {
      setNewApt((prev) => ({
        ...prev,
        doctorId: val,
        doctorName: 'Dr. Evelyn Reed, MD',
        specialization: 'Endocrinology',
      }));
    } else {
      setNewApt((prev) => ({
        ...prev,
        doctorId: val,
        doctorName: 'Dr. Jonathan Blake, MD',
        specialization: 'Ophthalmology',
      }));
    }
  };

  const handleBookSubmit = async (e) => {
    e.preventDefault();
    if (!newApt.appointmentDate || !newApt.reasonForVisit) {
      notifyError('Please fill in appointment date and reason for visit.');
      return;
    }

    setIsSubmitting(true);
    try {
      const created = await appointmentService.createAppointment({
        patientUserId: user?.userId || 'user-pat-1',
        doctorUserId: newApt.doctorId,
        doctorName: newApt.doctorName,
        specialization: newApt.specialization,
        appointmentDate: newApt.appointmentDate,
        appointmentType: newApt.appointmentType,
        reasonForVisit: newApt.reasonForVisit,
        notes: newApt.notes,
      });
      setAppointments((prev) => [created, ...prev]);
      notifySuccess('Appointment successfully scheduled and confirmed!');
      setIsBookModalOpen(false);
    } catch {
      notifyError('Could not book appointment. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmCancel = async () => {
    if (!cancelTarget) return;
    setIsCancelling(true);
    try {
      await appointmentService.updateAppointmentStatus(cancelTarget.id, 'CANCELLED');
      setAppointments((prev) =>
        prev.map((a) => (a.id === cancelTarget.id ? { ...a, status: 'CANCELLED' } : a))
      );
      notifySuccess('Appointment cancelled successfully.');
      setCancelTarget(null);
    } finally {
      setIsCancelling(false);
    }
  };

  const filteredAppointments = appointments.filter((a) => {
    if (filter === 'ALL') return true;
    return a.status === filter;
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading appointment schedule..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Appointment Desk</h2>
          <p className="text-xs text-slate-500 mt-1">
            Book consultations, track status, and manage visits with your healthcare specialists
          </p>
        </div>

        <Button
          variant="primary"
          icon={Plus}
          onClick={() => setIsBookModalOpen(true)}
        >
          Book New Consultation
        </Button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-3 overflow-x-auto text-xs">
        {['ALL', 'CONFIRMED', 'SCHEDULED', 'COMPLETED', 'CANCELLED'].map((tab) => (
          <button
            key={tab}
            onClick={() => setFilter(tab)}
            className={`px-3 py-1.5 rounded-lg font-semibold transition-all whitespace-nowrap ${
              filter === tab
                ? 'bg-mediverse-600 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            {tab.replace('_', ' ')}
          </button>
        ))}
      </div>

      {/* Appointment Cards */}
      <div className="space-y-3">
        {filteredAppointments.map((apt) => (
          <div
            key={apt.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 border border-blue-100">
                {apt.appointmentType === 'TELEHEALTH' ? (
                  <Video className="w-6 h-6" />
                ) : (
                  <Calendar className="w-6 h-6" />
                )}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{apt.doctorName}</h4>
                  <StatusBadge status={apt.status} size="sm" />
                </div>
                <p className="text-xs text-slate-600 mt-0.5">
                  {apt.specialization} • <span className="font-medium text-slate-800">{apt.reasonForVisit}</span>
                </p>
                <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
                  <span className="flex items-center gap-1.5 font-medium text-slate-700">
                    <Clock className="w-3.5 h-3.5 text-mediverse-600" />
                    {new Date(apt.appointmentDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                  </span>
                  <span className="capitalize">{apt.appointmentType.replace('_', ' ').toLowerCase()} Encounter</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full md:w-auto justify-end border-t md:border-t-0 pt-3 md:pt-0">
              {(apt.status === 'CONFIRMED' || apt.status === 'SCHEDULED') && (
                <Button
                  variant="outline"
                  size="sm"
                  className="text-rose-600 border-rose-200 hover:bg-rose-50"
                  onClick={() => setCancelTarget(apt)}
                >
                  Cancel Visit
                </Button>
              )}
            </div>
          </div>
        ))}

        {filteredAppointments.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No appointments found in this category.
          </div>
        )}
      </div>

      {/* Book Appointment Modal */}
      <Modal
        isOpen={isBookModalOpen}
        onClose={() => setIsBookModalOpen(false)}
        title="Schedule Clinical Consultation"
        subtitle="Select a physician specialist and preferred appointment slot"
      >
        <form onSubmit={handleBookSubmit} className="space-y-4">
          <SelectInput
            label="Physician Specialist"
            name="doctorId"
            value={newApt.doctorId}
            onChange={handleDoctorChange}
            options={[
              { value: 'user-doc-1', label: 'Dr. Marcus Sterling, MD (Cardiology)' },
              { value: 'user-doc-2', label: 'Dr. Evelyn Reed, MD (Endocrinology)' },
              { value: 'user-doc-3', label: 'Dr. Jonathan Blake, MD (Ophthalmology)' },
            ]}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormInput
              label="Date & Time"
              name="appointmentDate"
              type="datetime-local"
              value={newApt.appointmentDate}
              onChange={(e) => setNewApt((prev) => ({ ...prev, appointmentDate: e.target.value }))}
              required
            />
            <SelectInput
              label="Encounter Type"
              name="appointmentType"
              value={newApt.appointmentType}
              onChange={(e) => setNewApt((prev) => ({ ...prev, appointmentType: e.target.value }))}
              options={[
                { value: 'IN_PERSON', label: 'In-Person Clinic Visit' },
                { value: 'TELEHEALTH', label: 'Telehealth Video Encounter' },
              ]}
            />
          </div>

          <FormInput
            label="Reason for Visit / Chief Complaint"
            name="reasonForVisit"
            placeholder="e.g. Routine blood pressure checkup, prescription renewal"
            value={newApt.reasonForVisit}
            onChange={(e) => setNewApt((prev) => ({ ...prev, reasonForVisit: e.target.value }))}
            required
          />

          <FormInput
            label="Patient Notes for Doctor (Optional)"
            name="notes"
            placeholder="Any recent symptoms or questions..."
            value={newApt.notes}
            onChange={(e) => setNewApt((prev) => ({ ...prev, notes: e.target.value }))}
          />

          <div className="mt-6 pt-4 border-t border-slate-100 flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setIsBookModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              Confirm Booking
            </Button>
          </div>
        </form>
      </Modal>

      {/* Cancel Confirmation Dialog */}
      <ConfirmationDialog
        isOpen={!!cancelTarget}
        onClose={() => setCancelTarget(null)}
        onConfirm={handleConfirmCancel}
        title="Cancel Appointment"
        message={`Are you sure you want to cancel your consultation with ${cancelTarget?.doctorName}? This slot will be released.`}
        confirmLabel="Yes, Cancel Visit"
        isDangerous
        isLoading={isCancelling}
      />
    </div>
  );
};

export default AppointmentsPage;

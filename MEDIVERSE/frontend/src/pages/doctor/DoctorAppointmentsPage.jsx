import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, User, CheckCircle2, XCircle, Stethoscope, Video, Search } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import doctorService from '../../services/doctorService';
import appointmentService from '../../services/appointmentService';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/dashboard/SearchBar';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const DoctorAppointmentsPage = () => {
  const { notifySuccess } = useNotification();
  const [appointments, setAppointments] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    doctorService.getDoctorAppointments().then((data) => {
      setAppointments(data);
      setIsLoading(false);
    });
  }, []);

  const handleUpdateStatus = async (appointmentId, newStatus) => {
    await appointmentService.updateAppointmentStatus(appointmentId, newStatus);
    setAppointments((prev) =>
      prev.map((a) => (a.id === appointmentId ? { ...a, status: newStatus } : a))
    );
    notifySuccess(`Appointment marked as ${newStatus}.`);
  };

  const filtered = appointments.filter((apt) => {
    const matchesFilter = filter === 'ALL' || apt.status === filter;
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      apt.patientName.toLowerCase().includes(term) ||
      apt.healthId.toLowerCase().includes(term) ||
      apt.reasonForVisit.toLowerCase().includes(term);
    return matchesFilter && matchesSearch;
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading physician appointments..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Physician Appointment Desk</h2>
          <p className="text-xs text-slate-500 mt-1">
            Review patient clinic visits, telemedicine sessions, and update encounter attendance
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div className="max-w-md w-full">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search by patient name, Health ID, reason..."
          />
        </div>

        {/* Status Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto text-xs">
          {['ALL', 'CONFIRMED', 'SCHEDULED', 'COMPLETED', 'CANCELLED'].map((tab) => (
            <button
              key={tab}
              onClick={() => setFilter(tab)}
              className={`px-3 py-1.5 rounded-lg font-semibold transition-all whitespace-nowrap ${
                filter === tab
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {tab.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Appointment Cards */}
      <div className="space-y-3">
        {filtered.map((apt) => (
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
                  <h4 className="text-base font-bold text-slate-900">{apt.patientName}</h4>
                  <span className="font-mono text-xs font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-200">
                    {apt.healthId}
                  </span>
                  <StatusBadge status={apt.status} size="sm" />
                </div>
                <p className="text-xs text-slate-600 mt-1">
                  Reason for visit: <strong className="text-slate-800">{apt.reasonForVisit}</strong>
                </p>
                <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
                  <span className="flex items-center gap-1.5 font-medium text-slate-700">
                    <Clock className="w-3.5 h-3.5 text-blue-600" />
                    {new Date(apt.appointmentDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                  </span>
                  <span className="capitalize">{apt.appointmentType.replace('_', ' ').toLowerCase()} Session</span>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2 w-full md:w-auto justify-end border-t md:border-t-0 pt-3 md:pt-0">
              {(apt.status === 'CONFIRMED' || apt.status === 'SCHEDULED') && (
                <>
                  <Link
                    to={`/doctor/consultations?aptId=${apt.id}&healthId=${apt.healthId}&patientName=${encodeURIComponent(apt.patientName)}`}
                  >
                    <Button variant="primary" size="sm" icon={Stethoscope} className="bg-blue-600 hover:bg-blue-500">
                      Record Consultation
                    </Button>
                  </Link>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleUpdateStatus(apt.id, 'COMPLETED')}
                  >
                    Mark Completed
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-rose-600 hover:bg-rose-50"
                    onClick={() => handleUpdateStatus(apt.id, 'CANCELLED')}
                  >
                    Cancel
                  </Button>
                </>
              )}
            </div>
          </div>
        ))}

        {filtered.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No appointments found.
          </div>
        )}
      </div>
    </div>
  );
};

export default DoctorAppointmentsPage;

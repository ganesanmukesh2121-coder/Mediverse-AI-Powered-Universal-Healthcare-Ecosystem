import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Stethoscope,
  Calendar,
  Users,
  Pill,
  Clock,
  Plus,
  Search,
  ArrowRight,
  Activity,
  CheckCircle,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import doctorService from '../../services/doctorService';
import DashboardCard from '../../components/dashboard/DashboardCard';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const DoctorDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [patients, setPatients] = useState([]);
  const [searchHealthId, setSearchHealthId] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [aptData, patData] = await Promise.all([
          doctorService.getDoctorAppointments(),
          doctorService.getAuthorizedPatients(),
        ]);
        setAppointments(aptData);
        setPatients(patData);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const handlePatientLookup = (e) => {
    e.preventDefault();
    if (!searchHealthId.trim()) return;
    navigate(`/doctor/lookup?id=${encodeURIComponent(searchHealthId.trim())}`);
  };

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading physician clinical console..." />;
  }

  const todayAppointments = appointments.filter((a) => a.status === 'CONFIRMED' || a.status === 'SCHEDULED');

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-blue-900 via-indigo-950 to-navy-950 p-6 rounded-2xl text-white shadow-lg border border-blue-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase font-bold tracking-wider text-blue-300">
            Physician Clinical Portal • Clinical Duty
          </span>
          <h2 className="text-2xl font-bold tracking-tight mt-0.5">
            {user?.fullName || 'Dr. Marcus Sterling, MD'}
          </h2>
          <p className="text-xs text-blue-200/80 mt-1">
            Cardiology & Internal Medicine • License: MD-CAL-884920 (Verified)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link to="/doctor/consultations">
            <Button variant="primary" icon={Stethoscope} size="sm" className="bg-blue-600 hover:bg-blue-500">
              New Consultation
            </Button>
          </Link>
          <Link to="/doctor/prescriptions">
            <Button variant="secondary" icon={Pill} size="sm" className="bg-white/10 text-white hover:bg-white/20 border-white/20">
              Write Rx
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard
          title="Today's Appointments"
          value={todayAppointments.length}
          subtitle="3 scheduled visits"
          icon={Calendar}
          colorScheme="blue"
        />
        <DashboardCard
          title="Authorized Patients"
          value={patients.length}
          subtitle="Active patient consents"
          icon={Users}
          colorScheme="emerald"
        />
        <DashboardCard
          title="Consultations (Month)"
          value="48"
          subtitle="All encounters documented"
          icon={Activity}
          colorScheme="purple"
        />
        <DashboardCard
          title="Prescriptions Issued"
          value="92"
          subtitle="100% digital e-Prescriptions"
          icon={Pill}
          colorScheme="amber"
        />
      </div>

      {/* Quick Patient Lookup Bar */}
      <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center gap-2 mb-2">
          <Search className="w-4 h-4 text-blue-600" />
          <h3 className="text-sm font-bold text-slate-900">Direct Patient Health ID Lookup</h3>
        </div>
        <p className="text-xs text-slate-500 mb-3">
          Query patient clinical history, verify active consent category, or initiate walk-in consultation
        </p>

        <form onSubmit={handlePatientLookup} className="flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            value={searchHealthId}
            onChange={(e) => setSearchHealthId(e.target.value)}
            placeholder="e.g. MED-10293847-P"
            className="flex-1 px-3.5 py-2 rounded-xl border border-slate-300 text-xs font-mono uppercase font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
          />
          <Button type="submit" variant="primary" icon={Search} size="sm" className="bg-blue-600 hover:bg-blue-500">
            Lookup Patient
          </Button>
        </form>
      </div>

      {/* Schedule Table */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
        <div className="flex items-center justify-between pb-4 border-b border-slate-100">
          <div>
            <h3 className="text-base font-bold text-slate-900">Today's Clinical Appointment Schedule</h3>
            <p className="text-xs text-slate-500">Confirmed clinic consultations and telemedicine slots</p>
          </div>
          <Link to="/doctor/appointments">
            <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
              All Appointments
            </Button>
          </Link>
        </div>

        <div className="divide-y divide-slate-100 mt-2">
          {appointments.slice(0, 4).map((apt) => (
            <div key={apt.id} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
              <div className="flex items-start gap-3">
                <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 border border-blue-100">
                  <Calendar className="w-5 h-5" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h5 className="font-bold text-slate-900 text-sm">{apt.patientName}</h5>
                    <span className="font-mono text-[11px] text-blue-700 bg-blue-50 px-2 py-0.5 rounded">
                      {apt.healthId}
                    </span>
                    <StatusBadge status={apt.status} size="sm" />
                  </div>
                  <p className="text-slate-500 mt-0.5">
                    Reason: <strong className="text-slate-700">{apt.reasonForVisit}</strong>
                  </p>
                  <span className="text-slate-400 mt-1 block flex items-center gap-1">
                    <Clock className="w-3 h-3 text-slate-400" />
                    {new Date(apt.appointmentDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })} •{' '}
                    <span className="capitalize">{apt.appointmentType.replace('_', ' ').toLowerCase()}</span>
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Link to={`/doctor/consultations?aptId=${apt.id}&healthId=${apt.healthId}&patientName=${encodeURIComponent(apt.patientName)}`}>
                  <Button variant="primary" size="sm" icon={Stethoscope} className="bg-blue-600 hover:bg-blue-500">
                    Start Encounter
                  </Button>
                </Link>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default DoctorDashboard;

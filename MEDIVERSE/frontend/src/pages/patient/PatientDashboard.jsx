import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Calendar,
  Pill,
  FlaskConical,
  ShieldCheck,
  Plus,
  ArrowRight,
  Clock,
  HeartPulse,
  Activity,
  FileText,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import patientService from '../../services/patientService';
import appointmentService from '../../services/appointmentService';
import HealthIdCard from '../../components/dashboard/HealthIdCard';
import DashboardCard from '../../components/dashboard/DashboardCard';
import StatusBadge from '../../components/common/StatusBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const PatientDashboard = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [labReports, setLabReports] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [profData, aptData, rxData, labData] = await Promise.all([
          patientService.getProfile(),
          appointmentService.getMyAppointments(),
          patientService.getPrescriptions(),
          patientService.getLabReports(),
        ]);
        setProfile(profData);
        setAppointments(aptData);
        setPrescriptions(rxData);
        setLabReports(labData);
      } catch (err) {
        console.error('Failed to load patient dashboard data:', err);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading patient health dashboard..." />;
  }

  const upcomingAppointments = appointments.filter((a) => a.status === 'SCHEDULED' || a.status === 'CONFIRMED');
  const activePrescriptions = prescriptions.filter((p) => p.status === 'ACTIVE');

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 bg-gradient-to-r from-mediverse-900 to-navy-950 p-6 rounded-2xl text-white shadow-lg border border-mediverse-800">
        <div>
          <span className="text-xs uppercase font-bold tracking-wider text-mediverse-300">
            Patient Portal • Welcome Back
          </span>
          <h2 className="text-2xl font-bold tracking-tight mt-0.5">
            {profile?.fullName || user?.fullName || 'Sarah Jenkins'}
          </h2>
          <p className="text-xs text-slate-300 mt-1">
            Universal Health ID:{' '}
            <span className="font-mono font-bold text-emerald-300">
              {profile?.healthId || user?.healthId || 'MED-10293847-P'}
            </span>
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Link to="/patient/appointments">
            <Button variant="primary" icon={Plus} size="sm" className="bg-emerald-600 hover:bg-emerald-500">
              Book Appointment
            </Button>
          </Link>
          <Link to="/patient/emergency">
            <Button variant="danger" icon={HeartPulse} size="sm">
              Emergency ICE
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <DashboardCard
          title="Upcoming Visits"
          value={upcomingAppointments.length}
          subtitle="Next on Nov 12, 2026"
          icon={Calendar}
          colorScheme="blue"
        />
        <DashboardCard
          title="Active Medications"
          value={activePrescriptions.reduce((acc, p) => acc + (p.items?.length || 0), 0)}
          subtitle="2 prescriptions current"
          icon={Pill}
          colorScheme="emerald"
        />
        <DashboardCard
          title="Diagnostic Tests"
          value={labReports.length}
          subtitle="Latest from Oct 23"
          icon={FlaskConical}
          colorScheme="purple"
        />
        <DashboardCard
          title="Provider Consents"
          value="2 Active"
          subtitle="Controlled by you"
          icon={ShieldCheck}
          colorScheme="amber"
        />
      </div>

      {/* Main Grid: Health ID & Upcoming Appointments */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Universal Health ID Card Component */}
        <div className="lg:col-span-1 space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-800 uppercase tracking-wider">Digital Health ID</h3>
            <Link to="/patient/health-id" className="text-xs text-mediverse-600 font-semibold hover:underline">
              Full Card
            </Link>
          </div>
          <HealthIdCard
            healthId={profile?.healthId || 'MED-10293847-P'}
            patientName={profile?.fullName || 'Sarah Jenkins'}
            dateOfBirth={profile?.dateOfBirth || '1988-04-12'}
            bloodGroup="O+"
            emergencyContact={profile?.emergencyContact?.phone || '+1-555-019-2835'}
          />
        </div>

        {/* Upcoming Appointments Table / Card */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-200 p-6 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <div>
                <h3 className="text-base font-bold text-slate-900">Upcoming Appointments</h3>
                <p className="text-xs text-slate-500">Scheduled clinical consultations and reviews</p>
              </div>
              <Link to="/patient/appointments">
                <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
                  View All
                </Button>
              </Link>
            </div>

            <div className="divide-y divide-slate-100 mt-2">
              {upcomingAppointments.length > 0 ? (
                upcomingAppointments.slice(0, 3).map((apt) => (
                  <div key={apt.id} className="py-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div className="flex items-start gap-3">
                      <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 border border-blue-100">
                        <Calendar className="w-5 h-5" />
                      </div>
                      <div>
                        <h4 className="text-sm font-bold text-slate-900">{apt.doctorName}</h4>
                        <p className="text-xs text-slate-500">{apt.specialization} • {apt.reasonForVisit}</p>
                        <span className="text-[11px] text-slate-400 mt-0.5 block flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          {new Date(apt.appointmentDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                        </span>
                      </div>
                    </div>
                    <div className="flex sm:flex-col items-center sm:items-end justify-between gap-1">
                      <StatusBadge status={apt.status} />
                      <span className="text-[11px] font-medium text-slate-500 capitalize">{apt.appointmentType.replace('_', ' ').toLowerCase()}</span>
                    </div>
                  </div>
                ))
              ) : (
                <div className="py-8 text-center text-xs text-slate-400">No upcoming appointments scheduled</div>
              )}
            </div>
          </div>

          <div className="pt-4 border-t border-slate-100 mt-4 flex items-center justify-between text-xs text-slate-500">
            <span>Need to reschedule? Contact your provider directly or cancel online.</span>
          </div>
        </div>
      </div>

      {/* Bottom Grid: Recent Prescriptions & Lab Reports */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Active Prescriptions */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <h3 className="text-base font-bold text-slate-900">Active Prescriptions</h3>
              <p className="text-xs text-slate-500">Current therapies and medication regimens</p>
            </div>
            <Link to="/patient/prescriptions">
              <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
                View All
              </Button>
            </Link>
          </div>

          <div className="divide-y divide-slate-100 mt-2">
            {prescriptions.flatMap((p) => p.items || []).slice(0, 3).map((item) => (
              <div key={item.id} className="py-3 flex items-start justify-between gap-3">
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0 border border-emerald-100">
                    <Pill className="w-4 h-4" />
                  </div>
                  <div>
                    <h5 className="text-xs font-bold text-slate-900">{item.medicationName} ({item.dosage})</h5>
                    <p className="text-[11px] text-slate-500">{item.frequency} • {item.duration}</p>
                    <p className="text-[10px] text-slate-400 mt-0.5">{item.instructions}</p>
                  </div>
                </div>
                <StatusBadge status={item.dispensingStatus || 'DISPENSED'} size="sm" />
              </div>
            ))}
          </div>
        </div>

        {/* Recent Lab Reports */}
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs">
          <div className="flex items-center justify-between pb-4 border-b border-slate-100">
            <div>
              <h3 className="text-base font-bold text-slate-900">Recent Diagnostic Tests</h3>
              <p className="text-xs text-slate-500">Verified laboratory and pathology results</p>
            </div>
            <Link to="/patient/lab-reports">
              <Button variant="ghost" size="sm" icon={ArrowRight} iconPosition="right">
                View All
              </Button>
            </Link>
          </div>

          <div className="divide-y divide-slate-100 mt-2">
            {labReports.slice(0, 2).map((report) => (
              <div key={report.id} className="py-3 flex items-start justify-between gap-3">
                <div className="flex items-start gap-3">
                  <div className="w-8 h-8 rounded-lg bg-cyan-50 text-cyan-600 flex items-center justify-center shrink-0 border border-cyan-100">
                    <FlaskConical className="w-4 h-4" />
                  </div>
                  <div>
                    <h5 className="text-xs font-bold text-slate-900">{report.testName}</h5>
                    <p className="text-[11px] text-slate-500">{report.labFacility}</p>
                    <span className="text-[10px] text-slate-400 mt-0.5 block">
                      Reported: {new Date(report.reportedDate).toLocaleDateString()}
                    </span>
                  </div>
                </div>
                <StatusBadge status={report.overallStatus} size="sm" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientDashboard;

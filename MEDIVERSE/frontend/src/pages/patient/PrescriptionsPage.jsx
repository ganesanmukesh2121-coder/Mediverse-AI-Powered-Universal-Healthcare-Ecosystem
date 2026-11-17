import React, { useState, useEffect } from 'react';
import { Pill, Calendar, Clock, User, CheckCircle2, AlertCircle, Printer, Search } from 'lucide-react';
import patientService from '../../services/patientService';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/dashboard/SearchBar';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const PrescriptionsPage = () => {
  const [prescriptions, setPrescriptions] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    patientService.getPrescriptions().then((data) => {
      setPrescriptions(data);
      setIsLoading(false);
    });
  }, []);

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading prescriptions..." />;
  }

  const allItems = prescriptions.flatMap((rx) =>
    (rx.items || []).map((item) => ({
      ...item,
      doctorName: rx.doctorName,
      prescribedDate: rx.prescribedDate,
      rxStatus: rx.status,
    }))
  );

  const filteredItems = allItems.filter((item) => {
    const term = searchTerm.toLowerCase();
    return (
      item.medicationName.toLowerCase().includes(term) ||
      item.doctorName.toLowerCase().includes(term) ||
      item.instructions.toLowerCase().includes(term)
    );
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Digital Prescriptions</h2>
          <p className="text-xs text-slate-500 mt-1">
            Active and historical pharmacological regimens prescribed by authenticated physicians
          </p>
        </div>

        <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
          Print Medication Schedule
        </Button>
      </div>

      <div className="max-w-md">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Search by drug name, doctor, or instruction..."
        />
      </div>

      <div className="space-y-4">
        {filteredItems.map((item) => (
          <div
            key={item.id}
            className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs hover:border-emerald-300 transition-all"
          >
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100 shrink-0">
                  <Pill className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-base font-bold text-slate-900">
                    {item.medicationName}{' '}
                    <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                      {item.dosage}
                    </span>
                  </h4>
                  <p className="text-xs text-slate-500">
                    Prescribed by {item.doctorName} • Date: {item.prescribedDate}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <StatusBadge status={item.dispensingStatus || 'DISPENSED'} />
              </div>
            </div>

            <div className="mt-4 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Frequency</span>
                <span className="font-semibold text-slate-800 mt-0.5 block">{item.frequency}</span>
              </div>

              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Route</span>
                <span className="font-semibold text-slate-800 mt-0.5 block">{item.route}</span>
              </div>

              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Duration</span>
                <span className="font-semibold text-slate-800 mt-0.5 block">{item.duration}</span>
              </div>

              <div className="p-3 bg-slate-50 rounded-xl border border-slate-100">
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Refills Left</span>
                <span className="font-semibold text-slate-800 mt-0.5 block">{item.refillsRemaining} Refills</span>
              </div>
            </div>

            <div className="mt-3 p-3 bg-emerald-50/40 rounded-xl border border-emerald-100 text-xs text-slate-700">
              <strong className="text-emerald-900">Pharmacy Instructions: </strong>
              {item.instructions}
            </div>
          </div>
        ))}

        {filteredItems.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No medication orders found.
          </div>
        )}
      </div>
    </div>
  );
};

export default PrescriptionsPage;

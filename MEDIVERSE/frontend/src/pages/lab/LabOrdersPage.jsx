import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FlaskConical, Search, Clock, CheckCircle2, ArrowRight } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import labService from '../../services/labService';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/dashboard/SearchBar';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const LabOrdersPage = () => {
  const { notifySuccess } = useNotification();
  const [orders, setOrders] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    labService.getTestOrders().then((data) => {
      setOrders(data);
      setIsLoading(false);
    });
  }, []);

  const handleStatusChange = async (orderId, newStatus) => {
    await labService.updateOrderStatus(orderId, newStatus);
    setOrders((prev) =>
      prev.map((o) => (o.id === orderId ? { ...o, status: newStatus } : o))
    );
    notifySuccess(`Order status updated to ${newStatus}.`);
  };

  const filtered = orders.filter((o) => {
    const matchesFilter = filter === 'ALL' || o.status === filter;
    const term = searchTerm.toLowerCase();
    const matchesSearch =
      o.patientName.toLowerCase().includes(term) ||
      o.healthId.toLowerCase().includes(term) ||
      o.orderNumber.toLowerCase().includes(term) ||
      o.testType.toLowerCase().includes(term);
    return matchesFilter && matchesSearch;
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading laboratory orders..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Test Requisitions Worklist</h2>
            <DemoDataBadge label="LIS Processing Preview" />
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Monitor incoming requisitions, manage specimen accessioning, and progress laboratory workflow
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div className="max-w-md w-full">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search by order ID, patient, test type..."
          />
        </div>

        {/* Filter Tabs */}
        <div className="flex items-center gap-1.5 overflow-x-auto text-xs">
          {['ALL', 'PENDING_COLLECTION', 'IN_PROGRESS', 'COMPLETED'].map((tab) => (
            <button
              key={tab}
              onClick={() => setFilter(tab)}
              className={`px-3 py-1.5 rounded-lg font-semibold transition-all whitespace-nowrap ${
                filter === tab
                  ? 'bg-cyan-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {tab.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {/* Orders List */}
      <div className="space-y-3">
        {filtered.map((ord) => (
          <div
            key={ord.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-cyan-50 text-cyan-600 flex items-center justify-center shrink-0 border border-cyan-100">
                <FlaskConical className="w-6 h-6" />
              </div>

              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{ord.testType}</h4>
                  <span className="font-mono text-xs font-bold text-cyan-700 bg-cyan-50 px-2 py-0.5 rounded border border-cyan-200">
                    {ord.orderNumber}
                  </span>
                  <StatusBadge status={ord.status} size="sm" />
                  <StatusBadge status={ord.priority} size="sm" />
                </div>
                <p className="text-xs text-slate-600 mt-1">
                  Patient: <strong className="text-slate-800">{ord.patientName}</strong> ({ord.healthId}) • Ordered by: {ord.orderedBy}
                </p>
                <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
                  <span>Sample: <strong>{ord.sampleType}</strong></span>
                  <span>Date: {new Date(ord.orderedDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}</span>
                </div>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-2 w-full md:w-auto justify-end border-t md:border-t-0 pt-3 md:pt-0">
              {ord.status === 'PENDING_COLLECTION' && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleStatusChange(ord.id, 'IN_PROGRESS')}
                >
                  Accession Sample
                </Button>
              )}
              {ord.status === 'IN_PROGRESS' && (
                <Link to={`/lab/reports?orderId=${ord.orderNumber}&healthId=${ord.healthId}&testName=${encodeURIComponent(ord.testType)}`}>
                  <Button variant="primary" size="sm" className="bg-cyan-600 hover:bg-cyan-500 text-xs">
                    Input Results
                  </Button>
                </Link>
              )}
            </div>
          </div>
        ))}

        {filtered.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No laboratory orders matching your query.
          </div>
        )}
      </div>
    </div>
  );
};

export default LabOrdersPage;

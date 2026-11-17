import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FlaskConical, Clock, Activity } from 'lucide-react';
import labService from '../../services/labService';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const LabInProgressPage = () => {
  const [inProgressOrders, setInProgressOrders] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    labService.getTestOrders().then((data) => {
      setInProgressOrders(data.filter((o) => o.status === 'IN_PROGRESS'));
      setIsLoading(false);
    });
  }, []);

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading active bench analysis..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Active Bench Analysis (In-Progress)</h2>
          <p className="text-xs text-slate-500 mt-1">
            Specimens currently accessioned on laboratory analyzers awaiting quantitative verification
          </p>
        </div>
        <DemoDataBadge label="LIS Processing Preview" />
      </div>

      <div className="space-y-3">
        {inProgressOrders.map((ord) => (
          <div
            key={ord.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0 border border-blue-100">
                <FlaskConical className="w-6 h-6 animate-pulse" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{ord.testType}</h4>
                  <span className="font-mono text-xs font-bold text-cyan-700 bg-cyan-50 px-2 py-0.5 rounded">
                    {ord.orderNumber}
                  </span>
                  <StatusBadge status="IN_PROGRESS" size="sm" />
                </div>
                <p className="text-xs text-slate-600 mt-0.5">
                  Patient: <strong className="text-slate-800">{ord.patientName}</strong> ({ord.healthId}) • Ordered by: {ord.orderedBy}
                </p>
                <span className="text-slate-400 text-xs mt-1 block">Sample: {ord.sampleType}</span>
              </div>
            </div>

            <Link to={`/lab/reports?orderId=${ord.orderNumber}&healthId=${ord.healthId}&testName=${encodeURIComponent(ord.testType)}`}>
              <Button variant="primary" size="sm" className="bg-cyan-600 hover:bg-cyan-500">
                Finalize Results
              </Button>
            </Link>
          </div>
        ))}

        {inProgressOrders.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No specimens currently in analysis.
          </div>
        )}
      </div>
    </div>
  );
};

export default LabInProgressPage;

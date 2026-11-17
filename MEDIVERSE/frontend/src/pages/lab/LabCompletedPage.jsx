import React, { useState, useEffect } from 'react';
import { FlaskConical, CheckCircle2, Eye, Printer } from 'lucide-react';
import labService from '../../services/labService';
import StatusBadge from '../../components/common/StatusBadge';
import DemoDataBadge from '../../components/common/DemoDataBadge';
import Modal from '../../components/common/Modal';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const LabCompletedPage = () => {
  const [reports, setReports] = useState([]);
  const [selectedReport, setSelectedReport] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    labService.getCompletedReports().then((data) => {
      setReports(data);
      setIsLoading(false);
    });
  }, []);

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading completed test reports..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Verified Diagnostic Reports Archive</h2>
          <p className="text-xs text-slate-500 mt-1">
            Historical laboratory and pathology reports published and certified by laboratory directors
          </p>
        </div>
        <DemoDataBadge label="LIS Processing Preview" />
      </div>

      <div className="space-y-3">
        {reports.map((report) => (
          <div
            key={report.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0 border border-emerald-100">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{report.testName}</h4>
                  <span className="font-mono text-xs font-bold text-cyan-700 bg-cyan-50 px-2 py-0.5 rounded">
                    {report.orderId}
                  </span>
                  <StatusBadge status="COMPLETED" size="sm" />
                </div>
                <p className="text-xs text-slate-600 mt-0.5">
                  Ordering Doctor: <strong className="text-slate-800">{report.orderingDoctor}</strong> • Facility: {report.labFacility}
                </p>
                <span className="text-slate-400 text-xs mt-1 block">
                  Reported on: {new Date(report.reportedDate).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })}
                </span>
              </div>
            </div>

            <Button
              variant="outline"
              size="sm"
              icon={Eye}
              onClick={() => setSelectedReport(report)}
            >
              Inspect Parameters
            </Button>
          </div>
        ))}
      </div>

      {/* Inspect Modal */}
      <Modal
        isOpen={!!selectedReport}
        onClose={() => setSelectedReport(null)}
        title={selectedReport?.testName}
        subtitle={`Order ${selectedReport?.orderId} • Certified Report`}
        maxWidth="max-w-2xl"
      >
        {selectedReport && (
          <div className="space-y-4 text-xs">
            <div className="border border-slate-200 rounded-xl overflow-hidden">
              <table className="w-full text-left">
                <thead className="bg-slate-50 text-slate-500 font-bold uppercase text-[10px]">
                  <tr>
                    <th className="p-3">Parameter</th>
                    <th className="p-3">Result</th>
                    <th className="p-3">Reference Range</th>
                    <th className="p-3">Flag</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {selectedReport.results?.map((r, i) => (
                    <tr key={i}>
                      <td className="p-3 font-semibold text-slate-800">{r.parameter}</td>
                      <td className="p-3 font-mono font-bold">{r.value} {r.unit}</td>
                      <td className="p-3 text-slate-500">{r.referenceRange}</td>
                      <td className="p-3"><StatusBadge status={r.flag} size="sm" /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
                Print Report
              </Button>
              <Button variant="primary" size="sm" onClick={() => setSelectedReport(null)}>
                Close
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default LabCompletedPage;

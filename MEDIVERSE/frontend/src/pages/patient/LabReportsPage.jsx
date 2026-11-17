import React, { useState, useEffect } from 'react';
import { FlaskConical, Calendar, Building2, Eye, Download, Printer, CheckCircle, AlertTriangle } from 'lucide-react';
import patientService from '../../services/patientService';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const LabReportsPage = () => {
  const [reports, setReports] = useState([]);
  const [selectedReport, setSelectedReport] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    patientService.getLabReports().then((data) => {
      setReports(data);
      setIsLoading(false);
    });
  }, []);

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading diagnostic reports..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Diagnostic Lab Reports</h2>
          <p className="text-xs text-slate-500 mt-1">
            Pathology results, biomarker panels, and verified diagnostic laboratory findings
          </p>
        </div>
      </div>

      <div className="space-y-4">
        {reports.map((report) => (
          <div
            key={report.id}
            className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs hover:border-cyan-300 transition-all"
          >
            <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-cyan-50 text-cyan-600 flex items-center justify-center border border-cyan-100 shrink-0">
                  <FlaskConical className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-base font-bold text-slate-900">{report.testName}</h4>
                  <p className="text-xs text-slate-500">
                    Ordered by {report.orderingDoctor} • {report.labFacility}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <StatusBadge status={report.overallStatus} />
                <Button
                  variant="outline"
                  size="sm"
                  icon={Eye}
                  onClick={() => setSelectedReport(report)}
                >
                  View Parameters
                </Button>
              </div>
            </div>

            <div className="mt-4 flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs text-slate-600">
              <div className="flex items-center gap-4">
                <span>
                  <strong>Collected:</strong> {new Date(report.collectedDate).toLocaleDateString()}
                </span>
                <span>
                  <strong>Reported:</strong> {new Date(report.reportedDate).toLocaleDateString()}
                </span>
                <span>
                  <strong>Requisition ID:</strong> <span className="font-mono">{report.orderId}</span>
                </span>
              </div>
              <span className="text-slate-500 font-medium">{report.results?.length || 0} Analyzed Parameters</span>
            </div>

            <div className="mt-3 p-3 bg-slate-50 rounded-xl border border-slate-100 text-xs text-slate-700">
              <strong className="text-slate-900">Interpretation Summary: </strong>
              {report.clinicalInterpretation}
            </div>
          </div>
        ))}
      </div>

      {/* Lab Report Parameters Modal */}
      <Modal
        isOpen={!!selectedReport}
        onClose={() => setSelectedReport(null)}
        title={selectedReport?.testName}
        subtitle={`${selectedReport?.labFacility} • Order ${selectedReport?.orderId}`}
        maxWidth="max-w-3xl"
      >
        {selectedReport && (
          <div className="space-y-5 text-xs">
            <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex flex-wrap items-center justify-between gap-3 text-slate-600">
              <div>
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Ordering Physician</span>
                <span className="font-semibold text-slate-800">{selectedReport.orderingDoctor}</span>
              </div>
              <div>
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Specimen Collection</span>
                <span className="font-semibold text-slate-800">
                  {new Date(selectedReport.collectedDate).toLocaleString()}
                </span>
              </div>
              <div>
                <span className="text-[10px] uppercase font-bold text-slate-400 block">Verification Status</span>
                <span className="font-bold text-emerald-600 flex items-center gap-1">
                  <CheckCircle className="w-3.5 h-3.5" /> CLIA Certified
                </span>
              </div>
            </div>

            {/* Results Table */}
            <div className="border border-slate-200 rounded-xl overflow-hidden shadow-2xs">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50 border-b border-slate-200 text-[11px] font-bold text-slate-600 uppercase tracking-wider">
                    <th className="px-4 py-2.5">Parameter Tested</th>
                    <th className="px-4 py-2.5">Measured Value</th>
                    <th className="px-4 py-2.5">Unit</th>
                    <th className="px-4 py-2.5">Standard Reference Range</th>
                    <th className="px-4 py-2.5 text-center">Diagnostic Flag</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {selectedReport.results?.map((res, idx) => (
                    <tr key={idx} className="hover:bg-slate-50/60">
                      <td className="px-4 py-3 font-semibold text-slate-800">{res.parameter}</td>
                      <td className="px-4 py-3 font-bold text-slate-900 font-mono">{res.value}</td>
                      <td className="px-4 py-3 text-slate-500">{res.unit}</td>
                      <td className="px-4 py-3 text-slate-600">{res.referenceRange}</td>
                      <td className="px-4 py-3 text-center">
                        <StatusBadge status={res.flag} size="sm" />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="p-3.5 bg-cyan-50/60 rounded-xl border border-cyan-200 text-slate-800">
              <span className="font-bold text-cyan-950 uppercase tracking-wider text-[11px] block mb-1">
                Clinical Pathologist Notes
              </span>
              <p className="leading-relaxed">{selectedReport.clinicalInterpretation}</p>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <Button variant="secondary" size="sm" icon={Printer} onClick={() => window.print()}>
                Print Official Report
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

export default LabReportsPage;

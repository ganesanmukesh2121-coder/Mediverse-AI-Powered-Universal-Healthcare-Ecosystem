import React, { useState, useEffect } from 'react';
import { ShieldCheck, Download, Printer, Copy, Check, QrCode, AlertTriangle, Droplet, User, Phone, MapPin } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNotification } from '../../context/NotificationContext';
import patientService from '../../services/patientService';
import HealthIdCard from '../../components/dashboard/HealthIdCard';
import Button from '../../components/common/Button';

export const HealthIdPage = () => {
  const { user } = useAuth();
  const { notifySuccess } = useNotification();
  const [profile, setProfile] = useState(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    patientService.getProfile().then(setProfile);
  }, []);

  const healthId = profile?.healthId || user?.healthId || 'MED-10293847-P';

  const handleCopy = () => {
    navigator.clipboard.writeText(healthId);
    setCopied(true);
    notifySuccess('Universal Health ID copied to clipboard');
    setTimeout(() => setCopied(false), 2000);
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Universal Health ID Card</h2>
          <p className="text-xs text-slate-500 mt-1">
            Your tamper-evident digital identity credential across the Mediverse Universal Healthcare Network
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" icon={Printer} onClick={handlePrint}>
            Print Card
          </Button>
          <Button variant="outline" size="sm" icon={copied ? Check : Copy} onClick={handleCopy}>
            {copied ? 'Copied' : 'Copy Health ID'}
          </Button>
        </div>
      </div>

      {/* Main Digital Card Preview */}
      <div className="max-w-xl mx-auto">
        <HealthIdCard
          healthId={healthId}
          patientName={profile?.fullName || 'Sarah Jenkins'}
          dateOfBirth={profile?.dateOfBirth || '1988-04-12'}
          bloodGroup="O+"
          emergencyContact={profile?.emergencyContact?.phone || '+1-555-019-2835'}
        />
      </div>

      {/* Security & Health ID Details */}
      <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-xs space-y-6">
        <div>
          <h3 className="text-base font-bold text-slate-900">Health ID Specifications & Rights</h3>
          <p className="text-xs text-slate-500 mt-0.5">
            Key identifiers tied to your cryptographic patient record
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 text-xs">
          <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-semibold text-slate-500 uppercase block">Identifier Type</span>
            <span className="font-bold text-slate-900 mt-1 block">National Universal Health ID (UHID)</span>
            <span className="text-[10px] text-slate-400 mt-0.5 block">Format: MED-XXXXXXXX-P</span>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-semibold text-slate-500 uppercase block">Data Sovereign Authority</span>
            <span className="font-bold text-emerald-700 mt-1 block">Patient-Governed Consent</span>
            <span className="text-[10px] text-slate-400 mt-0.5 block">Only authorized providers access data</span>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200">
            <span className="text-[11px] font-semibold text-slate-500 uppercase block">Emergency ICE Gateway</span>
            <span className="font-bold text-amber-700 mt-1 block">Triage-Restricted Protocol</span>
            <span className="text-[10px] text-slate-400 mt-0.5 block">Audited 100% with justification</span>
          </div>
        </div>

        <div className="p-4 rounded-xl bg-emerald-50/60 border border-emerald-200 text-xs text-emerald-950 space-y-2">
          <div className="flex items-center gap-2 font-bold text-emerald-900">
            <ShieldCheck className="w-4 h-4 text-emerald-600" />
            <span>How to use your Universal Health ID:</span>
          </div>
          <ul className="list-disc list-inside space-y-1 text-emerald-900/90 pl-1">
            <li>Present this ID to your physician, clinic, hospital admissions, or diagnostic lab.</li>
            <li>When visiting a new provider, you will receive a consent request to authorize clinical history review.</li>
            <li>In an emergency, licensed first responders can scan your QR code to review vital allergies and medications.</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default HealthIdPage;

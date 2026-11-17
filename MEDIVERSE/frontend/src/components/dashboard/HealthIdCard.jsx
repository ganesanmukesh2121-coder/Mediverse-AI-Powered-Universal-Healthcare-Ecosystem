import React, { useState } from 'react';
import { ShieldCheck, Copy, Check, QrCode, AlertTriangle, Droplet } from 'lucide-react';

export const HealthIdCard = ({
  healthId = 'MED-10293847-P',
  patientName = 'Sarah Jenkins',
  dateOfBirth = '1988-04-12',
  bloodGroup = 'O+',
  emergencyContact = '+1-555-019-2835',
  className = '',
}) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(healthId);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div
      className={`relative overflow-hidden rounded-2xl bg-gradient-to-br from-mediverse-800 via-mediverse-700 to-navy-900 text-white p-6 shadow-xl border border-mediverse-600/40 ${className}`}
    >
      {/* Background Graphic Watermark */}
      <div className="absolute -right-8 -bottom-8 w-48 h-48 rounded-full bg-white/5 pointer-events-none blur-xl" />
      <div className="absolute right-6 top-6 opacity-10">
        <ShieldCheck className="w-28 h-28" />
      </div>

      {/* Header */}
      <div className="relative z-10 flex items-center justify-between pb-4 border-b border-white/15">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-white/20 backdrop-blur-md flex items-center justify-center font-bold text-white text-base">
            M
          </div>
          <div>
            <h4 className="text-xs font-semibold tracking-wider uppercase text-mediverse-200">
              Universal Health ID
            </h4>
            <span className="text-[11px] text-white/70">National Healthcare Ecosystem</span>
          </div>
        </div>
        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-500/20 border border-emerald-400/40 text-[11px] font-semibold text-emerald-300">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>VERIFIED</span>
        </div>
      </div>

      {/* Main Details */}
      <div className="relative z-10 mt-5 grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
        <div className="md:col-span-2 space-y-3">
          <div>
            <p className="text-[11px] text-white/60 uppercase tracking-wider">Patient Full Name</p>
            <h3 className="text-xl font-bold tracking-tight text-white">{patientName}</h3>
          </div>

          <div>
            <p className="text-[11px] text-white/60 uppercase tracking-wider">Universal Health Identifier</p>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="font-mono text-lg font-bold tracking-widest text-emerald-300 bg-white/10 px-2.5 py-1 rounded-md">
                {healthId}
              </span>
              <button
                type="button"
                onClick={handleCopy}
                className="p-1.5 rounded-lg bg-white/10 hover:bg-white/20 text-white/80 hover:text-white transition-colors"
                title="Copy Health ID"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
              </button>
            </div>
          </div>
        </div>

        {/* QR Code Placeholder Representation */}
        <div className="flex flex-col items-center justify-center p-3 rounded-xl bg-white text-slate-900 shadow-md">
          <QrCode className="w-16 h-16 text-slate-800" />
          <span className="text-[10px] font-semibold text-slate-500 mt-1 uppercase tracking-wider">
            Scan for Triage
          </span>
        </div>
      </div>

      {/* Footer Info */}
      <div className="relative z-10 mt-6 pt-4 border-t border-white/15 grid grid-cols-3 gap-2 text-xs">
        <div>
          <span className="text-white/60 text-[10px] uppercase block">Date of Birth</span>
          <span className="font-semibold">{dateOfBirth}</span>
        </div>
        <div>
          <span className="text-white/60 text-[10px] uppercase block">Blood Group</span>
          <span className="font-semibold text-rose-300 flex items-center gap-1">
            <Droplet className="w-3 h-3 text-rose-400" />
            {bloodGroup}
          </span>
        </div>
        <div>
          <span className="text-white/60 text-[10px] uppercase block">Emergency ICE</span>
          <span className="font-semibold text-amber-300 truncate block">{emergencyContact}</span>
        </div>
      </div>
    </div>
  );
};

export default HealthIdCard;

import React from 'react';
import { Sparkles, Info } from 'lucide-react';

export const DemoDataBadge = ({
  label = 'Demo Data / Interactive Preview',
  tooltip = 'This section utilizes high-fidelity simulated clinical and operational data while corresponding backend services are in staged rollout.',
  className = '',
}) => {
  return (
    <div
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-amber-50 text-amber-800 border border-amber-300 shadow-xs ${className}`}
      title={tooltip}
    >
      <Sparkles className="w-3.5 h-3.5 text-amber-600 shrink-0 animate-pulse" />
      <span>{label}</span>
      <Info className="w-3 h-3 text-amber-500 opacity-70 ml-0.5" />
    </div>
  );
};

export default DemoDataBadge;

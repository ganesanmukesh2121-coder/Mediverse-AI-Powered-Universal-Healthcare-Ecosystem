import React from 'react';
import { ArrowUpRight, ArrowDownRight } from 'lucide-react';

export const DashboardCard = ({
  title,
  value,
  subtitle,
  change,
  isPositive,
  icon: Icon,
  colorScheme = 'emerald',
  className = '',
  onClick,
}) => {
  const colorSchemes = {
    emerald: {
      bg: 'bg-emerald-50',
      text: 'text-emerald-700',
      iconBorder: 'border-emerald-200',
    },
    blue: {
      bg: 'bg-blue-50',
      text: 'text-blue-700',
      iconBorder: 'border-blue-200',
    },
    purple: {
      bg: 'bg-purple-50',
      text: 'text-purple-700',
      iconBorder: 'border-purple-200',
    },
    amber: {
      bg: 'bg-amber-50',
      text: 'text-amber-700',
      iconBorder: 'border-amber-200',
    },
    red: {
      bg: 'bg-red-50',
      text: 'text-red-700',
      iconBorder: 'border-red-200',
    },
  };

  const scheme = colorSchemes[colorScheme] || colorSchemes.emerald;

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-xl p-5 border border-slate-200/80 shadow-xs hover:shadow-md transition-all ${
        onClick ? 'cursor-pointer hover:border-mediverse-300' : ''
      } ${className}`}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{title}</p>
          <h3 className="text-2xl font-bold text-slate-900 mt-1 tracking-tight">{value}</h3>
        </div>
        {Icon && (
          <div className={`w-11 h-11 rounded-xl flex items-center justify-center border ${scheme.bg} ${scheme.text} ${scheme.iconBorder} shrink-0`}>
            <Icon className="w-5 h-5" />
          </div>
        )}
      </div>

      {(subtitle || change) && (
        <div className="mt-3 flex items-center gap-2 text-xs">
          {change && (
            <span
              className={`inline-flex items-center font-semibold ${
                isPositive ? 'text-emerald-600' : 'text-rose-600'
              }`}
            >
              {isPositive ? (
                <ArrowUpRight className="w-3.5 h-3.5 mr-0.5" />
              ) : (
                <ArrowDownRight className="w-3.5 h-3.5 mr-0.5" />
              )}
              {change}
            </span>
          )}
          {subtitle && <span className="text-slate-500">{subtitle}</span>}
        </div>
      )}
    </div>
  );
};

export default DashboardCard;

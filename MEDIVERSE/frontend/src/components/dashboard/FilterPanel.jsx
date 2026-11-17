import React from 'react';
import { Filter, RotateCcw } from 'lucide-react';
import Button from '../common/Button';

export const FilterPanel = ({
  filters = [],
  values = {},
  onChange,
  onReset,
  className = '',
}) => {
  return (
    <div
      className={`p-4 bg-slate-50 border border-slate-200 rounded-xl flex flex-wrap items-center gap-4 ${className}`}
    >
      <div className="flex items-center gap-1.5 text-xs font-bold text-slate-600 uppercase tracking-wider">
        <Filter className="w-3.5 h-3.5" />
        <span>Filters</span>
      </div>

      <div className="flex flex-wrap items-center gap-3 flex-1">
        {filters.map((filter) => (
          <div key={filter.key} className="flex items-center gap-2">
            {filter.label && (
              <label className="text-xs font-medium text-slate-600 whitespace-nowrap">
                {filter.label}:
              </label>
            )}
            <select
              value={values[filter.key] || ''}
              onChange={(e) => onChange(filter.key, e.target.value)}
              className="text-xs bg-white border border-slate-300 rounded-lg px-2.5 py-1.5 text-slate-700 focus:outline-none focus:ring-1 focus:ring-mediverse-500"
            >
              {filter.placeholder && <option value="">{filter.placeholder}</option>}
              {filter.options.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        ))}
      </div>

      {onReset && (
        <Button
          variant="ghost"
          size="sm"
          icon={RotateCcw}
          onClick={onReset}
          className="text-xs text-slate-500 hover:text-slate-800"
        >
          Reset
        </Button>
      )}
    </div>
  );
};

export default FilterPanel;

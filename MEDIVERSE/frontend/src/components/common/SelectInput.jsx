import React, { forwardRef } from 'react';

export const SelectInput = forwardRef(
  (
    {
      label,
      name,
      value,
      onChange,
      options = [],
      placeholder = 'Select an option',
      error,
      helperText,
      required = false,
      disabled = false,
      className = '',
      ...props
    },
    ref
  ) => {
    return (
      <div className={`w-full flex flex-col gap-1.5 ${className}`}>
        {label && (
          <label htmlFor={name} className="block text-xs font-semibold text-slate-700 tracking-wide">
            {label}
            {required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}
        <select
          ref={ref}
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          disabled={disabled}
          className={`w-full rounded-lg border text-sm px-3.5 py-2 transition-all focus:outline-none focus:ring-2 disabled:bg-slate-50 disabled:text-slate-400 disabled:cursor-not-allowed bg-white ${
            error
              ? 'border-red-400 text-red-900 focus:border-red-500 focus:ring-red-200'
              : 'border-slate-300 text-slate-900 focus:border-mediverse-500 focus:ring-mediverse-100'
          }`}
          {...props}
        >
          {placeholder && (
            <option value="" disabled>
              {placeholder}
            </option>
          )}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        {error ? (
          <p className="text-xs text-red-600 font-medium tracking-tight mt-0.5">{error}</p>
        ) : helperText ? (
          <p className="text-xs text-slate-500 mt-0.5">{helperText}</p>
        ) : null}
      </div>
    );
  }
);

SelectInput.displayName = 'SelectInput';

export default SelectInput;

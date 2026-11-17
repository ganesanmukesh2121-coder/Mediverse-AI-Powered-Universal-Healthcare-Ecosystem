import React, { forwardRef } from 'react';

export const FormInput = forwardRef(
  (
    {
      label,
      name,
      type = 'text',
      placeholder = '',
      value,
      onChange,
      error,
      helperText,
      required = false,
      disabled = false,
      icon: Icon,
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
        <div className="relative rounded-lg shadow-sm">
          {Icon && (
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
              <Icon className="w-4 h-4" />
            </div>
          )}
          <input
            ref={ref}
            id={name}
            name={name}
            type={type}
            value={value}
            onChange={onChange}
            disabled={disabled}
            placeholder={placeholder}
            className={`w-full rounded-lg border text-sm transition-all focus:outline-none focus:ring-2 disabled:bg-slate-50 disabled:text-slate-400 disabled:cursor-not-allowed ${
              Icon ? 'pl-10 pr-3.5' : 'px-3.5'
            } py-2 ${
              error
                ? 'border-red-400 text-red-900 focus:border-red-500 focus:ring-red-200 bg-red-50/20'
                : 'border-slate-300 text-slate-900 focus:border-mediverse-500 focus:ring-mediverse-100 bg-white'
            }`}
            {...props}
          />
        </div>
        {error ? (
          <p className="text-xs text-red-600 font-medium tracking-tight mt-0.5">{error}</p>
        ) : helperText ? (
          <p className="text-xs text-slate-500 mt-0.5">{helperText}</p>
        ) : null}
      </div>
    );
  }
);

FormInput.displayName = 'FormInput';

export default FormInput;

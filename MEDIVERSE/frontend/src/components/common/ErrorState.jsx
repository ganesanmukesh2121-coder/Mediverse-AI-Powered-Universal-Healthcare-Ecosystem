import React from 'react';
import { AlertCircle, RotateCcw } from 'lucide-react';
import Button from './Button';

export const ErrorState = ({
  title = 'Failed to load data',
  message = 'An unexpected error occurred while processing your request. Please verify network connectivity and try again.',
  onRetry,
  className = '',
}) => {
  return (
    <div
      className={`p-6 rounded-xl border border-red-200 bg-red-50/60 flex flex-col items-center text-center gap-3 ${className}`}
    >
      <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center text-red-600">
        <AlertCircle className="w-6 h-6" />
      </div>
      <div>
        <h4 className="text-base font-semibold text-red-900">{title}</h4>
        <p className="text-sm text-red-700 mt-1 max-w-md">{message}</p>
      </div>
      {onRetry && (
        <Button variant="outline" size="sm" icon={RotateCcw} onClick={onRetry} className="mt-2 bg-white">
          Try Again
        </Button>
      )}
    </div>
  );
};

export default ErrorState;

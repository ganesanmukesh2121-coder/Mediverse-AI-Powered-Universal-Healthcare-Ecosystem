import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, ArrowLeft, KeyRound, CheckCircle2 } from 'lucide-react';
import authService from '../../services/authService';
import FormInput from '../../components/common/FormInput';
import Button from '../../components/common/Button';

export const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) return;

    setIsLoading(true);
    try {
      await authService.requestPasswordReset(email);
    } catch {
      // Graceful fallback
    } finally {
      setIsLoading(false);
      setSubmitted(true);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <div className="w-12 h-12 rounded-xl bg-mediverse-600 text-white mx-auto flex items-center justify-center shadow-md">
          <KeyRound className="w-6 h-6" />
        </div>
        <h2 className="mt-3 text-2xl font-bold text-slate-900 tracking-tight">
          Reset Your Password
        </h2>
        <p className="mt-1 text-xs text-slate-500">
          Enter your registered email address to receive password reset instructions
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <div className="bg-white py-8 px-6 shadow-xl border border-slate-200/80 rounded-2xl sm:px-10">
          {submitted ? (
            <div className="text-center py-4">
              <CheckCircle2 className="w-12 h-12 text-emerald-500 mx-auto mb-3" />
              <h3 className="text-base font-bold text-slate-900">Check Your Email</h3>
              <p className="text-xs text-slate-600 mt-2 leading-relaxed">
                If an account exists for <span className="font-semibold text-slate-900">{email}</span>, we have dispatched a secure one-time password recovery link.
              </p>
              <div className="mt-6">
                <Link to="/login">
                  <Button variant="primary" size="md" className="w-full">
                    Return to Login
                  </Button>
                </Link>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <FormInput
                label="Registered Email Address"
                name="email"
                type="email"
                placeholder="name@example.com"
                icon={Mail}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />

              <Button
                type="submit"
                variant="primary"
                size="lg"
                className="w-full mt-2"
                isLoading={isLoading}
              >
                Send Password Reset Link
              </Button>

              <div className="pt-4 text-center">
                <Link
                  to="/login"
                  className="inline-flex items-center text-xs font-semibold text-mediverse-600 hover:text-mediverse-700 gap-1.5"
                >
                  <ArrowLeft className="w-3.5 h-3.5" />
                  <span>Back to Sign In</span>
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;

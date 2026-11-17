import React, { useState, useEffect } from 'react';
import { Users, Search, UserCheck, UserX, Shield, Clock } from 'lucide-react';
import { useNotification } from '../../context/NotificationContext';
import adminService from '../../services/adminService';
import StatusBadge from '../../components/common/StatusBadge';
import SearchBar from '../../components/dashboard/SearchBar';
import Button from '../../components/common/Button';
import ConfirmationDialog from '../../components/common/ConfirmationDialog';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const UserManagementPage = () => {
  const { notifySuccess } = useNotification();
  const [users, setUsers] = useState([]);
  const [filterRole, setFilterRole] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [targetUser, setTargetUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    adminService.getUsers().then((data) => {
      setUsers(data);
      setIsLoading(false);
    });
  }, []);

  const handleToggleStatus = async () => {
    if (!targetUser) return;
    const newStatus = targetUser.accountStatus === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    await adminService.updateUserStatus(targetUser.id, newStatus);
    setUsers((prev) =>
      prev.map((u) => (u.id === targetUser.id ? { ...u, accountStatus: newStatus } : u))
    );
    notifySuccess(`User account status transitioned to ${newStatus}.`);
    setTargetUser(null);
  };

  const filtered = users.filter((u) => {
    const matchesRole = filterRole === 'ALL' || u.role === filterRole;
    const term = searchTerm.toLowerCase();
    const matchesSearch = u.fullName.toLowerCase().includes(term) || u.email.toLowerCase().includes(term);
    return matchesRole && matchesSearch;
  });

  if (isLoading) {
    return <LoadingSpinner fullScreen message="Loading platform user registry..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Ecosystem User Management</h2>
          <p className="text-xs text-slate-500 mt-1">
            Manage authenticated accounts, enforce status suspensions, and audit credentials across all six roles
          </p>
        </div>
      </div>

      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
        <div className="max-w-md w-full">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search by full name or email address..."
          />
        </div>

        {/* Role Filters */}
        <div className="flex items-center gap-1.5 overflow-x-auto text-xs">
          {['ALL', 'PATIENT', 'DOCTOR', 'EMERGENCY', 'MEDICAL', 'LAB', 'ADMIN'].map((role) => (
            <button
              key={role}
              onClick={() => setFilterRole(role)}
              className={`px-3 py-1.5 rounded-lg font-semibold transition-all whitespace-nowrap ${
                filterRole === role
                  ? 'bg-indigo-600 text-white shadow-xs'
                  : 'text-slate-600 hover:bg-slate-100'
              }`}
            >
              {role}
            </button>
          ))}
        </div>
      </div>

      <div className="space-y-3">
        {filtered.map((user) => (
          <div
            key={user.id}
            className="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row items-start md:items-center justify-between gap-4"
          >
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center font-bold text-slate-700 shrink-0">
                {user.fullName.charAt(0)}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h4 className="text-base font-bold text-slate-900">{user.fullName}</h4>
                  <StatusBadge status={user.accountStatus} size="sm" />
                  <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                    {user.role}
                  </span>
                </div>
                <p className="text-xs text-slate-500 mt-0.5">{user.email}</p>
                <div className="flex items-center gap-4 mt-2 text-xs text-slate-400">
                  <span>Created: {user.createdAt}</span>
                  <span>Last Active: {user.lastLogin}</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 w-full md:w-auto justify-end border-t md:border-t-0 pt-3 md:pt-0">
              {user.accountStatus === 'ACTIVE' ? (
                <Button
                  variant="outline"
                  size="sm"
                  className="text-rose-600 border-rose-200 hover:bg-rose-50 text-xs"
                  onClick={() => setTargetUser(user)}
                >
                  Suspend Account
                </Button>
              ) : (
                <Button
                  variant="primary"
                  size="sm"
                  className="bg-emerald-600 hover:bg-emerald-500 text-xs"
                  onClick={() => setTargetUser(user)}
                >
                  Re-Activate Account
                </Button>
              )}
            </div>
          </div>
        ))}

        {filtered.length === 0 && (
          <div className="py-12 text-center text-sm text-slate-400 bg-white rounded-2xl border border-slate-200">
            No users found matching query.
          </div>
        )}
      </div>

      <ConfirmationDialog
        isOpen={!!targetUser}
        onClose={() => setTargetUser(null)}
        onConfirm={handleToggleStatus}
        title={targetUser?.accountStatus === 'ACTIVE' ? 'Suspend User Access' : 'Reactivate User Account'}
        message={
          targetUser?.accountStatus === 'ACTIVE'
            ? `Are you sure you want to suspend ${targetUser?.fullName}? They will be immediately locked out of all portal operations.`
            : `Are you sure you want to restore active status for ${targetUser?.fullName}?`
        }
        confirmLabel={targetUser?.accountStatus === 'ACTIVE' ? 'Confirm Suspension' : 'Confirm Reactivation'}
        isDangerous={targetUser?.accountStatus === 'ACTIVE'}
      />
    </div>
  );
};

export default UserManagementPage;

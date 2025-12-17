"use client";

import { motion } from "framer-motion";
import { User, Mail, Shield, LogOut } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

export default function ProfilePage() {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    window.location.href = "/";
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.1 },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0 },
  };

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="max-w-2xl space-y-8"
    >
      {/* Header */}
      <motion.div variants={itemVariants}>
        <h1 className="text-3xl font-bold text-white mb-2">Profile</h1>
        <p className="text-slate-400">Manage your account settings</p>
      </motion.div>

      {/* Profile Card */}
      <motion.div
        variants={itemVariants}
        className="bg-slate-800/50 border border-slate-700 rounded-2xl p-6"
      >
        {/* Avatar & Name */}
        <div className="flex items-center gap-4 mb-6">
          <div className="w-20 h-20 rounded-full bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center text-white text-3xl font-bold">
            {user?.email?.charAt(0).toUpperCase() || "U"}
          </div>
          <div>
            <h2 className="text-xl font-semibold text-white">{user?.email || "User"}</h2>
            <p className="text-slate-400 capitalize">{user?.role?.toLowerCase()} Account</p>
          </div>
        </div>

        {/* Details */}
        <div className="space-y-4">
          <div className="flex items-center gap-4 p-4 bg-slate-900/50 rounded-lg">
            <Mail className="w-5 h-5 text-slate-400" />
            <div>
              <p className="text-sm text-slate-400">Email</p>
              <p className="text-white">{user?.email || "—"}</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 bg-slate-900/50 rounded-lg">
            <Shield className="w-5 h-5 text-slate-400" />
            <div>
              <p className="text-sm text-slate-400">Role</p>
              <p className="text-white capitalize">{user?.role?.toLowerCase() || "—"}</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 bg-slate-900/50 rounded-lg">
            <User className="w-5 h-5 text-slate-400" />
            <div>
              <p className="text-sm text-slate-400">User ID</p>
              <p className="text-white font-mono text-sm">{user?.id || "—"}</p>
            </div>
          </div>
        </div>
      </motion.div>

      {/* Actions */}
      <motion.div variants={itemVariants}>
        <h3 className="text-lg font-semibold text-white mb-4">Account Actions</h3>
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full p-4 bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg hover:bg-red-500/20 transition-colors"
        >
          <LogOut className="w-5 h-5" />
          <span className="font-medium">Sign Out</span>
        </button>
      </motion.div>
    </motion.div>
  );
}

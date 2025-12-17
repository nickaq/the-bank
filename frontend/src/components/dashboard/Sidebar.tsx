"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { motion } from "framer-motion";
import {
  LayoutDashboard,
  Wallet,
  ArrowLeftRight,
  FileText,
  User,
  LogOut,
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import Logo from "@/components/Logo";

const navItems = [
  { href: "/dashboard", icon: LayoutDashboard, label: "Overview" },
  { href: "/dashboard/accounts", icon: Wallet, label: "Accounts" },
  { href: "/dashboard/transfers", icon: ArrowLeftRight, label: "Transfers" },
  { href: "/dashboard/statements", icon: FileText, label: "Statements" },
  { href: "/dashboard/profile", icon: User, label: "Profile" },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    window.location.href = "/";
  };

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-slate-900/95 backdrop-blur-xl border-r border-slate-800 flex flex-col z-50">
      {/* Logo */}
      <div className="p-6 border-b border-slate-800">
        <Link href="/dashboard" className="flex items-center gap-3">
          <Logo size="sm" animated={false} />
          <span className="text-xl font-bold bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent">
            Z-Bank
          </span>
        </Link>
      </div>

      {/* Navigation */}
      <nav className="flex-1 py-6 px-3">
        <ul className="space-y-1">
          {navItems.map((item) => {
            const isActive = pathname === item.href || 
              (item.href !== "/dashboard" && pathname.startsWith(item.href));
            
            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={`relative flex items-center gap-3 px-4 py-3 rounded-lg transition-all ${
                    isActive
                      ? "text-white"
                      : "text-slate-400 hover:text-white hover:bg-slate-800/50"
                  }`}
                >
                  {isActive && (
                    <motion.div
                      layoutId="activeNav"
                      className="absolute inset-0 bg-gradient-to-r from-indigo-600/20 to-purple-600/20 border border-indigo-500/30 rounded-lg"
                      transition={{ type: "spring", duration: 0.5 }}
                    />
                  )}
                  <item.icon className={`w-5 h-5 relative z-10 ${isActive ? "text-indigo-400" : ""}`} />
                  <span className="relative z-10 font-medium">{item.label}</span>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* User section */}
      <div className="p-4 border-t border-slate-800">
        <div className="flex items-center gap-3 mb-4 px-2">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center text-white font-semibold">
            {user?.email?.charAt(0).toUpperCase() || "U"}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">
              {user?.email || "User"}
            </p>
            <p className="text-xs text-slate-500 capitalize">
              {user?.role?.toLowerCase() || "client"}
            </p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-4 py-3 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-all"
        >
          <LogOut className="w-5 h-5" />
          <span className="font-medium">Sign Out</span>
        </button>
      </div>
    </aside>
  );
}

"use client";

import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Wallet, Eye, EyeOff } from "lucide-react";
import Link from "next/link";
import { Account } from "@/types";
import { accountApi } from "@/lib/services";

export default function AccountsPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showBalances, setShowBalances] = useState(true);

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const data = await accountApi.getMyAccounts();
        setAccounts(data);
      } catch (error) {
        console.error("Failed to fetch accounts:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchAccounts();
  }, []);

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

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <motion.div
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      className="space-y-8"
    >
      {/* Header */}
      <motion.div variants={itemVariants} className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Accounts</h1>
          <p className="text-slate-400">Manage your bank accounts</p>
        </div>
        <button
          onClick={() => setShowBalances(!showBalances)}
          className="flex items-center gap-2 px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-300 hover:bg-slate-700 transition-colors"
        >
          {showBalances ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
          {showBalances ? "Hide" : "Show"} Balances
        </button>
      </motion.div>

      {/* Accounts Grid */}
      {accounts.length === 0 ? (
        <motion.div
          variants={itemVariants}
          className="p-12 text-center bg-slate-800/30 border border-slate-700 rounded-2xl"
        >
          <Wallet className="w-16 h-16 text-slate-600 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-white mb-2">No Accounts</h3>
          <p className="text-slate-400">Contact support to open your first account</p>
        </motion.div>
      ) : (
        <div className="grid gap-6">
          {accounts.map((account) => (
            <motion.div
              key={account.id}
              variants={itemVariants}
              className="bg-slate-800/50 border border-slate-700 rounded-2xl p-6 hover:border-indigo-500/50 transition-colors"
            >
              <div className="flex items-start justify-between mb-6">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-500 flex items-center justify-center">
                    <Wallet className="w-7 h-7 text-white" />
                  </div>
                  <div>
                    <p className="text-lg font-semibold text-white">{account.currency} Account</p>
                    <p className="text-sm text-slate-400 font-mono">{account.iban}</p>
                  </div>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-xs font-medium ${
                    account.status === "ACTIVE"
                      ? "bg-green-500/20 text-green-400"
                      : account.status === "BLOCKED"
                      ? "bg-red-500/20 text-red-400"
                      : "bg-yellow-500/20 text-yellow-400"
                  }`}
                >
                  {account.status}
                </span>
              </div>

              <div className="mb-6">
                <p className="text-sm text-slate-400 mb-1">Available Balance</p>
                <p className="text-3xl font-bold text-white">
                  {showBalances
                    ? `€${account.balance.toLocaleString("en-US", { minimumFractionDigits: 2 })}`
                    : "€••••••"}
                </p>
              </div>

              <div className="flex gap-3">
                <Link
                  href={`/dashboard/transfers/new?from=${account.id}`}
                  className="flex-1 py-3 px-4 bg-indigo-600 hover:bg-indigo-500 text-white font-medium rounded-lg text-center transition-colors"
                >
                  Send Money
                </Link>
                <Link
                  href={`/dashboard/statements?accountId=${account.id}`}
                  className="flex-1 py-3 px-4 bg-slate-700 hover:bg-slate-600 text-white font-medium rounded-lg text-center transition-colors"
                >
                  View Statement
                </Link>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </motion.div>
  );
}

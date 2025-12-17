"use client";

import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { Wallet, ArrowUpRight, ArrowDownRight, Plus } from "lucide-react";
import Link from "next/link";
import { Account, Transfer } from "@/types";
import { accountApi, transferApi } from "@/lib/services";

export default function DashboardPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [recentTransfers, setRecentTransfers] = useState<Transfer[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [accountsData, transfersData] = await Promise.all([
          accountApi.getMyAccounts(),
          transferApi.getMyTransfers(0, 5),
        ]);
        setAccounts(accountsData);
        setRecentTransfers(transfersData.content);
      } catch (error) {
        console.error("Failed to fetch data:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

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
      <motion.div variants={itemVariants}>
        <h1 className="text-3xl font-bold text-white mb-2">Dashboard</h1>
        <p className="text-slate-400">Welcome back! Here's your financial overview.</p>
      </motion.div>

      {/* Total Balance Card */}
      <motion.div
        variants={itemVariants}
        className="bg-gradient-to-br from-indigo-600 to-purple-600 rounded-2xl p-6 shadow-lg shadow-indigo-500/20"
      >
        <p className="text-indigo-200 mb-2">Total Balance</p>
        <p className="text-4xl font-bold text-white">
          €{totalBalance.toLocaleString("en-US", { minimumFractionDigits: 2 })}
        </p>
        <p className="text-indigo-200 mt-2">
          Across {accounts.length} account{accounts.length !== 1 ? "s" : ""}
        </p>
      </motion.div>

      {/* Quick Actions */}
      <motion.div variants={itemVariants} className="grid grid-cols-2 gap-4">
        <Link
          href="/dashboard/transfers/new"
          className="flex items-center gap-3 p-4 bg-slate-800/50 border border-slate-700 rounded-xl hover:bg-slate-800 transition-colors"
        >
          <div className="w-10 h-10 rounded-lg bg-indigo-500/20 flex items-center justify-center">
            <ArrowUpRight className="w-5 h-5 text-indigo-400" />
          </div>
          <span className="font-medium text-white">Send Money</span>
        </Link>
        <Link
          href="/dashboard/accounts"
          className="flex items-center gap-3 p-4 bg-slate-800/50 border border-slate-700 rounded-xl hover:bg-slate-800 transition-colors"
        >
          <div className="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center">
            <Plus className="w-5 h-5 text-purple-400" />
          </div>
          <span className="font-medium text-white">View Accounts</span>
        </Link>
      </motion.div>

      {/* Accounts List */}
      <motion.div variants={itemVariants}>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-white">Your Accounts</h2>
          <Link href="/dashboard/accounts" className="text-sm text-indigo-400 hover:text-indigo-300">
            View all
          </Link>
        </div>
        <div className="grid gap-4">
          {accounts.length === 0 ? (
            <div className="p-8 text-center bg-slate-800/30 border border-slate-700 rounded-xl">
              <Wallet className="w-12 h-12 text-slate-600 mx-auto mb-4" />
              <p className="text-slate-400">No accounts yet</p>
              <p className="text-sm text-slate-500 mt-1">Contact support to open an account</p>
            </div>
          ) : (
            accounts.slice(0, 3).map((account) => (
              <Link
                key={account.id}
                href={`/dashboard/statements?accountId=${account.id}`}
                className="flex items-center justify-between p-4 bg-slate-800/50 border border-slate-700 rounded-xl hover:bg-slate-800 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-indigo-500/20 to-purple-500/20 flex items-center justify-center">
                    <Wallet className="w-6 h-6 text-indigo-400" />
                  </div>
                  <div>
                    <p className="font-medium text-white">{account.iban}</p>
                    <p className="text-sm text-slate-400">{account.currency} Account</p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-white">
                    €{account.balance.toLocaleString("en-US", { minimumFractionDigits: 2 })}
                  </p>
                  <p className={`text-xs ${
                    account.status === "ACTIVE" ? "text-green-400" : "text-yellow-400"
                  }`}>
                    {account.status}
                  </p>
                </div>
              </Link>
            ))
          )}
        </div>
      </motion.div>

      {/* Recent Transfers */}
      <motion.div variants={itemVariants}>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-white">Recent Transfers</h2>
          <Link href="/dashboard/transfers" className="text-sm text-indigo-400 hover:text-indigo-300">
            View all
          </Link>
        </div>
        <div className="space-y-3">
          {recentTransfers.length === 0 ? (
            <div className="p-8 text-center bg-slate-800/30 border border-slate-700 rounded-xl">
              <ArrowUpRight className="w-12 h-12 text-slate-600 mx-auto mb-4" />
              <p className="text-slate-400">No transfers yet</p>
              <p className="text-sm text-slate-500 mt-1">Make your first transfer to get started</p>
            </div>
          ) : (
            recentTransfers.map((transfer) => (
              <div
                key={transfer.id}
                className="flex items-center justify-between p-4 bg-slate-800/30 border border-slate-700 rounded-xl"
              >
                <div className="flex items-center gap-4">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                    transfer.status === "COMPLETED" ? "bg-green-500/20" : "bg-yellow-500/20"
                  }`}>
                    <ArrowUpRight className={`w-5 h-5 ${
                      transfer.status === "COMPLETED" ? "text-green-400" : "text-yellow-400"
                    }`} />
                  </div>
                  <div>
                    <p className="font-medium text-white">
                      {transfer.description || "Transfer"}
                    </p>
                    <p className="text-sm text-slate-400">
                      To: {transfer.toIban?.slice(-8)}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="font-semibold text-white">
                    -€{transfer.amount.toLocaleString("en-US", { minimumFractionDigits: 2 })}
                  </p>
                  <p className={`text-xs ${
                    transfer.status === "COMPLETED" ? "text-green-400" :
                    transfer.status === "PENDING" ? "text-yellow-400" : "text-red-400"
                  }`}>
                    {transfer.status}
                  </p>
                </div>
              </div>
            ))
          )}
        </div>
      </motion.div>
    </motion.div>
  );
}

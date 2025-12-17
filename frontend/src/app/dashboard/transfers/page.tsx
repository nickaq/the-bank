"use client";

import { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { ArrowUpRight, ArrowDownRight, Plus, Filter } from "lucide-react";
import Link from "next/link";
import { Transfer, Account, Page } from "@/types";
import { transferApi, accountApi } from "@/lib/services";

export default function TransfersPage() {
  const [transfers, setTransfers] = useState<Transfer[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [transfersData, accountsData] = await Promise.all([
          transferApi.getMyTransfers(page, 10),
          accountApi.getMyAccounts(),
        ]);
        setTransfers(transfersData.content);
        setTotalPages(transfersData.totalPages);
        setAccounts(accountsData);
      } catch (error) {
        console.error("Failed to fetch transfers:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, [page]);

  const getAccountIban = (accountId: string) => {
    const account = accounts.find((a) => a.id === accountId);
    return account?.iban || accountId;
  };

  const isOutgoing = (transfer: Transfer) => {
    return accounts.some((a) => a.id === transfer.fromAccountId);
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.05 },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 10 },
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
          <h1 className="text-3xl font-bold text-white mb-2">Transfers</h1>
          <p className="text-slate-400">View and manage your transfers</p>
        </div>
        <Link
          href="/dashboard/transfers/new"
          className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-lg shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40 transition-all"
        >
          <Plus className="w-5 h-5" />
          New Transfer
        </Link>
      </motion.div>

      {/* Transfers List */}
      {transfers.length === 0 ? (
        <motion.div
          variants={itemVariants}
          className="p-12 text-center bg-slate-800/30 border border-slate-700 rounded-2xl"
        >
          <ArrowUpRight className="w-16 h-16 text-slate-600 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-white mb-2">No Transfers Yet</h3>
          <p className="text-slate-400 mb-6">Make your first transfer to get started</p>
          <Link
            href="/dashboard/transfers/new"
            className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-500 transition-colors"
          >
            <Plus className="w-5 h-5" />
            New Transfer
          </Link>
        </motion.div>
      ) : (
        <>
          <div className="bg-slate-800/30 border border-slate-700 rounded-2xl overflow-hidden">
            <div className="divide-y divide-slate-700">
              {transfers.map((transfer) => {
                const outgoing = isOutgoing(transfer);
                return (
                  <motion.div
                    key={transfer.id}
                    variants={itemVariants}
                    className="flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors"
                  >
                    <div className="flex items-center gap-4">
                      <div
                        className={`w-12 h-12 rounded-xl flex items-center justify-center ${
                          outgoing ? "bg-red-500/20" : "bg-green-500/20"
                        }`}
                      >
                        {outgoing ? (
                          <ArrowUpRight className="w-6 h-6 text-red-400" />
                        ) : (
                          <ArrowDownRight className="w-6 h-6 text-green-400" />
                        )}
                      </div>
                      <div>
                        <p className="font-medium text-white">
                          {transfer.description || (outgoing ? "Outgoing Transfer" : "Incoming Transfer")}
                        </p>
                        <p className="text-sm text-slate-400">
                          {outgoing ? `To: ${transfer.toIban}` : `From: ${transfer.fromIban}`}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className={`font-semibold ${outgoing ? "text-red-400" : "text-green-400"}`}>
                        {outgoing ? "-" : "+"}€{transfer.amount.toLocaleString("en-US", { minimumFractionDigits: 2 })}
                      </p>
                      <p
                        className={`text-xs ${
                          transfer.status === "COMPLETED"
                            ? "text-green-400"
                            : transfer.status === "PENDING"
                            ? "text-yellow-400"
                            : "text-red-400"
                        }`}
                      >
                        {transfer.status}
                      </p>
                    </div>
                  </motion.div>
                );
              })}
            </div>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors"
              >
                Previous
              </button>
              <span className="px-4 py-2 text-slate-400">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="px-4 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-slate-700 transition-colors"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </motion.div>
  );
}

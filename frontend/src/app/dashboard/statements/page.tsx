"use client";

import { useEffect, useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { motion } from "framer-motion";
import { ArrowUpRight, ArrowDownRight, FileText, Calendar } from "lucide-react";
import { Account, Statement, LedgerEntry } from "@/types";
import { accountApi } from "@/lib/services";

function StatementsContent() {
  const searchParams = useSearchParams();
  const accountIdParam = searchParams.get("accountId");

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selectedAccount, setSelectedAccount] = useState<string>("");
  const [statement, setStatement] = useState<Statement | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingStatement, setIsLoadingStatement] = useState(false);

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const data = await accountApi.getMyAccounts();
        setAccounts(data);
        if (accountIdParam && data.some((a) => a.id === accountIdParam)) {
          setSelectedAccount(accountIdParam);
        } else if (data.length > 0) {
          setSelectedAccount(data[0].id);
        }
      } catch (error) {
        console.error("Failed to fetch accounts:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchAccounts();
  }, [accountIdParam]);

  useEffect(() => {
    if (!selectedAccount) return;

    const fetchStatement = async () => {
      setIsLoadingStatement(true);
      try {
        const data = await accountApi.getStatement(selectedAccount, { size: 50 });
        setStatement(data);
      } catch (error) {
        console.error("Failed to fetch statement:", error);
      } finally {
        setIsLoadingStatement(false);
      }
    };
    fetchStatement();
  }, [selectedAccount]);

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
          <h1 className="text-3xl font-bold text-white mb-2">Statements</h1>
          <p className="text-slate-400">View your account transaction history</p>
        </div>
      </motion.div>

      {/* Account Selector */}
      <motion.div variants={itemVariants}>
        <label className="block text-sm font-medium text-slate-300 mb-2">Select Account</label>
        <select
          value={selectedAccount}
          onChange={(e) => setSelectedAccount(e.target.value)}
          className="w-full max-w-md px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
        >
          {accounts.map((account) => (
            <option key={account.id} value={account.id}>
              {account.iban} — {account.currency}
            </option>
          ))}
        </select>
      </motion.div>

      {/* Statement Summary */}
      {statement && !isLoadingStatement && (
        <motion.div
          variants={itemVariants}
          className="grid grid-cols-1 md:grid-cols-3 gap-4"
        >
          <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-5">
            <p className="text-sm text-slate-400 mb-1">Opening Balance</p>
            <p className="text-2xl font-bold text-white">
              €{statement.openingBalance.toLocaleString("en-US", { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-5">
            <p className="text-sm text-slate-400 mb-1">Closing Balance</p>
            <p className="text-2xl font-bold text-white">
              €{statement.closingBalance.toLocaleString("en-US", { minimumFractionDigits: 2 })}
            </p>
          </div>
          <div className="bg-slate-800/50 border border-slate-700 rounded-xl p-5">
            <p className="text-sm text-slate-400 mb-1">Total Transactions</p>
            <p className="text-2xl font-bold text-white">{statement.totalEntries}</p>
          </div>
        </motion.div>
      )}

      {/* Transactions */}
      {isLoadingStatement ? (
        <div className="flex items-center justify-center h-32">
          <div className="w-6 h-6 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
        </div>
      ) : statement && statement.entries.length > 0 ? (
        <motion.div variants={itemVariants}>
          <div className="bg-slate-800/30 border border-slate-700 rounded-2xl overflow-hidden">
            <div className="divide-y divide-slate-700">
              {statement.entries.map((entry: LedgerEntry) => (
                <motion.div
                  key={entry.id}
                  variants={itemVariants}
                  className="flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div
                      className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                        entry.direction === "CREDIT" ? "bg-green-500/20" : "bg-red-500/20"
                      }`}
                    >
                      {entry.direction === "CREDIT" ? (
                        <ArrowDownRight className="w-5 h-5 text-green-400" />
                      ) : (
                        <ArrowUpRight className="w-5 h-5 text-red-400" />
                      )}
                    </div>
                    <div>
                      <p className="font-medium text-white">
                        {entry.description || (entry.direction === "CREDIT" ? "Credit" : "Debit")}
                      </p>
                      <p className="text-sm text-slate-400">
                        {new Date(entry.createdAt).toLocaleDateString("en-US", {
                          year: "numeric",
                          month: "short",
                          day: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p
                      className={`font-semibold ${
                        entry.direction === "CREDIT" ? "text-green-400" : "text-red-400"
                      }`}
                    >
                      {entry.direction === "CREDIT" ? "+" : "-"}€
                      {entry.amount.toLocaleString("en-US", { minimumFractionDigits: 2 })}
                    </p>
                    <p className="text-sm text-slate-500">
                      Balance: €{entry.balanceAfter.toLocaleString("en-US", { minimumFractionDigits: 2 })}
                    </p>
                  </div>
                </motion.div>
              ))}
            </div>
          </div>
        </motion.div>
      ) : (
        <motion.div
          variants={itemVariants}
          className="p-12 text-center bg-slate-800/30 border border-slate-700 rounded-2xl"
        >
          <FileText className="w-16 h-16 text-slate-600 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-white mb-2">No Transactions</h3>
          <p className="text-slate-400">This account has no transaction history yet</p>
        </motion.div>
      )}
    </motion.div>
  );
}

export default function StatementsPage() {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
      </div>
    }>
      <StatementsContent />
    </Suspense>
  );
}

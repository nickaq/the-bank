"use client";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { motion } from "framer-motion";
import { ArrowRight, AlertCircle, CheckCircle } from "lucide-react";
import { Account, CreateTransferRequest } from "@/types";
import { accountApi, transferApi } from "@/lib/services";

export default function NewTransferPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const fromAccountId = searchParams.get("from");

  const [accounts, setAccounts] = useState<Account[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const [fromAccount, setFromAccount] = useState(fromAccountId || "");
  const [toIban, setToIban] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const data = await accountApi.getMyAccounts();
        setAccounts(data.filter((a) => a.status === "ACTIVE"));
        if (fromAccountId) {
          setFromAccount(fromAccountId);
        } else if (data.length > 0) {
          setFromAccount(data[0].id);
        }
      } catch (error) {
        console.error("Failed to fetch accounts:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchAccounts();
  }, [fromAccountId]);

  const selectedAccount = accounts.find((a) => a.id === fromAccount);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    const amountNum = parseFloat(amount);
    if (isNaN(amountNum) || amountNum <= 0) {
      setError("Please enter a valid amount");
      setIsSubmitting(false);
      return;
    }

    if (selectedAccount && amountNum > selectedAccount.balance) {
      setError("Insufficient funds");
      setIsSubmitting(false);
      return;
    }

    // Find target account by IBAN
    const targetAccount = accounts.find((a) => a.iban === toIban);
    if (!targetAccount) {
      setError("Target account not found. Please check the IBAN.");
      setIsSubmitting(false);
      return;
    }

    try {
      const request: CreateTransferRequest = {
        fromAccountId: fromAccount,
        toAccountId: targetAccount.id,
        amount: amountNum,
        description: description || undefined,
      };

      await transferApi.createTransfer(request, crypto.randomUUID());
      setSuccess(true);
      setTimeout(() => {
        router.push("/dashboard/transfers");
      }, 2000);
    } catch (err: unknown) {
      const error = err as { message?: string };
      setError(error.message || "Transfer failed. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-4 border-indigo-500/30 border-t-indigo-500 rounded-full animate-spin" />
      </div>
    );
  }

  if (success) {
    return (
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="flex flex-col items-center justify-center h-64 text-center"
      >
        <div className="w-16 h-16 rounded-full bg-green-500/20 flex items-center justify-center mb-4">
          <CheckCircle className="w-8 h-8 text-green-400" />
        </div>
        <h2 className="text-2xl font-bold text-white mb-2">Transfer Successful!</h2>
        <p className="text-slate-400">Redirecting to transfers...</p>
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="max-w-xl mx-auto"
    >
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-2">New Transfer</h1>
        <p className="text-slate-400">Send money to another account</p>
      </div>

      {/* Error */}
      {error && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center gap-3"
        >
          <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0" />
          <p className="text-red-400 text-sm">{error}</p>
        </motion.div>
      )}

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* From Account */}
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">From Account</label>
          <select
            value={fromAccount}
            onChange={(e) => setFromAccount(e.target.value)}
            className="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
          >
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.iban} — €{account.balance.toLocaleString("en-US", { minimumFractionDigits: 2 })}
              </option>
            ))}
          </select>
        </div>

        {/* To IBAN */}
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">To IBAN</label>
          <input
            type="text"
            value={toIban}
            onChange={(e) => setToIban(e.target.value.toUpperCase())}
            required
            placeholder="Enter recipient IBAN"
            className="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent font-mono"
          />
        </div>

        {/* Amount */}
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">Amount (EUR)</label>
          <div className="relative">
            <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">€</span>
            <input
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              required
              min="0.01"
              step="0.01"
              placeholder="0.00"
              className="w-full pl-10 pr-4 py-3 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            />
          </div>
          {selectedAccount && (
            <p className="mt-2 text-sm text-slate-500">
              Available: €{selectedAccount.balance.toLocaleString("en-US", { minimumFractionDigits: 2 })}
            </p>
          )}
        </div>

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-2">Description (Optional)</label>
          <input
            type="text"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Payment for..."
            className="w-full px-4 py-3 bg-slate-800/50 border border-slate-700 rounded-lg text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
          />
        </div>

        {/* Submit */}
        <motion.button
          type="submit"
          disabled={isSubmitting || !fromAccount || !toIban || !amount}
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          className="w-full py-4 px-6 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-lg shadow-lg shadow-indigo-500/25 hover:shadow-indigo-500/40 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
        >
          {isSubmitting ? (
            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          ) : (
            <>
              Send Transfer
              <ArrowRight className="w-5 h-5" />
            </>
          )}
        </motion.button>
      </form>
    </motion.div>
  );
}

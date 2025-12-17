// User & Auth Types
export interface User {
  id: string;
  email: string;
  role: "CLIENT" | "ADMIN" | "AUDITOR";
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  role: string;
}

export interface AuthRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  role?: "CLIENT" | "ADMIN" | "AUDITOR";
}

// Customer Types
export interface Customer {
  id: string;
  fullName: string;
  email: string;
  phone?: string;
  status: "ACTIVE" | "BLOCKED" | "PENDING";
  createdAt: string;
  updatedAt: string;
}

// Account Types
export interface Account {
  id: string;
  customerId: string;
  customerName: string;
  iban: string;
  currency: string;
  status: "ACTIVE" | "BLOCKED" | "CLOSED";
  balance: number;
  createdAt: string;
  updatedAt: string;
}

// Transfer Types
export interface Transfer {
  id: string;
  fromAccountId: string;
  fromIban: string;
  toAccountId: string;
  toIban: string;
  amount: number;
  currency: string;
  status: "PENDING" | "COMPLETED" | "REJECTED";
  description?: string;
  failureReason?: string;
  createdAt: string;
  completedAt?: string;
}

export interface CreateTransferRequest {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  description?: string;
  idempotencyKey?: string;
}

// Ledger Types
export interface LedgerEntry {
  id: string;
  accountId: string;
  transferId?: string;
  direction: "DEBIT" | "CREDIT";
  amount: number;
  balanceAfter: number;
  description?: string;
  createdAt: string;
}

export interface Statement {
  accountId: string;
  iban: string;
  openingBalance: number;
  closingBalance: number;
  fromDate: string;
  toDate: string;
  entries: LedgerEntry[];
  totalEntries: number;
  page: number;
  size: number;
}

// Pagination Types
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// API Error
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path?: string;
}

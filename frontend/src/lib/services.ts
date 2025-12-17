import { api } from "./api";
import {
  AuthResponse,
  AuthRequest,
  RegisterRequest,
  Customer,
  Account,
  Transfer,
  CreateTransferRequest,
  Statement,
  Page,
} from "@/types";

// Auth API
export const authApi = {
  login: (data: AuthRequest) =>
    api.post<AuthResponse>("/api/v1/auth/login", data, false),

  register: (data: RegisterRequest) =>
    api.post<AuthResponse>("/api/v1/auth/register", data, false),

  refresh: (refreshToken: string) =>
    api.post<AuthResponse>("/api/v1/auth/refresh", refreshToken, false),
};

// Customer API
export const customerApi = {
  getProfile: () => api.get<Customer>("/api/v1/me"),
};

// Account API
export const accountApi = {
  getMyAccounts: () => api.get<Account[]>("/api/v1/accounts"),

  getAccount: (id: string) => api.get<Account>(`/api/v1/accounts/${id}`),

  getBalance: (accountId: string) =>
    api.get<number>(`/api/v1/accounts/${accountId}/balance`),

  getStatement: (
    accountId: string,
    params?: { from?: string; to?: string; page?: number; size?: number }
  ) => {
    const searchParams = new URLSearchParams();
    if (params?.from) searchParams.set("from", params.from);
    if (params?.to) searchParams.set("to", params.to);
    if (params?.page !== undefined)
      searchParams.set("page", params.page.toString());
    if (params?.size !== undefined)
      searchParams.set("size", params.size.toString());

    const query = searchParams.toString();
    return api.get<Statement>(
      `/api/v1/accounts/${accountId}/statement${query ? `?${query}` : ""}`
    );
  },
};

// Transfer API
export const transferApi = {
  getMyTransfers: (page = 0, size = 20) =>
    api.get<Page<Transfer>>(`/api/v1/transfers?page=${page}&size=${size}`),

  getTransfer: (id: string) => api.get<Transfer>(`/api/v1/transfers/${id}`),

  createTransfer: (data: CreateTransferRequest, idempotencyKey?: string) =>
    api.post<Transfer>("/api/v1/transfers", data, true, {
      ...(idempotencyKey && { "Idempotency-Key": idempotencyKey }),
    }),
};

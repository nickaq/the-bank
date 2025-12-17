import { ApiError } from "@/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private getToken(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem("accessToken");
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const error: ApiError = await response.json().catch(() => ({
        message: "An unexpected error occurred",
        status: response.status,
        timestamp: new Date().toISOString(),
      }));
      throw error;
    }
    
    // Handle empty responses
    const text = await response.text();
    if (!text) return {} as T;
    return JSON.parse(text);
  }

  async get<T>(endpoint: string, authenticated = true): Promise<T> {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    if (authenticated) {
      const token = this.getToken();
      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: "GET",
      headers,
    });

    return this.handleResponse<T>(response);
  }

  async post<T, D = unknown>(
    endpoint: string,
    data?: D,
    authenticated = true,
    customHeaders?: Record<string, string>
  ): Promise<T> {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...customHeaders,
    };

    if (authenticated) {
      const token = this.getToken();
      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: "POST",
      headers,
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  async put<T, D = unknown>(endpoint: string, data: D): Promise<T> {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    const token = this.getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: "PUT",
      headers,
      body: JSON.stringify(data),
    });

    return this.handleResponse<T>(response);
  }

  async delete<T>(endpoint: string): Promise<T> {
    const headers: HeadersInit = {
      "Content-Type": "application/json",
    };

    const token = this.getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${this.baseUrl}${endpoint}`, {
      method: "DELETE",
      headers,
    });

    return this.handleResponse<T>(response);
  }
}

export const api = new ApiClient(API_URL);
export { API_URL };

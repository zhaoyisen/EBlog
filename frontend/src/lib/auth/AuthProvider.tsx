"use client";

import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { appConfig } from "../../config/appConfig";
import { getCsrfToken } from "./auth";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type AuthContextValue = {
  accessToken: string | null;
  ready: boolean;
  apiFetch: (input: string, init?: RequestInit) => Promise<Response>;
  loginWithPassword: (email: string, password: string) => Promise<{ ok: boolean; message?: string }>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function apiUrl(path: string) {
  const baseRaw = appConfig.apiBase;
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [ready, setReady] = useState(false);

  const refreshingRef = useRef<Promise<string | null> | null>(null);

  const csrfHeader = useCallback((): Record<string, string> => {
    const token = getCsrfToken();
    return token ? { "X-XSRF-TOKEN": token } : {};
  }, []);

  const warmUpCsrf = useCallback(async () => {
    try {
      await fetch(apiUrl("/api/v1/auth/csrf"), { cache: "no-store", credentials: "include" });
    } catch {
      // 忽略：离线或后端未启动时不阻断页面。
    }
  }, []);

  const refreshAccessToken = useCallback(async (): Promise<string | null> => {
    if (refreshingRef.current) {
      return refreshingRef.current;
    }

    const p = (async () => {
      try {
        const res = await fetch(apiUrl("/api/v1/auth/refresh"), {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            ...csrfHeader(),
          },
          credentials: "include",
        });
        if (!res.ok) {
          return null;
        }
        const json = (await res.json()) as ApiResponse<{ accessToken: string }>;
        if (json?.success && json.data?.accessToken) {
          setAccessToken(json.data.accessToken);
          return json.data.accessToken;
        }
        return null;
      } catch {
        return null;
      } finally {
        refreshingRef.current = null;
      }
    })();

    refreshingRef.current = p;
    return p;
  }, [csrfHeader]);

  const apiFetch = useCallback(
    async (input: string, init?: RequestInit) => {
      const url = apiUrl(input);

      const requestHeaders = new Headers(init?.headers);
      if (accessToken && !requestHeaders.has("authorization")) {
        requestHeaders.set("Authorization", `Bearer ${accessToken}`);
      }

      const mergedInit: RequestInit = {
        ...init,
        credentials: "include",
        headers: requestHeaders,
      };

      const res = await fetch(url, mergedInit);
      if (res.status !== 401) {
        return res;
      }

      const newToken = await refreshAccessToken();
      if (!newToken) {
        setAccessToken(null);
        return res;
      }

      const retryHeaders = new Headers(init?.headers);
      if (!retryHeaders.has("authorization")) {
        retryHeaders.set("Authorization", `Bearer ${newToken}`);
      }

      return fetch(url, {
        ...init,
        credentials: "include",
        headers: retryHeaders,
      });
    },
    [accessToken, refreshAccessToken]
  );

  const loginWithPassword = useCallback(
    async (email: string, password: string) => {
      try {
        await warmUpCsrf();

        const res = await fetch(apiUrl("/api/v1/auth/login"), {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            email: email.trim(),
            password: password.trim(),
          }),
        });
        const json = (await res.json()) as ApiResponse<{ accessToken: string }>;
        if (json?.success && json.data?.accessToken) {
          setAccessToken(json.data.accessToken);
          return { ok: true };
        }
        return { ok: false, message: json?.error?.message ?? "登录失败" };
      } catch {
        return { ok: false, message: "暂时无法登录" };
      }
    },
    [warmUpCsrf]
  );

  const logout = useCallback(async () => {
    try {
      await fetch(apiUrl("/api/v1/auth/logout"), {
        method: "POST",
        headers: {
          ...csrfHeader(),
        },
        credentials: "include",
      });
    } catch {
      // 忽略网络错误
    } finally {
      setAccessToken(null);
    }
  }, [csrfHeader]);

  useEffect(() => {
    (async () => {
      await warmUpCsrf();
      await refreshAccessToken();
      setReady(true);
    })();
  }, [refreshAccessToken, warmUpCsrf]);

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      ready,
      apiFetch,
      loginWithPassword,
      logout,
    }),
    [accessToken, apiFetch, loginWithPassword, logout, ready]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth 必须在 AuthProvider 内使用");
  }
  return ctx;
}

"use client";

import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import { appConfig } from "../../config/appConfig";
import { getCsrfToken, parseJwt } from "./auth";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type AuthContextValue = {
  accessToken: string | null;
  ready: boolean;
  isAuthenticated: boolean;
  userRole: string | null;
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

export function AuthProvider({
  children,
  initialAuth = false,
}: {
  children: React.ReactNode;
  initialAuth?: boolean;
}) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [ready, setReady] = useState(false);
  const [userRole, setUserRole] = useState<string | null>(null);

  // 核心优化：如果有 AccessToken，或（初始预判已登录且尚未完成验证），则视为“已登录”
  // 这样在 SSR 只有 refresh_token cookie 时，首屏就能直接渲染登录态 UI
  const isAuthenticated = !!accessToken || (initialAuth && !ready);

  const refreshingRef = useRef<Promise<string | null> | null>(null);

  const csrfHeader = useCallback((): Record<string, string> => {
    const token = getCsrfToken();
    console.log('[AuthProvider] Getting CSRF Token from cookie:', token);
    return token ? { "X-XSRF-TOKEN": token } : {};
  }, []);

  // 解析并更新角色信息
  useEffect(() => {
    if (accessToken) {
      const payload = parseJwt(accessToken);
      if (payload && typeof payload.role === "string") {
        setUserRole(payload.role);
      } else {
        setUserRole(null);
      }
    } else {
      setUserRole(null);
    }
  }, [accessToken]);

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
            ...csrfHeader(),
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
      // 确保在请求完成后清除 Cookie（虽然是 HttpOnly，但后端 Set-Cookie 会处理）
      // 我们可以手动尝试过期一些非 HttpOnly 的 Cookie 如果有的话，但这里主要是 refresh_token
    } catch {
      // 忽略网络错误
    } finally {
      setAccessToken(null);
      // 关键修正：虽然 initialAuth 是 prop 不会变，但我们需要确保在单页应用生命周期内
      // 用户登出后，不再依赖 initialAuth。
      // 不过由于 setAccessToken(null) 且 ready=true，isAuthenticated 逻辑本身是正确的。
      // 问题大概率在于后端 Cookie 清除响应头没被浏览器正确处理，或者并发请求导致 Cookie 又被种上了？
      // 或者就是页面跳转太快。
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
      isAuthenticated,
      userRole,
      apiFetch,
      loginWithPassword,
      logout,
    }),
    [accessToken, apiFetch, isAuthenticated, userRole, loginWithPassword, logout, ready]
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

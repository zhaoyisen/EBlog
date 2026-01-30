"use client";

import React, { useState } from "react";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

function apiUrl(path: string) {
  const baseRaw = process.env.NEXT_PUBLIC_API_BASE ?? "";
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  async function onLogin(e: React.FormEvent) {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      setError("请输入邮箱和密码");
      return;
    }

    setLoading(true);
    setError("");
    setSuccess(false);

    try {
      const res = await fetch(apiUrl("/api/v1/auth/login"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: email.trim(),
          password: password.trim(),
        }),
      });

      const json = (await res.json()) as ApiResponse<{
        access_token: string;
        refresh_token: string;
      }>;

      if (json?.success && json.data) {
        localStorage.setItem("access_token", json.data.access_token);
        localStorage.setItem("refresh_token", json.data.refresh_token);
        setSuccess(true);
        setTimeout(() => {
          window.location.href = "/";
        }, 1000);
      } else {
        setError(json?.error?.message ?? "登录失败");
      }
    } catch {
      setError("暂时无法登录");
    } finally {
      setLoading(false);
    }
  }

  function onLogout() {
    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    window.location.href = "/login";
  }

  // 检查是否已登录
  if (typeof window !== "undefined") {
    const accessToken = localStorage.getItem("access_token");
    if (accessToken) {
      window.location.href = "/";
    }
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-md">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">
            登录
          </h1>
          <p className="mt-2 text-sm text-neutral-600">
            使用您的邮箱和密码登录
          </p>
        </div>

        {success ? (
          <div className="rounded-2xl border border-green-200 bg-green-50 p-6">
            <p className="text-sm font-medium text-green-900">
              登录成功！正在跳转到首页...
            </p>
          </div>
        ) : null}

        {error && (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6">
            <p className="text-sm font-medium text-rose-900">
              {error}
            </p>
          </div>
        )}

        <div className="mt-6 rounded-2xl border border-black/10 bg-white/70 p-6">
          <form onSubmit={onLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-neutral-900">邮箱</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your@email.com"
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                disabled={loading}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-neutral-900">密码</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="your password"
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                disabled={loading}
                required
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white hover:bg-zinc-800 disabled:opacity-50"
            >
              {loading ? "登录中..." : "登录"}
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}

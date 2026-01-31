"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useAuth } from "../../lib/auth/AuthProvider";

export default function LoginPage() {
  const { accessToken, ready, loginWithPassword } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (ready && accessToken) {
      window.location.href = "/";
    }
  }, [accessToken, ready]);

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
      const result = await loginWithPassword(email, password);
      if (result.ok) {
        setSuccess(true);
        setTimeout(() => {
          window.location.href = "/";
        }, 1000);
      } else {
        setError(result.message ?? "登录失败");
      }
    } catch {
      setError("暂时无法登录");
    } finally {
      setLoading(false);
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

        <div className="mt-4 text-center text-sm text-neutral-600">
          还没有账号？
          <Link href="/register" className="ml-2 text-zinc-900 hover:underline">
            去注册
          </Link>
        </div>
      </div>
    </main>
  );
}

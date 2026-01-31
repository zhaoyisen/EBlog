"use client";

import React, { useState } from "react";
import Link from "next/link";

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

export default function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [inviteCode, setInviteCode] = useState("");
  const [emailCode, setEmailCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  async function onSendCode() {
    if (!email.trim()) {
      setError("请输入邮箱");
      return;
    }

    setSending(true);
    setError("");

    try {
      const res = await fetch(apiUrl("/api/v1/auth/email-code/send-register"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email: email.trim() }),
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        setError("验证码已发送，请查看后端日志");
      } else {
        setError(json?.error?.message ?? "发送验证码失败");
      }
    } catch {
      setError("发送验证码失败");
    } finally {
      setSending(false);
    }
  }

  async function onRegister(e: React.FormEvent) {
    e.preventDefault();

    if (!email.trim() || !password.trim() || !inviteCode.trim() || !emailCode.trim()) {
      setError("请填写完整信息");
      return;
    }

    setLoading(true);
    setError("");
    setSuccess(false);

    try {
      const res = await fetch(apiUrl("/api/v1/auth/register"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: email.trim(),
          password: password.trim(),
          inviteCode: inviteCode.trim(),
          emailCode: emailCode.trim(),
        }),
      });
      const json = (await res.json()) as ApiResponse<{ userId: number }>;
      if (json?.success) {
        setSuccess(true);
        setTimeout(() => {
          window.location.href = "/login";
        }, 1000);
      } else {
        setError(json?.error?.message ?? "注册失败");
      }
    } catch {
      setError("注册失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-md">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">注册</h1>
          <p className="mt-2 text-sm text-neutral-600">请输入邮箱、邀请码与验证码完成注册</p>
        </div>

        {success ? (
          <div className="rounded-2xl border border-green-200 bg-green-50 p-6">
            <p className="text-sm font-medium text-green-900">注册成功！正在跳转到登录页...</p>
          </div>
        ) : null}

        {error && (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6">
            <p className="text-sm font-medium text-rose-900">{error}</p>
          </div>
        )}

        <div className="mt-6 rounded-2xl border border-black/10 bg-white/70 p-6">
          <form onSubmit={onRegister} className="space-y-4" noValidate>
            <div>
              <label className="block text-sm font-medium text-neutral-900">邮箱</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your@email.com"
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                disabled={loading || sending}
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
                disabled={loading || sending}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-neutral-900">邀请码</label>
              <input
                type="text"
                value={inviteCode}
                onChange={(e) => setInviteCode(e.target.value)}
                placeholder="dev-invite-xxxx"
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                disabled={loading || sending}
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-neutral-900">邮箱验证码</label>
              <div className="mt-1 flex gap-2">
                <input
                  type="text"
                  value={emailCode}
                  onChange={(e) => setEmailCode(e.target.value)}
                  placeholder="6 位验证码"
                  className="flex-1 rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                  disabled={loading || sending}
                  required
                />
                <button
                  type="button"
                  onClick={onSendCode}
                  disabled={sending}
                  className="rounded-xl border border-black/10 bg-white px-4 py-3 text-sm font-medium text-zinc-900 hover:bg-zinc-50 disabled:opacity-50"
                >
                  {sending ? "发送中" : "发送验证码"}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white hover:bg-zinc-800 disabled:opacity-50"
            >
              {loading ? "注册中..." : "注册"}
            </button>
          </form>
        </div>

        <div className="mt-4 text-center text-sm text-neutral-600">
          已有账号？
          <Link href="/login" className="ml-2 text-zinc-900 hover:underline">
            去登录
          </Link>
        </div>
      </div>
    </main>
  );
}

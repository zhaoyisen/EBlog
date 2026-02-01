"use client";

import React, { useState } from "react";
import Link from "next/link";
import { toast } from "sonner";
import { appConfig } from "../../config/appConfig";
import { getCsrfToken } from "../../lib/auth/auth";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

function apiUrl(path: string) {
  const baseRaw = appConfig.apiBase;
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

  async function warmUpCsrf() {
    try {
      await fetch(apiUrl("/api/v1/auth/csrf"), { cache: "no-store", credentials: "include" });
    } catch {
      // ignore
    }
  }

  async function onSendCode() {
    if (!email.trim()) {
      toast.error("请输入邮箱");
      return;
    }

    setSending(true);

    try {
      await warmUpCsrf();
      const token = getCsrfToken();
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };
      if (token) {
        headers["X-XSRF-TOKEN"] = token;
      }

      const res = await fetch(apiUrl("/api/v1/auth/email-code/send-register"), {
        method: "POST",
        headers,
        body: JSON.stringify({ email: email.trim() }),
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        toast.success("验证码已发送，请查看后端日志");
      } else {
        toast.error(json?.error?.message ?? "发送验证码失败");
      }
    } catch {
      toast.error("发送验证码失败");
    } finally {
      setSending(false);
    }
  }

  async function onRegister(e: React.FormEvent) {
    e.preventDefault();

    if (!email.trim() || !password.trim() || !inviteCode.trim() || !emailCode.trim()) {
      toast.error("请填写完整信息");
      return;
    }

    setLoading(true);

    try {
      await warmUpCsrf();
      const token = getCsrfToken();
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };
      if (token) {
        headers["X-XSRF-TOKEN"] = token;
      }

      const res = await fetch(apiUrl("/api/v1/auth/register"), {
        method: "POST",
        headers,
        body: JSON.stringify({
          email: email.trim(),
          password: password.trim(),
          inviteCode: inviteCode.trim(),
          emailCode: emailCode.trim(),
        }),
      });
      const json = (await res.json()) as ApiResponse<{ userId: number }>;
      if (json?.success) {
        toast.success("注册成功！正在跳转到登录页...");
        setTimeout(() => {
          window.location.href = "/login";
        }, 1000);
      } else {
        toast.error(json?.error?.message ?? "注册失败");
      }
    } catch {
      toast.error("注册失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-muted/40 px-4 py-12 sm:px-6 lg:px-8">
      <div className="w-full max-w-md space-y-8 rounded-lg border bg-card p-8 shadow-sm">
        <div className="text-center">
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            创建账号
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            请输入邮箱、邀请码与验证码完成注册
          </p>
        </div>

        <form onSubmit={onRegister} className="space-y-4" noValidate>
          <div className="space-y-2">
            <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70" htmlFor="email">
              邮箱
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="name@example.com"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              disabled={loading || sending}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70" htmlFor="password">
              密码
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              disabled={loading || sending}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70" htmlFor="inviteCode">
              邀请码
            </label>
            <input
              id="inviteCode"
              type="text"
              value={inviteCode}
              onChange={(e) => setInviteCode(e.target.value)}
              placeholder="dev-invite-xxxx"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              disabled={loading || sending}
              required
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70" htmlFor="emailCode">
              邮箱验证码
            </label>
            <div className="flex gap-2">
              <input
                id="emailCode"
                type="text"
                value={emailCode}
                onChange={(e) => setEmailCode(e.target.value)}
                placeholder="6 位验证码"
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                disabled={loading || sending}
                required
              />
              <button
                type="button"
                onClick={onSendCode}
                disabled={sending}
                className="inline-flex h-10 items-center justify-center rounded-md border border-input bg-background px-4 py-2 text-sm font-medium ring-offset-background transition-colors hover:bg-accent hover:text-accent-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 whitespace-nowrap"
              >
                {sending ? "发送中..." : "发送验证码"}
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="inline-flex h-10 w-full items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground ring-offset-background transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 mt-2"
          >
            {loading ? (
              <>
                <svg className="mr-2 h-4 w-4 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                注册中...
              </>
            ) : (
              "注册"
            )}
          </button>
        </form>

        <div className="text-center text-sm text-muted-foreground">
          已有账号？{" "}
          <Link href="/login" className="font-medium text-primary underline-offset-4 hover:underline">
            去登录
          </Link>
        </div>
      </div>
    </div>
  );
}

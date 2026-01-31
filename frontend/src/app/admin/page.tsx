"use client";

import React, { useEffect } from "react";
import Link from "next/link";
import { useAuth } from "../../lib/auth/AuthProvider";
import { AdminNav } from "./_components/AdminNav";
import { AdminShell } from "./_components/AdminShell";

const cards = [
  {
    href: "/admin/users",
    title: "用户管理",
    desc: "查看用户列表，封禁或解封异常账号。",
  },
  {
    href: "/admin/invite-codes",
    title: "邀请码管理",
    desc: "批量生成邀请码，跟踪使用记录，按需吊销。",
  },
  {
    href: "/admin/moderation",
    title: "内容审核",
    desc: "处理文章与评论的人工审核，查看审核日志。",
  },
];

export default function AdminPage() {
  const { accessToken, ready } = useAuth();

  useEffect(() => {
    if (!ready) {
      return;
    }
    if (!accessToken) {
      window.location.href = "/login";
    }
  }, [accessToken, ready]);

  if (!ready) {
    return (
      <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
        <div className="mx-auto flex max-w-3xl items-center justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200"></div>
        </div>
      </main>
    );
  }

  if (!accessToken) {
    return null;
  }

  return (
    <AdminShell title="管理员控制台" description="集中处理用户、邀请码与内容审核。">
      <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <AdminNav />
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {cards.map((card) => (
          <Link
            key={card.href}
            href={card.href}
            className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur transition hover:-translate-y-0.5 hover:bg-white"
          >
            <div className="text-xs font-medium text-neutral-500">模块</div>
            <div className="mt-2 text-lg font-semibold text-neutral-900">{card.title}</div>
            <div className="mt-2 text-sm text-neutral-700">{card.desc}</div>
          </Link>
        ))}
      </div>
    </AdminShell>
  );
}

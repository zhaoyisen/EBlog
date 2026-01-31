"use client";

import React, { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../../lib/auth/AuthProvider";
import { AdminNav } from "../_components/AdminNav";
import { AdminShell } from "../_components/AdminShell";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type InviteCodeSummary = {
  code: string;
  status: string;
  maxUses: number;
  usedCount: number;
  expiresAt: string | null;
  createdAt: string | null;
  revokedAt: string | null;
};

type InviteCodeUseView = {
  usedByUserId: number;
  usedIp: string;
  usedAt: string | null;
};

export default function InviteCodesPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [codes, setCodes] = useState<InviteCodeSummary[]>([]);
  const [uses, setUses] = useState<InviteCodeUseView[]>([]);
  const [selectedCode, setSelectedCode] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState("");
  const [count, setCount] = useState(1);
  const [maxUses, setMaxUses] = useState(1);
  const [expiresAt, setExpiresAt] = useState("");
  const [revokeCode, setRevokeCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [createdCodes, setCreatedCodes] = useState<string[]>([]);

  const fetchCodes = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const params = new URLSearchParams({ limit: "50", offset: "0" });
      if (statusFilter.trim()) {
        params.set("status", statusFilter.trim());
      }
      const res = await apiFetch(`/api/v1/admin/invite-codes?${params.toString()}`, { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<InviteCodeSummary[]>;
      if (json?.success && Array.isArray(json.data)) {
        setCodes(json.data);
        return;
      }
      setError(json?.error?.message ?? "加载邀请码失败");
    } catch {
      setError("加载邀请码失败");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, statusFilter]);

  useEffect(() => {
    if (!ready) {
      return;
    }
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }
    void fetchCodes();
  }, [accessToken, fetchCodes, ready]);

  const handleCreate = async () => {
    if (count <= 0 || maxUses <= 0) {
      setError("生成数量与最大使用次数必须大于 0");
      return;
    }
    setLoading(true);
    setError("");
    setCreatedCodes([]);

    try {
      const res = await apiFetch("/api/v1/admin/invite-codes/batch-create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          count,
          maxUses,
          expiresAt: expiresAt ? expiresAt : null,
        }),
      });
      const json = (await res.json()) as ApiResponse<{ codes: string[] }>;
      if (json?.success && json.data?.codes) {
        setCreatedCodes(json.data.codes);
        await fetchCodes();
        return;
      }
      setError(json?.error?.message ?? "生成邀请码失败");
    } catch {
      setError("生成邀请码失败");
    } finally {
      setLoading(false);
    }
  };

  const handleRevoke = async () => {
    if (!revokeCode.trim()) {
      setError("请输入要吊销的邀请码");
      return;
    }
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch("/api/v1/admin/invite-codes/revoke", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code: revokeCode.trim() }),
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        setRevokeCode("");
        await fetchCodes();
        return;
      }
      setError(json?.error?.message ?? "吊销失败");
    } catch {
      setError("吊销失败");
    } finally {
      setLoading(false);
    }
  };

  const handleShowUses = async (code: string) => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/admin/invite-codes/${encodeURIComponent(code)}/uses?limit=50&offset=0`, {
        cache: "no-store",
      });
      const json = (await res.json()) as ApiResponse<InviteCodeUseView[]>;
      if (json?.success && Array.isArray(json.data)) {
        setSelectedCode(code);
        setUses(json.data);
        return;
      }
      setError(json?.error?.message ?? "加载使用记录失败");
    } catch {
      setError("加载使用记录失败");
    } finally {
      setLoading(false);
    }
  };

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
    <AdminShell title="邀请码管理" description="批量生成邀请码并追踪使用情况。">
      <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <AdminNav />
      </div>

      {error ? (
        <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
          {error}
        </div>
      ) : null}

      <div className="mt-6 grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="text-sm font-semibold text-neutral-900">批量生成</div>
          <div className="mt-4 grid gap-4">
            <div>
              <label className="block text-sm font-medium text-neutral-900" htmlFor="invite-count">
                生成数量
              </label>
              <input
                id="invite-count"
                type="number"
                min={1}
                value={count}
                onChange={(e) => setCount(Number(e.target.value))}
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-black/10"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-900" htmlFor="invite-max-uses">
                最大使用次数
              </label>
              <input
                id="invite-max-uses"
                type="number"
                min={1}
                value={maxUses}
                onChange={(e) => setMaxUses(Number(e.target.value))}
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-black/10"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-neutral-900" htmlFor="invite-expires">
                过期时间（可选）
              </label>
              <input
                id="invite-expires"
                type="datetime-local"
                value={expiresAt}
                onChange={(e) => setExpiresAt(e.target.value)}
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-black/10"
              />
            </div>
            <button
              onClick={handleCreate}
              className="rounded-xl bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-800"
              disabled={loading}
            >
              批量生成
            </button>
            {createdCodes.length ? (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-3 text-xs text-emerald-900">
                生成成功：{createdCodes.join(", ")}
              </div>
            ) : null}
          </div>
        </div>

        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="text-sm font-semibold text-neutral-900">吊销邀请码</div>
          <div className="mt-4 flex flex-col gap-3">
            <label className="text-sm font-medium text-neutral-900" htmlFor="revoke-code">
              邀请码
            </label>
            <input
              id="revoke-code"
              value={revokeCode}
              onChange={(e) => setRevokeCode(e.target.value)}
              className="rounded-xl border border-black/10 bg-white px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-black/10"
              placeholder="dev-invite-xxxx"
            />
            <button
              onClick={handleRevoke}
              className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-medium text-rose-900 hover:bg-rose-100"
              disabled={loading}
            >
              吊销
            </button>
          </div>
        </div>
      </div>

      <div className="mt-6 rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <div className="flex flex-wrap items-center gap-3">
          <div className="text-sm font-semibold text-neutral-900">邀请码列表</div>
          <select
            className="rounded-full border border-black/10 bg-white px-3 py-1 text-xs"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="">全部状态</option>
            <option value="ACTIVE">ACTIVE</option>
            <option value="USED">USED</option>
            <option value="EXPIRED">EXPIRED</option>
            <option value="REVOKED">REVOKED</option>
          </select>
          <button
            onClick={fetchCodes}
            className="rounded-full border border-black/10 bg-white px-3 py-1 text-xs font-medium text-neutral-700 hover:bg-zinc-50"
            disabled={loading}
          >
            刷新
          </button>
        </div>

        <div className="mt-4 grid gap-3">
          {loading ? (
            <div className="text-sm text-neutral-600">加载中...</div>
          ) : codes.length === 0 ? (
            <div className="text-sm text-neutral-600">暂无邀请码。</div>
          ) : (
            codes.map((code) => (
              <div key={code.code} className="rounded-2xl border border-black/10 bg-white px-4 py-3">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <div className="text-sm font-semibold text-neutral-900">{code.code}</div>
                    <div className="mt-1 text-xs text-neutral-500">
                      状态 {code.status} · 已用 {code.usedCount}/{code.maxUses}
                    </div>
                  </div>
                  <button
                    onClick={() => handleShowUses(code.code)}
                    className="rounded-full border border-black/10 bg-white px-3 py-1 text-xs font-medium text-neutral-700 hover:bg-zinc-50"
                    disabled={loading}
                  >
                    查看使用记录
                  </button>
                </div>
                <div className="mt-2 text-xs text-neutral-500">
                  创建 {code.createdAt ?? "-"} · 过期 {code.expiresAt ?? "-"} · 吊销 {code.revokedAt ?? "-"}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {selectedCode ? (
        <div className="mt-6 rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="text-sm font-semibold text-neutral-900">使用记录：{selectedCode}</div>
          <div className="mt-3 grid gap-2">
            {uses.length === 0 ? (
              <div className="text-sm text-neutral-600">暂无使用记录。</div>
            ) : (
              uses.map((use, idx) => (
                <div key={`${use.usedByUserId}-${idx}`} className="rounded-2xl border border-black/10 bg-white px-4 py-2 text-xs text-neutral-600">
                  用户 {use.usedByUserId} · {use.usedIp} · {use.usedAt ?? "-"}
                </div>
              ))
            )}
          </div>
        </div>
      ) : null}
    </AdminShell>
  );
}

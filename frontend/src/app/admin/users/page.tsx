"use client";

import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useAuth } from "../../../lib/auth/AuthProvider";
import { AdminNav } from "../_components/AdminNav";
import { AdminShell } from "../_components/AdminShell";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type UserListItem = {
  userId: number;
  nickname: string;
  role: string;
  isBanned: boolean;
  bannedReason: string | null;
  createdAt: string | null;
};

export default function AdminUsersPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [users, setUsers] = useState<UserListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [banReasons, setBanReasons] = useState<Record<number, string>>({});

  const formatDate = useMemo(() => {
    return (value: string | null) => {
      if (!value) {
        return "-";
      }
      return value.replace("T", " ").replace("Z", "");
    };
  }, []);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch("/api/v1/admin/users?limit=50&offset=0", { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<UserListItem[]>;
      if (json?.success && Array.isArray(json.data)) {
        setUsers(json.data);
        return;
      }
      setError(json?.error?.message ?? "加载用户列表失败");
    } catch {
      setError("加载用户列表失败");
    } finally {
      setLoading(false);
    }
  }, [apiFetch]);

  useEffect(() => {
    if (!ready) {
      return;
    }
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }
    void fetchUsers();
  }, [accessToken, fetchUsers, ready]);

  const handleBanReasonChange = (userId: number, value: string) => {
    setBanReasons((prev) => ({ ...prev, [userId]: value }));
  };

  const handleBan = async (userId: number) => {
    const reason = (banReasons[userId] ?? "").trim();
    if (!reason) {
      setError("请填写封禁原因");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/admin/users/ban/${userId}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ reason }),
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        await fetchUsers();
        return;
      }
      setError(json?.error?.message ?? "封禁失败");
    } catch {
      setError("封禁失败");
    } finally {
      setLoading(false);
    }
  };

  const handleUnban = async (userId: number) => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/admin/users/unban/${userId}`, {
        method: "POST",
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        await fetchUsers();
        return;
      }
      setError(json?.error?.message ?? "解封失败");
    } catch {
      setError("解封失败");
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
    <AdminShell title="用户管理" description="查看用户列表，并处理封禁或解封。">
      <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <AdminNav />
      </div>

      {error ? (
        <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
          {error}
        </div>
      ) : null}

      <div className="mt-6 overflow-hidden rounded-3xl border border-black/10 bg-white/70 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <div className="grid grid-cols-[1.2fr_0.8fr_0.7fr_1fr_1.3fr] gap-4 border-b border-black/10 bg-white/80 px-6 py-3 text-xs font-semibold text-neutral-600">
          <div>用户</div>
          <div>角色</div>
          <div>状态</div>
          <div>创建时间</div>
          <div>操作</div>
        </div>

        {loading ? (
          <div className="px-6 py-8 text-sm text-neutral-600">加载中...</div>
        ) : users.length === 0 ? (
          <div className="px-6 py-8 text-sm text-neutral-600">暂无用户数据。</div>
        ) : (
          <div className="divide-y divide-black/10">
            {users.map((user) => (
              <div key={user.userId} className="grid grid-cols-[1.2fr_0.8fr_0.7fr_1fr_1.3fr] gap-4 px-6 py-4 text-sm text-neutral-800">
                <div>
                  <div className="font-semibold text-neutral-900">{user.nickname || "未命名用户"}</div>
                  <div className="text-xs text-neutral-500">ID: {user.userId}</div>
                  {user.bannedReason ? (
                    <div className="mt-1 text-xs text-rose-600">原因：{user.bannedReason}</div>
                  ) : null}
                </div>
                <div>{user.role || "-"}</div>
                <div>
                  {user.isBanned ? (
                    <span className="rounded-full bg-rose-100 px-2 py-1 text-xs font-medium text-rose-800">已封禁</span>
                  ) : (
                    <span className="rounded-full bg-emerald-100 px-2 py-1 text-xs font-medium text-emerald-800">正常</span>
                  )}
                </div>
                <div className="text-xs text-neutral-500">{formatDate(user.createdAt)}</div>
                <div>
                  {user.isBanned ? (
                    <button
                      onClick={() => handleUnban(user.userId)}
                      className="rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-medium text-emerald-900 hover:bg-emerald-100"
                      disabled={loading}
                    >
                      解封
                    </button>
                  ) : (
                    <div className="flex flex-col gap-2">
                      <label className="text-xs text-neutral-500" htmlFor={`ban-reason-${user.userId}`}>
                        封禁原因
                      </label>
                      <input
                        id={`ban-reason-${user.userId}`}
                        aria-label="封禁原因"
                        value={banReasons[user.userId] ?? ""}
                        onChange={(e) => handleBanReasonChange(user.userId, e.target.value)}
                        className="rounded-xl border border-black/10 bg-white px-3 py-2 text-xs outline-none focus:ring-2 focus:ring-black/10"
                        placeholder="填写原因"
                      />
                      <button
                        onClick={() => handleBan(user.userId)}
                        className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-medium text-rose-900 hover:bg-rose-100"
                        disabled={loading}
                      >
                        封禁
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </AdminShell>
  );
}

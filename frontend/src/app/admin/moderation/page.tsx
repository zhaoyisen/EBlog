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

type ModerationItem = {
  postId: number;
  title: string;
  authorId: number;
  moderationStatus: string;
  createdAt: string | null;
};

type CommentModerationItem = {
  commentId: number;
  postId: number;
  authorId: number;
  content: string;
  moderationStatus: string;
  createdAt: string | null;
};

type AuditLogItem = {
  id: number;
  entityType: string;
  entityId: number;
  actorId: number;
  action: string;
  reason: string | null;
  ruleHit: string | null;
  createdAt: string | null;
};

export default function ModerationPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [posts, setPosts] = useState<ModerationItem[]>([]);
  const [comments, setComments] = useState<CommentModerationItem[]>([]);
  const [logs, setLogs] = useState<AuditLogItem[]>([]);
  const [postReasons, setPostReasons] = useState<Record<number, string>>({});
  const [commentReasons, setCommentReasons] = useState<Record<number, string>>({});
  const [logEntityType, setLogEntityType] = useState("");
  const [logEntityId, setLogEntityId] = useState("");
  const [logLimit, setLogLimit] = useState("50");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const formatDate = useMemo(() => {
    return (value: string | null) => {
      if (!value) {
        return "-";
      }
      return value.replace("T", " ").replace("Z", "");
    };
  }, []);

  const fetchQueues = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const [postRes, commentRes] = await Promise.all([
        apiFetch("/api/v1/admin/moderation/review-queue?limit=20&offset=0", { cache: "no-store" }),
        apiFetch("/api/v1/admin/moderation/review-queue/comments?limit=20&offset=0", { cache: "no-store" }),
      ]);

      const postJson = (await postRes.json()) as ApiResponse<ModerationItem[]>;
      const commentJson = (await commentRes.json()) as ApiResponse<CommentModerationItem[]>;

      if (postJson?.success && Array.isArray(postJson.data)) {
        setPosts(postJson.data);
      } else {
        setError(postJson?.error?.message ?? "加载审核队列失败");
      }

      if (commentJson?.success && Array.isArray(commentJson.data)) {
        setComments(commentJson.data);
      } else {
        setError(commentJson?.error?.message ?? "加载评论审核队列失败");
      }
    } catch {
      setError("加载审核队列失败");
    } finally {
      setLoading(false);
    }
  }, [apiFetch]);

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const params = new URLSearchParams();
      if (logEntityType.trim()) {
        params.set("entityType", logEntityType.trim());
      }
      if (logEntityId.trim()) {
        params.set("entityId", logEntityId.trim());
      }
      params.set("limit", logLimit || "50");

      const res = await apiFetch(`/api/v1/admin/moderation/audit-logs?${params.toString()}`, { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<AuditLogItem[]>;
      if (json?.success && Array.isArray(json.data)) {
        setLogs(json.data);
        return;
      }
      setError(json?.error?.message ?? "加载审核日志失败");
    } catch {
      setError("加载审核日志失败");
    } finally {
      setLoading(false);
    }
  }, [apiFetch, logEntityId, logEntityType, logLimit]);

  useEffect(() => {
    if (!ready) {
      return;
    }
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }
    void fetchQueues();
  }, [accessToken, fetchQueues, ready]);

  const handlePostReasonChange = (postId: number, value: string) => {
    setPostReasons((prev) => ({ ...prev, [postId]: value }));
  };

  const handleCommentReasonChange = (commentId: number, value: string) => {
    setCommentReasons((prev) => ({ ...prev, [commentId]: value }));
  };

  const handlePostDecision = async (postId: number, action: "approve" | "reject") => {
    const reason = (postReasons[postId] ?? "").trim();
    if (!reason) {
      setError("请填写审核原因");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/admin/moderation/${action}/${postId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason }),
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        await fetchQueues();
        return;
      }
      setError(json?.error?.message ?? "审核操作失败");
    } catch {
      setError("审核操作失败");
    } finally {
      setLoading(false);
    }
  };

  const handleCommentDecision = async (commentId: number, action: "approve" | "reject") => {
    const reason = (commentReasons[commentId] ?? "").trim();
    if (!reason) {
      setError("请填写审核原因");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/admin/moderation/${action}/comment/${commentId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason }),
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        await fetchQueues();
        return;
      }
      setError(json?.error?.message ?? "审核操作失败");
    } catch {
      setError("审核操作失败");
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
    <AdminShell title="内容审核" description="人工审核文章与评论，必要时查看审核日志。">
      <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <AdminNav />
      </div>

      {error ? (
        <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
          {error}
        </div>
      ) : null}

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="text-sm font-semibold text-neutral-900">文章审核队列</div>
          <div className="mt-4 grid gap-4">
            {loading ? (
              <div className="text-sm text-neutral-600">加载中...</div>
            ) : posts.length === 0 ? (
              <div className="text-sm text-neutral-600">暂无待审文章。</div>
            ) : (
              posts.map((post) => (
                <div key={post.postId} className="rounded-2xl border border-black/10 bg-white p-4">
                  <div className="text-sm font-semibold text-neutral-900">{post.title}</div>
                  <div className="mt-1 text-xs text-neutral-500">
                    作者 {post.authorId} · {post.moderationStatus} · {formatDate(post.createdAt)}
                  </div>
                  <div className="mt-3 grid gap-2">
                    <label className="text-xs text-neutral-500" htmlFor={`post-reason-${post.postId}`}>
                      审核原因
                    </label>
                    <input
                      id={`post-reason-${post.postId}`}
                      aria-label="审核原因"
                      value={postReasons[post.postId] ?? ""}
                      onChange={(e) => handlePostReasonChange(post.postId, e.target.value)}
                      className="rounded-xl border border-black/10 bg-white px-3 py-2 text-xs outline-none focus:ring-2 focus:ring-black/10"
                      placeholder="填写原因"
                    />
                    <div className="flex gap-2">
                      <button
                        onClick={() => handlePostDecision(post.postId, "approve")}
                        className="rounded-xl bg-emerald-600 px-3 py-2 text-xs font-medium text-white hover:bg-emerald-700"
                        disabled={loading}
                      >
                        通过
                      </button>
                      <button
                        onClick={() => handlePostDecision(post.postId, "reject")}
                        className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-medium text-rose-900 hover:bg-rose-100"
                        disabled={loading}
                      >
                        拒绝
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="text-sm font-semibold text-neutral-900">评论审核队列</div>
          <div className="mt-4 grid gap-4">
            {loading ? (
              <div className="text-sm text-neutral-600">加载中...</div>
            ) : comments.length === 0 ? (
              <div className="text-sm text-neutral-600">暂无待审评论。</div>
            ) : (
              comments.map((comment) => (
                <div key={comment.commentId} className="rounded-2xl border border-black/10 bg-white p-4">
                  <div className="text-sm font-semibold text-neutral-900">评论 #{comment.commentId}</div>
                  <div className="mt-1 text-xs text-neutral-500">
                    文章 {comment.postId} · 作者 {comment.authorId} · {comment.moderationStatus}
                  </div>
                  <div className="mt-2 text-sm text-neutral-700">{comment.content}</div>
                  <div className="mt-1 text-xs text-neutral-500">{formatDate(comment.createdAt)}</div>
                  <div className="mt-3 grid gap-2">
                    <label className="text-xs text-neutral-500" htmlFor={`comment-reason-${comment.commentId}`}>
                      审核原因
                    </label>
                    <input
                      id={`comment-reason-${comment.commentId}`}
                      value={commentReasons[comment.commentId] ?? ""}
                      onChange={(e) => handleCommentReasonChange(comment.commentId, e.target.value)}
                      className="rounded-xl border border-black/10 bg-white px-3 py-2 text-xs outline-none focus:ring-2 focus:ring-black/10"
                      placeholder="填写原因"
                    />
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleCommentDecision(comment.commentId, "approve")}
                        className="rounded-xl bg-emerald-600 px-3 py-2 text-xs font-medium text-white hover:bg-emerald-700"
                        disabled={loading}
                      >
                        通过
                      </button>
                      <button
                        onClick={() => handleCommentDecision(comment.commentId, "reject")}
                        className="rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-medium text-rose-900 hover:bg-rose-100"
                        disabled={loading}
                      >
                        拒绝
                      </button>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <div className="mt-6 rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <div className="text-sm font-semibold text-neutral-900">审核日志</div>
        <div className="mt-4 grid gap-3 md:grid-cols-4">
          <div>
            <label className="block text-xs font-medium text-neutral-600">实体类型</label>
            <input
              value={logEntityType}
              onChange={(e) => setLogEntityType(e.target.value)}
              className="mt-1 w-full rounded-xl border border-black/10 bg-white px-3 py-2 text-xs outline-none focus:ring-2 focus:ring-black/10"
              placeholder="POST / COMMENT"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-neutral-600">实体 ID</label>
            <input
              value={logEntityId}
              onChange={(e) => setLogEntityId(e.target.value)}
              className="mt-1 w-full rounded-xl border border-black/10 bg-white px-3 py-2 text-xs outline-none focus:ring-2 focus:ring-black/10"
              placeholder="123"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-neutral-600">数量</label>
            <input
              value={logLimit}
              onChange={(e) => setLogLimit(e.target.value)}
              className="mt-1 w-full rounded-xl border border-black/10 bg-white px-3 py-2 text-xs outline-none focus:ring-2 focus:ring-black/10"
              placeholder="50"
            />
          </div>
          <div className="flex items-end">
            <button
              onClick={fetchLogs}
              className="w-full rounded-xl border border-black/10 bg-white px-4 py-2 text-xs font-medium text-neutral-800 hover:bg-zinc-50"
              disabled={loading}
            >
              查询日志
            </button>
          </div>
        </div>

        <div className="mt-4 grid gap-2">
          {logs.length === 0 ? (
            <div className="text-xs text-neutral-500">暂无日志数据。</div>
          ) : (
            logs.map((log) => (
              <div key={log.id} className="rounded-2xl border border-black/10 bg-white px-4 py-2 text-xs text-neutral-700">
                <div>
                  {log.entityType} #{log.entityId} · {log.action} · 操作人 {log.actorId}
                </div>
                <div className="text-neutral-500">原因：{log.reason ?? "-"} · 规则：{log.ruleHit ?? "-"}</div>
                <div className="text-neutral-400">{formatDate(log.createdAt)}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </AdminShell>
  );
}

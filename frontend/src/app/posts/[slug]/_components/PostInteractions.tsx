"use client";

import React, { useEffect, useState, useCallback, useMemo } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import { parseJwt } from "@/lib/auth/auth";
import { Heart, Star, MessageSquare, Send, User } from "lucide-react";
import { toast } from "sonner";

type CommentDetail = {
  id: string;
  postId: string;
  authorId: string;
  content: string;
  createdAt: string;
};

type LikeStatus = {
  count: number;
  liked: boolean;
};

type FavoriteStatus = {
  favorited: boolean;
};

export default function PostInteractions({ postId }: { postId: string }) {
  const { accessToken, apiFetch, isAuthenticated } = useAuth();
  const [comments, setComments] = useState<CommentDetail[]>([]);
  const [likeStatus, setLikeStatus] = useState<LikeStatus>({ count: 0, liked: false });
  const [favoriteStatus, setFavoriteStatus] = useState<FavoriteStatus>({ favorited: false });
  const [loading, setLoading] = useState(true);
  const [commentContent, setCommentContent] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentUserId = useMemo(() => {
    if (!accessToken) return undefined;
    const payload = parseJwt(accessToken);
    return payload?.sub ? String(payload.sub) : undefined;
  }, [accessToken]);

  const fetchComments = useCallback(async () => {
    try {
      const res = await apiFetch(`/api/v1/posts/${postId}/comments?limit=20&offset=0`);
      if (res.ok) {
        const json = await res.json();
        if (json.success) {
          setComments(json.data || []);
        }
      }
    } catch (e) {
      console.error("Failed to fetch comments", e);
    }
  }, [postId, apiFetch]);

  const fetchLikeStatus = useCallback(async () => {
    try {
      const url = currentUserId
        ? `/api/v1/posts/${postId}/likes?userId=${currentUserId}`
        : `/api/v1/posts/${postId}/likes`;
      const res = await apiFetch(url);
      if (res.ok) {
        const json = await res.json();
        if (json.success) {
          setLikeStatus(json.data);
        }
      }
    } catch (e) {
      console.error("Failed to fetch like status", e);
    }
  }, [postId, apiFetch, currentUserId]);

  const fetchFavoriteStatus = useCallback(async () => {
    try {
      const url = currentUserId
        ? `/api/v1/posts/${postId}/favorites?userId=${currentUserId}`
        : `/api/v1/posts/${postId}/favorites`;
      const res = await apiFetch(url);
      if (res.ok) {
        const json = await res.json();
        if (json.success) {
          setFavoriteStatus(json.data);
        }
      }
    } catch (e) {
      console.error("Failed to fetch favorite status", e);
    }
  }, [postId, apiFetch, currentUserId]);

  const refreshAll = useCallback(async () => {
    setLoading(true);
    await Promise.all([fetchComments(), fetchLikeStatus(), fetchFavoriteStatus()]);
    setLoading(false);
  }, [fetchComments, fetchLikeStatus, fetchFavoriteStatus]);

  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  const handleLike = async () => {
    if (!isAuthenticated) return;

    const previousStatus = likeStatus;
    const newLiked = !likeStatus.liked;
    const newCount = newLiked ? likeStatus.count + 1 : Math.max(0, likeStatus.count - 1);

    // Optimistic update
    setLikeStatus({ liked: newLiked, count: newCount });

    try {
      const method = previousStatus.liked ? "DELETE" : "POST";
      const res = await apiFetch(`/api/v1/posts/${postId}/likes`, { method });
      
      if (!res.ok) {
        throw new Error("Network response was not ok");
      }

      const json = await res.json();
      if (!json.success) {
        throw new Error(json.error?.message || "操作失败");
      }
    } catch (e) {
      // Rollback
      setLikeStatus(previousStatus);
      toast.error("点赞失败，请重试");
    }
  };

  const handleFavorite = async () => {
    if (!isAuthenticated) return;

    const previousStatus = favoriteStatus;
    const newFavorited = !favoriteStatus.favorited;

    // Optimistic update
    setFavoriteStatus({ favorited: newFavorited });

    try {
      const method = previousStatus.favorited ? "DELETE" : "POST";
      const res = await apiFetch(`/api/v1/posts/${postId}/favorites`, { method });
      
      if (!res.ok) {
        throw new Error("Network response was not ok");
      }

      const json = await res.json();
      if (!json.success) {
        throw new Error(json.error?.message || "操作失败");
      }
    } catch (e) {
      // Rollback
      setFavoriteStatus(previousStatus);
      toast.error("收藏失败，请重试");
    }
  };

  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated || !commentContent.trim()) return;
    setError(null);
    setSubmitting(true);
    try {
      const res = await apiFetch(`/api/v1/posts/${postId}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content: commentContent }),
      });
      if (res.ok) {
        const json = await res.json();
        if (json.success) {
          setCommentContent("");
          fetchComments();
        } else {
          setError(json.error?.message || "评论失败");
        }
      } else {
        setError("网络请求失败");
      }
    } catch (e) {
      setError("发生错误");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-12">
      {/* Action Bar */}
      <div className="flex items-center justify-between rounded-2xl border border-neutral-100 bg-neutral-50/50 p-4">
        <div className="flex items-center gap-4">
          <button
            onClick={handleLike}
            disabled={!isAuthenticated}
            className={`group flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all ${
              likeStatus.liked
                ? "bg-rose-50 text-rose-600 ring-1 ring-inset ring-rose-200"
                : "bg-white text-neutral-600 shadow-sm ring-1 ring-inset ring-neutral-200 hover:bg-neutral-50"
            } ${!isAuthenticated ? "cursor-not-allowed opacity-50" : ""}`}
            title={!isAuthenticated ? "登录后点赞" : ""}
          >
            <Heart
              className={`h-4 w-4 transition-transform group-active:scale-90 ${
                likeStatus.liked ? "fill-current" : ""
              }`}
            />
            <span>{likeStatus.count > 0 ? likeStatus.count : "点赞"}</span>
          </button>

          <button
            onClick={handleFavorite}
            disabled={!isAuthenticated}
            className={`group flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all ${
              favoriteStatus.favorited
                ? "bg-amber-50 text-amber-600 ring-1 ring-inset ring-amber-200"
                : "bg-white text-neutral-600 shadow-sm ring-1 ring-inset ring-neutral-200 hover:bg-neutral-50"
            } ${!isAuthenticated ? "cursor-not-allowed opacity-50" : ""}`}
            title={!isAuthenticated ? "登录后收藏" : ""}
          >
            <Star
              className={`h-4 w-4 transition-transform group-active:scale-90 ${
                favoriteStatus.favorited ? "fill-current" : ""
              }`}
            />
            <span>{favoriteStatus.favorited ? "已收藏" : "收藏"}</span>
          </button>
        </div>

        <div className="flex items-center gap-2 text-sm text-neutral-500">
          <MessageSquare className="h-4 w-4" />
          <span>{comments.length} 条评论</span>
        </div>
      </div>

      {error && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
          {error}
        </div>
      )}

      {/* Comments Section */}
      <div className="space-y-8">
        <h3 className="text-lg font-bold text-neutral-900">评论区</h3>

        {/* Comment Form */}
        {isAuthenticated ? (
          <form onSubmit={handleCommentSubmit} className="relative">
            <div className="overflow-hidden rounded-2xl border border-neutral-200 bg-white shadow-sm focus-within:border-neutral-900 focus-within:ring-1 focus-within:ring-neutral-900">
              <textarea
                value={commentContent}
                onChange={(e) => setCommentContent(e.target.value)}
                placeholder="分享你的想法..."
                className="block w-full resize-none border-0 bg-transparent p-4 text-sm text-neutral-900 placeholder:text-neutral-400 focus:ring-0"
                rows={3}
                disabled={submitting}
              />
              <div className="flex items-center justify-between border-t border-neutral-100 bg-neutral-50 px-4 py-2">
                <span className="text-xs text-neutral-400">支持 Markdown</span>
                <button
                  type="submit"
                  disabled={submitting || !commentContent.trim()}
                  className="inline-flex items-center gap-2 rounded-lg bg-neutral-900 px-4 py-1.5 text-xs font-medium text-white transition-colors hover:bg-neutral-800 disabled:opacity-50"
                >
                  {submitting ? (
                    "发送中..."
                  ) : (
                    <>
                      发送 <Send className="h-3 w-3" />
                    </>
                  )}
                </button>
              </div>
            </div>
          </form>
        ) : (
          <div className="rounded-2xl border border-dashed border-neutral-200 bg-neutral-50 p-8 text-center">
            <p className="text-sm text-neutral-500">登录后参与讨论</p>
            <a
              href="/login"
              className="mt-4 inline-block rounded-lg bg-white px-4 py-2 text-sm font-medium text-neutral-900 shadow-sm ring-1 ring-inset ring-neutral-200 hover:bg-neutral-50"
            >
              去登录
            </a>
          </div>
        )}

        {/* Comments List */}
        <div className="space-y-6">
          {loading ? (
            <div className="py-12 text-center">
              <div className="inline-block h-6 w-6 animate-spin rounded-full border-2 border-neutral-200 border-t-neutral-900"></div>
            </div>
          ) : comments.length > 0 ? (
            comments.map((comment) => (
              <div key={comment.id} className="flex gap-4 group">
                <div className="flex-shrink-0">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-neutral-100 text-neutral-500 ring-1 ring-inset ring-neutral-200">
                    <User className="h-5 w-5" />
                  </div>
                </div>
                <div className="flex-1 space-y-1">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-semibold text-neutral-900">
                        用户 #{comment.authorId}
                      </span>
                      <span className="text-xs text-neutral-400">
                        {new Date(comment.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                  <div className="rounded-2xl rounded-tl-none bg-neutral-50 px-4 py-3 text-sm text-neutral-700 ring-1 ring-inset ring-neutral-200/50">
                    {comment.content}
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="py-12 text-center text-sm text-neutral-500">
              暂无评论，来抢沙发吧！
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

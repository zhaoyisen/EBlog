"use client";

import React, { useState, useEffect } from "react";
import { useAuth } from "../../../lib/auth/AuthProvider";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type Category = {
  id: number;
  name: string;
};

export default function PostNewPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [tagsCsv, setTagsCsv] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [categories, setCategories] = useState<Category[]>([]);

  useEffect(() => {
    if (ready && !accessToken) {
      window.location.href = "/login";
      return;
    }

    async function loadCategories() {
      try {
        const res = await apiFetch("/api/v1/categories", { cache: "no-store" });
        const json = (await res.json()) as ApiResponse<Category[]>;
        if (json?.success && json.data) {
          setCategories(json.data);
        }
      } catch {
        console.error("加载分类失败");
      }
    }

    loadCategories();
  }, [accessToken, apiFetch, ready]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (!title.trim() || !content.trim()) {
      setError("标题和内容不能为空");
      return;
    }

    setLoading(true);
    setError("");

    try {
      if (!ready || !accessToken) {
        setError("请先登录");
        return;
      }

      const res = await apiFetch("/api/v1/posts", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          title: title.trim(),
          content: content.trim(),
          categoryId: categoryId ? parseInt(categoryId) : undefined,
          tagsCsv: tagsCsv.trim(),
        }),
      });

      const json = (await res.json()) as ApiResponse<{ id: number; slug: string }>;
      if (json?.success && json.data) {
        window.location.href = `/posts/${json.data.slug}`;
      } else {
        setError(json?.error?.message ?? "发布文章失败");
      }
    } catch {
      setError("发布文章失败");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight text-neutral-900">
            创建文章
          </h1>
          <p className="mt-2 text-sm text-neutral-600">
            填写文章内容，选择分类，添加标签
          </p>
        </div>

        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <form onSubmit={onSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-neutral-900">标题</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="请输入文章标题"
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                disabled={loading}
                required
              />

              <label className="block text-sm font-medium text-neutral-900">内容</label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="支持 Markdown 语法"
                rows={10}
                className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                disabled={loading}
                required
              />

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-neutral-900">分类</label>
                  <select
                    value={categoryId}
                    onChange={(e) => setCategoryId(e.target.value)}
                    className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    disabled={loading}
                  >
                    <option value="">请选择分类</option>
                    {categories.map((cat) => (
                      <option key={cat.id} value={cat.id.toString()}>
                        {cat.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-neutral-900">标签</label>
                  <input
                    type="text"
                    value={tagsCsv}
                    onChange={(e) => setTagsCsv(e.target.value)}
                    placeholder="标签之间用逗号分隔"
                    className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    disabled={loading}
                  />
                </div>
              </div>

            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white hover:bg-zinc-800 disabled:opacity-50"
            >
              {loading ? "发布中..." : "发布文章"}
            </button>
          </form>

          {error && (
            <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-6">
              <div className="text-sm font-medium text-rose-900">{error}</div>
            </div>
          )}

          {loading && (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-4 border-zinc-200"></div>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}

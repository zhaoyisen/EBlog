"use client";

import React, { useState, useEffect } from "react";
import { useAuth } from "../../../lib/auth/AuthProvider";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

export default function PostNewPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [content, setContent] = useState("");
  const [category, setCategory] = useState("");
  const [tagsCsv, setTagsCsv] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (ready && !accessToken) {
      window.location.href = "/login";
      return;
    }
  }, [accessToken, ready]);

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
          summary: summary.trim(),
          contentMarkdown: content.trim(),
          tagsCsv: tagsCsv.trim(),
          category: category.trim(),
          status: "PUBLISHED",
          format: "MARKDOWN",
        }),
      });

      const json = (await res.json()) as ApiResponse<{ postId: number; slug: string }>;
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
    <main className="min-h-[calc(100vh-4rem)] bg-neutral-50/50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-4xl">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight text-neutral-900">
            创建新文章
          </h1>
          <p className="mt-2 text-neutral-600">
            分享你的知识和见解。支持 Markdown 格式。
          </p>
        </div>

        <div className="overflow-hidden rounded-2xl border border-neutral-200 bg-white shadow-sm">
          <form onSubmit={onSubmit} className="divide-y divide-neutral-100">
            <div className="p-6 sm:p-8 space-y-6">
              {/* Title */}
              <div>
                <label htmlFor="title" className="block text-sm font-medium text-neutral-900">
                  文章标题 <span className="text-rose-500">*</span>
                </label>
                <input
                  id="title"
                  type="text"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="输入引人注目的标题"
                  className="mt-2 block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm"
                  disabled={loading}
                  required
                />
              </div>

              {/* Summary */}
              <div>
                <label htmlFor="summary" className="block text-sm font-medium text-neutral-900">
                  摘要 <span className="text-neutral-400 font-normal">(可选)</span>
                </label>
                <textarea
                  id="summary"
                  value={summary}
                  onChange={(e) => setSummary(e.target.value)}
                  placeholder="简短描述文章的主要内容..."
                  rows={2}
                  className="mt-2 block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm resize-none"
                  disabled={loading}
                />
              </div>

              {/* Content */}
              <div>
                <label htmlFor="content" className="block text-sm font-medium text-neutral-900">
                  正文内容 <span className="text-rose-500">*</span>
                </label>
                <div className="mt-2 rounded-lg border border-neutral-200 bg-neutral-50 focus-within:border-neutral-900 focus-within:bg-white focus-within:ring-1 focus-within:ring-neutral-900">
                  <textarea
                    id="content"
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="# 开始你的创作..."
                    rows={15}
                    className="block w-full rounded-lg border-0 bg-transparent px-4 py-3 font-mono text-sm text-neutral-900 placeholder:text-neutral-400 focus:ring-0"
                    disabled={loading}
                    required
                  />
                  <div className="border-t border-neutral-200 bg-neutral-50 px-4 py-2 text-xs text-neutral-500 flex justify-between items-center rounded-b-lg">
                    <span>支持 Markdown</span>
                    <span>{content.length} 字符</span>
                  </div>
                </div>
              </div>

              {/* Meta */}
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                <div>
                  <label htmlFor="category" className="block text-sm font-medium text-neutral-900">
                    分类
                  </label>
                  <input
                    id="category"
                    type="text"
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    placeholder="例如：技术"
                    className="mt-2 block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm"
                    disabled={loading}
                  />
                </div>

                <div>
                  <label htmlFor="tags" className="block text-sm font-medium text-neutral-900">
                    标签
                  </label>
                  <input
                    id="tags"
                    type="text"
                    value={tagsCsv}
                    onChange={(e) => setTagsCsv(e.target.value)}
                    placeholder="React, Next.js, Tutorial"
                    className="mt-2 block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm"
                    disabled={loading}
                  />
                  <p className="mt-1 text-xs text-neutral-500">使用逗号分隔多个标签</p>
                </div>
              </div>
            </div>

            {/* Footer */}
            <div className="bg-neutral-50 px-6 py-4 sm:px-8 flex items-center justify-between">
              <div className="text-sm text-rose-600 font-medium">
                {error && error}
              </div>
              <button
                type="submit"
                disabled={loading}
                className="inline-flex items-center justify-center rounded-lg bg-neutral-900 px-6 py-2.5 text-sm font-medium text-white shadow-sm hover:bg-neutral-800 focus:outline-none focus:ring-2 focus:ring-neutral-900 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
              >
                {loading ? (
                  <>
                    <svg className="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    发布中...
                  </>
                ) : (
                  "发布文章"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>
  );
}

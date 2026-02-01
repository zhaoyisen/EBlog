"use client";

import React, { useState, useEffect } from "react";
import { useAuth } from "../../../../lib/auth/AuthProvider";
import dynamic from "next/dynamic";
import Link from "next/link";

// 动态导入 ByteMDEditor 以避免 SSR 问题 (编辑器依赖 window)
const ByteMDEditor = dynamic(() => import("../../../../components/ByteMDEditor"), {
  ssr: false,
  loading: () => (
    <div className="flex items-center justify-center p-12 min-h-[500px] border border-neutral-200 rounded-lg bg-neutral-50">
      <div className="text-neutral-500">编辑器加载中...</div>
    </div>
  ),
});

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type PostDetail = {
  id: number;
  title: string;
  summary: string;
  contentMarkdown: string;
  tagsCsv: string;
  category: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  format: string;
};

export default function PostEditPage({ params }: { params: Promise<{ id: string }> }) {
  const { accessToken, ready, apiFetch } = useAuth();
  const [postId, setPostId] = useState<number | null>(null);

  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [content, setContent] = useState("");
  const [category, setCategory] = useState("");
  const [tagsCsv, setTagsCsv] = useState("");
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  // 提取 params.id
  useEffect(() => {
    void (async () => {
      const resolvedParams = await params;
      setPostId(parseInt(resolvedParams.id, 10));
    })();
  }, [params]);

  // 加载文章数据
  useEffect(() => {
    if (!ready) return;
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }

    if (!postId) return;

    const loadPost = async () => {
      setPageLoading(true);
      try {
        const res = await apiFetch(`/api/v1/posts/${postId}`, { cache: "no-store" });
        const json = (await res.json()) as ApiResponse<PostDetail>;
        if (json?.success && json.data) {
          const post = json.data;
          setTitle(post.title || "");
          setSummary(post.summary || "");
          setContent(post.contentMarkdown || "");
          setCategory(post.category || "");
          setTagsCsv(post.tagsCsv || "");
        } else {
          setError(json?.error?.message ?? "加载文章失败");
        }
      } catch {
        setError("加载文章失败");
      } finally {
        setPageLoading(false);
      }
    };

    void loadPost();
  }, [ready, accessToken, postId, apiFetch]);

  const uploadImages = async (files: File[]) => {
    if (!accessToken) {
       setError("请先登录再上传图片");
       return [];
    }

    try {
      const uploads = await Promise.all(
        files.map(async (file) => {
          // 1. 获取预签名 URL
          const presignRes = await apiFetch("/api/v1/uploads/presign", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              filename: file.name,
              contentType: file.type,
              sizeBytes: file.size,
            }),
          });
          
          const presignJson = await presignRes.json();
          if (!presignJson.success || !presignJson.data) {
            throw new Error(presignJson.error?.message || "获取上传链接失败");
          }

          const { uploadUrl, publicUrl } = presignJson.data;

          // 2. 上传文件到 MinIO (通过预签名 URL)
          await fetch(uploadUrl, {
            method: "PUT",
            body: file,
            headers: {
              "Content-Type": file.type,
            },
          });

          return {
            url: publicUrl,
            title: file.name,
            alt: file.name,
          };
        })
      );
      return uploads;
    } catch (err: any) {
      console.error("Upload failed", err);
      setError(`图片上传失败: ${err.message}`);
      return [];
    }
  };

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

      const res = await apiFetch(`/api/v1/posts/${postId}`, {
        method: "PUT",
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

      const json = (await res.json()) as ApiResponse<{ slug: string }>;
      if (json?.success && json.data) {
        const slug = json.data.slug;
        setSuccessMsg("文章已发布");
        setTimeout(() => {
          window.location.href = `/posts/${slug}`;
        }, 1500);
      } else {
        setError(json?.error?.message ?? "更新文章失败");
      }
    } catch {
      setError("更新文章失败");
    } finally {
      setLoading(false);
    }
  }

  async function saveDraft() {
    if (!title.trim() || !content.trim()) {
      setError("标题和内容不能为空");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/posts/${postId}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          title: title.trim(),
          summary: summary.trim(),
          contentMarkdown: content.trim(),
          tagsCsv: tagsCsv.trim(),
          category: category.trim(),
          status: "DRAFT",
          format: "MARKDOWN",
        }),
      });

      const json = (await res.json()) as ApiResponse<null>;
      if (json?.success) {
        setSuccessMsg("草稿已保存");
        setTimeout(() => setSuccessMsg(""), 3000);
      } else if (json?.error) {
        setError(json.error.message ?? "保存草稿失败");
      } else {
        setError("保存草稿失败");
      }
    } catch {
      setError("保存草稿失败");
    } finally {
      setLoading(false);
    }
  }

  if (pageLoading) {
    return (
      <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-neutral-50/50 px-4 py-12 sm:px-6 lg:px-8">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-neutral-200 border-t-neutral-900"></div>
      </div>
    );
  }

  if (error && !title) {
    return (
      <main className="min-h-[calc(100vh-4rem)] bg-neutral-50/50 px-4 py-12 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-4xl">
          <div className="mb-8">
            <h1 className="text-3xl font-bold tracking-tight text-neutral-900">编辑文章</h1>
          </div>
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6">
            <p className="text-rose-700">{error}</p>
            <Link href="/profile" className="mt-4 inline-block rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white hover:bg-neutral-800">
              返回我的文章
            </Link>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-neutral-50/50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-4xl">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-neutral-900">
              编辑文章
            </h1>
            <p className="mt-2 text-neutral-600">
              修改你的文章内容。支持 Markdown 格式。
            </p>
          </div>
          <Link
            href="/profile"
            className="inline-flex items-center rounded-lg border border-neutral-300 bg-white px-4 py-2 text-sm font-medium text-neutral-700 hover:bg-neutral-50"
          >
            <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
            返回我的文章
          </Link>
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
                <label htmlFor="content" className="block text-sm font-medium text-neutral-900 mb-2">
                  正文内容 <span className="text-rose-500">*</span>
                </label>
                <div className="rounded-lg border border-neutral-200 bg-white overflow-hidden shadow-sm">
                  <ByteMDEditor
                    value={content}
                    onChange={setContent}
                    uploadImages={uploadImages}
                    placeholder="# 开始你的创作..."
                    className="min-h-[600px]"
                  />
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
              <div className="flex flex-col gap-1">
                <div className="text-sm text-rose-600 font-medium">
                  {error && error}
                </div>
                <div className="text-sm text-emerald-600 font-medium">
                  {successMsg && successMsg}
                </div>
              </div>
              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={saveDraft}
                  disabled={loading || !title.trim() || !content.trim()}
                  className="inline-flex items-center justify-center rounded-lg bg-white border border-neutral-300 px-6 py-2.5 text-sm font-medium text-neutral-700 shadow-sm hover:bg-neutral-50 focus:outline-none focus:ring-2 focus:ring-neutral-900 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                  {loading ? (
                    <>
                      <svg className="mr-2 h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                      </svg>
                      保存中...
                    </>
                  ) : (
                    "保存草稿"
                  )}
                </button>
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
                      更新中...
                    </>
                  ) : (
                    "更新发布"
                  )}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </main>
  );
}

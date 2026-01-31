"use client";

import React, { useCallback, useEffect, useState } from "react";
import { appConfig } from "../../config/appConfig";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type AuthorSummary = {
  id: number;
  nickname: string | null;
  avatarUrl: string | null;
  bio: string | null;
  createdAt: string | null;
};

function apiUrl(path: string) {
  const baseRaw = appConfig.apiBase;
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default function AuthorsPage() {
  const pageSize = 10;

  const [page, setPage] = useState(0);
  const [authors, setAuthors] = useState<AuthorSummary[] | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPage = useCallback(async (nextPage: number, replace: boolean) => {
    setLoading(true);
    setError(null);
    try {
      const offset = nextPage * pageSize;
      const res = await fetch(apiUrl(`/api/v1/authors?limit=${pageSize}&offset=${offset}`), {
        cache: "no-store",
      });
      const json = (await res.json()) as ApiResponse<AuthorSummary[]>;
      if (!json?.success || !json.data) {
        setError(json?.error?.message ?? "加载作者失败");
        return;
      }

      setAuthors((prev) => {
        if (replace) return json.data ?? [];
        const base = prev ?? [];
        return base.concat(json.data ?? []);
      });
      setPage(nextPage);
      setHasMore((json.data ?? []).length >= pageSize);
    } catch {
      setError("暂时无法加载作者信息");
    } finally {
      setLoading(false);
    }
  }, [pageSize]);

  useEffect(() => {
    void loadPage(0, true);
  }, [loadPage]);

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight text-neutral-900">
            作者列表
          </h1>
          <p className="mt-2 text-sm text-neutral-600">
            查看所有已注册作者
          </p>
        </div>

        {error && (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-900">
            {error}
          </div>
        )}

        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-4 border-zinc-200"></div>
          </div>
        )}

        {!loading && !error && authors && authors.length === 0 && (
          <div className="rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">
            还没有作者。
          </div>
        )}

        {!loading && !error && authors && authors.length > 0 && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {authors.map((author) => (
              <a
                key={author.id}
                href={`/authors/${author.id}`}
                className="block rounded-2xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur transition hover:-translate-y-0.5 hover:shadow-[0_16px_56px_rgba(0,0,0,0.08)]"
              >
                <div className="flex items-start gap-4">
                  {author.avatarUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element -- 远程头像来源不固定，暂不使用 next/image
                    <img
                      src={author.avatarUrl}
                      alt={author.nickname || `Author #${author.id}`}
                      className="h-16 w-16 rounded-full object-cover"
                    />
                  ) : (
                    <div className="h-16 w-16 rounded-full bg-gradient-to-br from-amber-200 to-rose-200 flex items-center justify-center text-2xl font-bold text-white">
                      {author.nickname?.charAt(0) || "A"}
                    </div>
                  )}
                  <div className="min-w-0">
                    <div className="text-lg font-semibold text-neutral-900">
                      {author.nickname || `未命名作者 #${author.id}`}
                    </div>
                    <div className="text-sm text-neutral-600">
                      {author.bio?.trim() ? author.bio : "这个作者还没有填写简介。"}
                    </div>
                  </div>
                </div>
              </a>
            ))}
          </div>
        )}

        {!loading && !error && authors && authors.length > 0 && (
          <div className="mt-8 flex justify-center">
            <button
              type="button"
              onClick={() => void loadPage(page + 1, false)}
              disabled={loading || !hasMore}
              className="rounded-xl border border-black/10 bg-zinc-900 px-5 py-3 text-sm font-medium text-white hover:bg-zinc-800 disabled:opacity-50"
            >
              {loading ? "加载中..." : "加载更多"}
            </button>
          </div>
        )}
      </div>
    </main>
  );
}

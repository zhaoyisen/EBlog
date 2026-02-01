"use client";

import Link from "next/link";
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
  const pageSize = 12;

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
    <main className="min-h-screen bg-zinc-50/50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-7xl">
        <div className="mb-12 text-center">
          <h1 className="text-4xl font-bold tracking-tight text-zinc-900 sm:text-5xl">
            探索作者
          </h1>
          <p className="mt-4 text-lg text-zinc-600">
            发现优秀的创作者和他们独特的观点
          </p>
        </div>

        {error && (
          <div className="mx-auto max-w-2xl rounded-lg bg-rose-50 p-4 text-center text-sm text-rose-600 border border-rose-100 mb-8">
            {error}
          </div>
        )}

        {!loading && !error && authors && authors.length === 0 && (
          <div className="flex flex-col items-center justify-center py-20 text-zinc-500">
            <svg className="h-16 w-16 text-zinc-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <p className="mt-4 text-lg">还没有作者加入</p>
          </div>
        )}

        {authors && authors.length > 0 && (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {authors.map((author) => (
              <Link
                key={author.id}
                href={`/authors/${author.id}`}
                className="group relative flex flex-col overflow-hidden rounded-2xl bg-white shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl border border-zinc-100"
              >
                <div className="aspect-[3/2] w-full bg-zinc-100 relative overflow-hidden">
                  {author.avatarUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      src={author.avatarUrl}
                      alt={author.nickname || ""}
                      className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                    />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-zinc-200 to-zinc-300 text-4xl font-bold text-zinc-400">
                      {author.nickname?.charAt(0)?.toUpperCase() || "A"}
                    </div>
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
                </div>
                
                <div className="flex flex-1 flex-col p-6">
                  <h3 className="text-xl font-bold text-zinc-900 group-hover:text-indigo-600 transition-colors">
                    {author.nickname || `Author #${author.id}`}
                  </h3>
                  <p className="mt-2 flex-1 text-sm text-zinc-500 line-clamp-3 leading-relaxed">
                    {author.bio?.trim() ? author.bio : "这位作者很神秘，还没有写下简介。"}
                  </p>
                  <div className="mt-4 flex items-center text-xs font-medium text-zinc-400">
                    <span>加入于 {new Date(author.createdAt || "").toLocaleDateString()}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}

        {loading && (
          <div className="flex items-center justify-center py-12">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900"></div>
          </div>
        )}

        {!loading && !error && hasMore && authors && authors.length > 0 && (
          <div className="mt-12 flex justify-center">
            <button
              type="button"
              onClick={() => void loadPage(page + 1, false)}
              className="rounded-full border border-zinc-200 bg-white px-8 py-3 text-sm font-medium text-zinc-900 shadow-sm transition hover:bg-zinc-50 hover:shadow-md active:scale-95"
            >
              加载更多作者
            </button>
          </div>
        )}
      </div>
    </main>
  );
}

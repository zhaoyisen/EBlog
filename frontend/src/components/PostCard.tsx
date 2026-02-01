import React from 'react';

type PostSummary = {
  id: number;
  authorId: number;
  authorName?: string | null;
  authorAvatar?: string | null;
  title: string;
  slug: string;
  summary: string | null;
  tagsCsv: string | null;
  category: string | null;
  createdAt: string | null;
};

interface PostCardProps {
  post: PostSummary;
  isLoading?: boolean;
}

export default function PostCard({ post, isLoading }: PostCardProps) {
  if (isLoading) {
    return (
      <div className="flex flex-col overflow-hidden rounded-2xl border border-neutral-200 bg-white shadow-sm">
        <div className="flex flex-1 flex-col p-6 space-y-4">
          <div className="flex items-center justify-between gap-2">
            <div className="h-6 w-20 rounded-full bg-neutral-200 animate-pulse" />
            <div className="h-4 w-24 rounded bg-neutral-200 animate-pulse" />
          </div>
          <div className="h-8 w-3/4 rounded bg-neutral-200 animate-pulse" />
          <div className="space-y-2">
            <div className="h-4 w-full rounded bg-neutral-200 animate-pulse" />
            <div className="h-4 w-5/6 rounded bg-neutral-200 animate-pulse" />
            <div className="h-4 w-4/6 rounded bg-neutral-200 animate-pulse" />
          </div>
          <div className="flex items-center justify-between pt-4 border-t border-neutral-100">
            <div className="flex items-center gap-2">
              <div className="h-6 w-6 rounded-full bg-neutral-200 animate-pulse" />
              <div className="h-4 w-16 rounded bg-neutral-200 animate-pulse" />
            </div>
            <div className="flex gap-1">
              <div className="h-6 w-12 rounded bg-neutral-200 animate-pulse" />
              <div className="h-6 w-12 rounded bg-neutral-200 animate-pulse" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  const tags = post.tagsCsv ? post.tagsCsv.split(",").filter(Boolean) : [];
  const authorName = post.authorName || `#${post.authorId}`;

  return (
    <a
      href={`/posts/${encodeURIComponent(post.slug)}`}
      className="group flex flex-col overflow-hidden rounded-2xl border border-neutral-200 bg-white shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:border-neutral-300"
    >
      <div className="flex flex-1 flex-col p-6">
        <div className="flex items-center justify-between gap-2">
          <span className="inline-flex items-center rounded-full bg-gradient-to-r from-neutral-900 to-neutral-700 px-3 py-1 text-xs font-medium text-white shadow-sm">
            {post.category || "未分类"}
          </span>
          <span className="text-xs text-neutral-500 font-medium">
            {post.createdAt ? new Date(post.createdAt).toLocaleDateString("zh-CN", {
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            }) : ""}
          </span>
        </div>

        <h3 className="mt-4 text-xl font-bold tracking-tight text-neutral-900 group-hover:text-neutral-700 transition-colors line-clamp-2">
          {post.title}
        </h3>

        <p className="mt-3 flex-1 text-sm leading-relaxed text-neutral-600 line-clamp-3">
          {post.summary?.trim() ? post.summary : "暂无摘要"}
        </p>

        <div className="mt-6 flex items-center justify-between pt-4 border-t border-neutral-100">
          <div className="flex items-center gap-2">
            {post.authorAvatar ? (
              <img
                src={post.authorAvatar}
                alt={authorName}
                className="h-6 w-6 rounded-full object-cover ring-1 ring-neutral-200"
              />
            ) : (
              <div className="h-6 w-6 rounded-full bg-gradient-to-br from-neutral-300 to-neutral-400 flex items-center justify-center text-[10px] text-neutral-700 font-medium">
                {authorName.charAt(0).toUpperCase()}
              </div>
            )}
            <span className="text-xs text-neutral-500 font-medium truncate max-w-[100px]">{authorName}</span>
          </div>

          {tags.length > 0 && (
            <div className="flex gap-1">
              {tags.slice(0, 2).map((tag) => (
                <span
                  key={tag}
                  className="inline-flex items-center rounded-md bg-neutral-50 px-2.5 py-1 text-xs font-medium text-neutral-600 ring-1 ring-inset ring-neutral-500/10 group-hover:bg-neutral-100 transition-colors"
                >
                  {tag}
                </span>
              ))}
              {tags.length > 2 && (
                <span className="inline-flex items-center rounded-md bg-neutral-50 px-2.5 py-1 text-xs font-medium text-neutral-600 ring-1 ring-inset ring-neutral-500/10 group-hover:bg-neutral-100 transition-colors">
                  +{tags.length - 2}
                </span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* 装饰性渐变边框 */}
      <div className="h-1 bg-gradient-to-r from-neutral-900 via-neutral-600 to-neutral-900 opacity-0 group-hover:opacity-100 transition-opacity" />
    </a>
  );
}

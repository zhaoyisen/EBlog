import React from 'react';
import { Eye, Heart, MessageSquare, Calendar } from 'lucide-react';
import { Badge } from './Badge';
import { UserBadge } from './UserBadge';

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
  views?: number;
  likes?: number;
  comments?: number;
};

interface PostCardProps {
  post: PostSummary;
  isLoading?: boolean;
  compact?: boolean;
}

export default function PostCard({ post, isLoading, compact }: PostCardProps) {
  if (isLoading) {
    if (compact) {
      return (
        <div className="flex items-center justify-between p-4 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900 shadow-sm">
          <div className="h-5 w-1/3 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
          <div className="flex gap-2">
            <div className="h-5 w-16 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
            <div className="h-5 w-12 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
          </div>
        </div>
      );
    }
    return (
      <div className="flex flex-col overflow-hidden rounded-2xl border border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900 shadow-sm">
        <div className="flex flex-1 flex-col p-6 space-y-4">
          <div className="flex items-center justify-between gap-2">
            <div className="h-6 w-20 rounded-full bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
            <div className="h-4 w-24 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
          </div>
          <div className="h-8 w-3/4 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
          <div className="space-y-2">
            <div className="h-4 w-full rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
            <div className="h-4 w-5/6 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
            <div className="h-4 w-4/6 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
          </div>
          <div className="flex items-center justify-between pt-4 border-t border-neutral-100 dark:border-neutral-800">
            <div className="flex items-center gap-2">
              <div className="h-6 w-6 rounded-full bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
              <div className="h-4 w-16 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
            </div>
            <div className="flex gap-1">
              <div className="h-6 w-12 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
              <div className="h-6 w-12 rounded bg-neutral-200 dark:bg-neutral-800 animate-pulse" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  const tags = post.tagsCsv ? post.tagsCsv.split(",").filter(Boolean) : [];
  const authorName = post.authorName || `#${post.authorId}`;
  
  // Format numbers for display (e.g. 1.2k) - simple version for now
  const formatNumber = (num?: number) => {
    if (!num) return '0';
    if (num >= 1000) return `${(num / 1000).toFixed(1)}k`;
    return num.toString();
  };

  const Stats = () => (
    <div className="flex items-center gap-3 text-neutral-500 dark:text-neutral-400">
      <div className="flex items-center gap-1" title="Views">
        <Eye className="w-3.5 h-3.5" />
        <span className="text-xs font-medium">{formatNumber(post.views)}</span>
      </div>
      <div className="flex items-center gap-1" title="Likes">
        <Heart className="w-3.5 h-3.5" />
        <span className="text-xs font-medium">{formatNumber(post.likes)}</span>
      </div>
      <div className="flex items-center gap-1" title="Comments">
        <MessageSquare className="w-3.5 h-3.5" />
        <span className="text-xs font-medium">{formatNumber(post.comments)}</span>
      </div>
    </div>
  );

  if (compact) {
    return (
      <a
        href={`/posts/${encodeURIComponent(post.slug)}`}
        className="group flex flex-col sm:flex-row sm:items-center justify-between gap-3 p-4 rounded-lg border border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900 shadow-sm transition-all duration-300 hover:border-neutral-300 dark:hover:border-neutral-700 hover:bg-neutral-50 dark:hover:bg-neutral-800/50"
      >
        <div className="flex flex-col gap-1 min-w-0">
          <div className="flex items-center gap-2">
            {post.category && (
              <Badge variant="outline" className="shrink-0 px-1.5 py-0 text-[10px] uppercase tracking-wider">
                {post.category}
              </Badge>
            )}
            <h3 className="font-semibold text-neutral-900 dark:text-neutral-100 truncate group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
              {post.title}
            </h3>
          </div>
          <div className="flex items-center gap-3 text-xs text-neutral-500 dark:text-neutral-500">
            <span className="flex items-center gap-1">
              <Calendar className="w-3 h-3" />
              {post.createdAt ? new Date(post.createdAt).toLocaleDateString("zh-CN") : ""}
            </span>
            <span className="hidden sm:inline-block text-neutral-300 dark:text-neutral-700">|</span>
            <UserBadge name={authorName} avatar={post.authorAvatar} size="sm" className="hidden sm:flex" />
          </div>
        </div>

        <div className="flex items-center justify-between sm:justify-end gap-4 shrink-0 mt-2 sm:mt-0 pt-2 sm:pt-0 border-t sm:border-t-0 border-neutral-100 dark:border-neutral-800">
          <div className="flex sm:hidden">
             <UserBadge name={authorName} avatar={post.authorAvatar} size="sm" />
          </div>
          <Stats />
        </div>
      </a>
    );
  }

  return (
    <a
      href={`/posts/${encodeURIComponent(post.slug)}`}
      className="group flex flex-col overflow-hidden rounded-2xl border border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:border-neutral-300 dark:hover:border-neutral-700"
    >
      <div className="flex flex-1 flex-col p-6">
        <div className="flex items-center justify-between gap-2">
          <Badge variant="default" className="shadow-sm">
            {post.category || "未分类"}
          </Badge>
          <span className="text-xs text-neutral-500 dark:text-neutral-400 font-medium">
            {post.createdAt ? new Date(post.createdAt).toLocaleDateString("zh-CN", {
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            }) : ""}
          </span>
        </div>

        <h3 className="mt-4 text-xl font-bold tracking-tight text-neutral-900 dark:text-neutral-100 group-hover:text-neutral-700 dark:group-hover:text-neutral-300 transition-colors line-clamp-2">
          {post.title}
        </h3>

        <p className="mt-3 flex-1 text-sm leading-relaxed text-neutral-600 dark:text-neutral-400 line-clamp-3">
          {post.summary?.trim() ? post.summary : "暂无摘要"}
        </p>

        <div className="mt-6 flex flex-col gap-4 pt-4 border-t border-neutral-100 dark:border-neutral-800">
          <div className="flex items-center justify-between">
            <UserBadge name={authorName} avatar={post.authorAvatar} />
            <Stats />
          </div>
          
          {tags.length > 0 && (
            <div className="flex flex-wrap gap-1.5">
              {tags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-[10px] px-2 py-0.5">
                  {tag}
                </Badge>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* 装饰性渐变边框 */}
      <div className="h-1 bg-gradient-to-r from-neutral-900 via-neutral-600 to-neutral-900 dark:from-neutral-100 dark:via-neutral-400 dark:to-neutral-100 opacity-0 group-hover:opacity-100 transition-opacity" />
    </a>
  );
}

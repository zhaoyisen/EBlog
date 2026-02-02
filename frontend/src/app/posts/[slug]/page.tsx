import Link from "next/link";
import { appConfig } from "../../../config/appConfig";
import PostInteractions from "./_components/PostInteractions";
import MarkdownRenderer from "../../../components/MarkdownRenderer";
import { FollowButton } from "../../../components/FollowButton";
import { Eye, Calendar, Tag, Hash } from "lucide-react";


type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type PostDetail = {
  id: number;
  authorId: number;
  authorName?: string | null;
  authorAvatar?: string | null;
  title: string;
  slug: string;
  summary: string | null;
  contentMarkdown: string;
  contentHtml?: string | null;
  format?: string | null;
  tagsCsv: string | null;
  category: string | null;
  createdAt: string | null;
  updatedAt: string | null;
  viewCount?: number;
};

function apiUrl(path: string) {
  const baseRaw = appConfig.internalApiBase;
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  return `${base}${path}`;
}

export default async function PostPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;

  let post: PostDetail | null = null;
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl(`/api/v1/posts/${encodeURIComponent(slug)}`), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<PostDetail>;
    if (json?.success && json.data) {
      post = json.data;
    } else {
      error = json?.error?.message ?? "文章不存在";
    }
  } catch {
    error = "暂时无法加载文章";
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] bg-white pb-20">
      {error ? (
        <div className="mx-auto max-w-3xl px-5 py-20">
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-center text-rose-900">
            <p className="font-medium">{error}</p>
            <a href="/posts" className="mt-4 inline-block text-sm text-rose-700 underline hover:text-rose-800">
              返回文章列表
            </a>
          </div>
        </div>
      ) : post ? (
        <article>
          {/* Header Section */}
          <header className="bg-neutral-50/50 border-b border-neutral-100 px-5 py-16 sm:py-20">
            <div className="mx-auto max-w-3xl text-center">
              <div className="flex flex-wrap items-center justify-center gap-3 mb-8">
                {post.category && (
                  <Link
                    href={`/categories/${encodeURIComponent(post.category)}`}
                    className="inline-flex items-center rounded-full bg-neutral-900 px-3 py-1 text-xs font-medium text-white shadow-sm hover:bg-neutral-800 transition-colors"
                  >
                    {post.category}
                  </Link>
                )}
                <div className="flex items-center gap-3 text-sm text-neutral-500">
                  <span className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5" />
                    {post.createdAt ? new Date(post.createdAt).toLocaleDateString() : ""}
                  </span>
                  <span className="flex items-center gap-1.5">
                    <Eye className="w-3.5 h-3.5" />
                    {post.viewCount || 0} 阅读
                  </span>
                </div>
              </div>
              
              <h1 className="text-4xl font-bold tracking-tight text-neutral-900 sm:text-5xl leading-tight">
                {post.title}
              </h1>
              
              {post.summary && (
                <p className="mt-6 text-lg text-neutral-600 leading-relaxed max-w-2xl mx-auto">
                  {post.summary}
                </p>
              )}

              <div className="mt-8 flex items-center justify-center gap-4 animate-in fade-in slide-in-from-bottom-2 duration-500">
                <div className="flex items-center gap-2">
                  {post.authorAvatar ? (
                    <img
                      src={post.authorAvatar}
                      alt={post.authorName || `作者 #${post.authorId}`}
                      className="h-10 w-10 rounded-full object-cover ring-2 ring-white shadow-sm"
                    />
                  ) : (
                    <div className="h-10 w-10 rounded-full bg-gradient-to-br from-neutral-200 to-neutral-300 flex items-center justify-center text-sm text-neutral-600 font-medium ring-2 ring-white shadow-sm">
                      {(post.authorName || `#${post.authorId}`).charAt(0).toUpperCase()}
                    </div>
                  )}
                  <div className="text-left">
                    <div className="text-sm font-semibold text-neutral-900">{post.authorName || `作者 #${post.authorId}`}</div>
                    <div className="text-xs text-neutral-500">发布者</div>
                  </div>
                </div>
                
                <div className="h-8 w-px bg-neutral-200 mx-2"></div>
                
                <FollowButton targetUserId={post.authorId} />
              </div>
            </div>
          </header>

          {/* Content Section */}
          <div className="mx-auto max-w-3xl px-5 py-12">
            <MarkdownRenderer content={post.contentMarkdown} />

            {/* Tags */}
            {post.tagsCsv && (
              <div className="mt-16 pt-8 border-t border-neutral-100">
                <div className="flex flex-wrap gap-2">
                  {post.tagsCsv.split(",").filter(Boolean).map((tag) => (
                    <Link
                      key={tag}
                      href={`/tags/${encodeURIComponent(tag.trim())}`}
                      className="inline-flex items-center gap-1.5 rounded-full bg-neutral-50 px-3 py-1.5 text-sm font-medium text-neutral-600 hover:bg-neutral-100 hover:text-neutral-900 transition-colors"
                    >
                      <Hash className="w-3.5 h-3.5" />
                      {tag.trim()}
                    </Link>
                  ))}
                </div>
              </div>
            )}

            {/* Interactions */}
            <div className="mt-12">
              <PostInteractions postId={String(post.id)} />
            </div>
          </div>
        </article>
      ) : null}
    </main>
  );
}

import { appConfig } from "../../../config/appConfig";
import PostInteractions from "./_components/PostInteractions";
import MarkdownRenderer from "../../../components/MarkdownRenderer";


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
          <header className="bg-neutral-50/50 border-b border-neutral-100 px-5 py-16 sm:py-24">
            <div className="mx-auto max-w-3xl text-center">
              <div className="flex items-center justify-center gap-3 mb-6">
                {post.category && (
                  <span className="inline-flex items-center rounded-full bg-neutral-900 px-3 py-1 text-xs font-medium text-white">
                    {post.category}
                  </span>
                )}
                <span className="text-sm text-neutral-500">
                  {post.createdAt ? new Date(post.createdAt).toLocaleDateString() : ""}
                </span>
              </div>
              
              <h1 className="text-4xl font-bold tracking-tight text-neutral-900 sm:text-5xl leading-tight">
                {post.title}
              </h1>
              
              {post.summary && (
                <p className="mt-6 text-lg text-neutral-600 leading-relaxed">
                  {post.summary}
                </p>
              )}

              <div className="mt-8 flex items-center justify-center gap-2">
                {post.authorAvatar ? (
                  <img
                    src={post.authorAvatar}
                    alt={post.authorName || `作者 #${post.authorId}`}
                    className="h-8 w-8 rounded-full object-cover ring-1 ring-neutral-200"
                  />
                ) : (
                  <div className="h-8 w-8 rounded-full bg-neutral-200 flex items-center justify-center text-xs text-neutral-600 font-medium">
                    {(post.authorName || `#${post.authorId}`).charAt(0).toUpperCase()}
                  </div>
                )}
                <span className="text-sm font-medium text-neutral-900">{post.authorName || `作者 #${post.authorId}`}</span>
              </div>
            </div>
          </header>

          {/* Content Section */}
          <div className="mx-auto max-w-3xl px-5 py-12">
            <MarkdownRenderer content={post.contentMarkdown} />

            {/* Tags */}
            {post.tagsCsv && (
              <div className="mt-12 pt-8 border-t border-neutral-100">
                <div className="flex flex-wrap gap-2">
                  {post.tagsCsv.split(",").filter(Boolean).map((tag) => (
                    <span key={tag} className="inline-flex items-center rounded-md bg-neutral-100 px-2.5 py-1 text-sm font-medium text-neutral-700">
                      #{tag}
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Interactions */}
            <div className="mt-12">
              <PostInteractions postId={post.id} />
            </div>
          </div>
        </article>
      ) : null}
    </main>
  );
}

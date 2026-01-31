import { renderMdxCached } from "../../../lib/mdx/renderMdx";
import { appConfig } from "../../../config/appConfig";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type PostDetail = {
  id: number;
  authorId: number;
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
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        {error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : post ? (
          <article className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
            <div className="text-xs text-neutral-500">作者 #{post.authorId}</div>
            <h1 className="mt-1 text-3xl font-semibold tracking-tight text-neutral-900">{post.title}</h1>
            <p className="mt-3 text-sm text-neutral-700">{post.summary?.trim() ? post.summary : ""}</p>
            <div className="mt-6 rounded-2xl border border-black/10 bg-white p-4">
              <div className="text-xs text-neutral-500">正文</div>
              {String(post.format).toUpperCase() === "MDX" ? (
                <div className="mt-3">
                  {await (async () => {
                    try {
                      const key = `${post.id}:${post.updatedAt ?? ""}`;
                      const node = await renderMdxCached(key, post.contentMarkdown);
                      return <div className="prose max-w-none">{node}</div>;
                    } catch {
                      return (
                        <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
                          这篇文章的 MDX 渲染失败，已安全降级。
                        </div>
                      );
                    }
                  })()}
                </div>
              ) : post.contentHtml?.trim() ? (
                <div className="prose mt-3 max-w-none" dangerouslySetInnerHTML={{ __html: post.contentHtml }} />
              ) : (
                <pre className="mt-2 whitespace-pre-wrap text-sm text-neutral-900">{post.contentMarkdown}</pre>
              )}
            </div>
          </article>
        ) : null}
      </div>
    </main>
  );
}

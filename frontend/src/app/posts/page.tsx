type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type PostSummary = {
  id: number;
  authorId: number;
  title: string;
  slug: string;
  summary: string | null;
  tagsCsv: string | null;
  category: string | null;
  createdAt: string | null;
};

function apiUrl(path: string) {
  const baseRaw = process.env.NEXT_PUBLIC_API_BASE ?? "";
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default async function PostsPage() {
  let posts: PostSummary[] = [];
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl("/api/v1/posts?limit=20&offset=0"), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<PostSummary[]>;
    if (json?.success && Array.isArray(json.data)) {
      posts = json.data;
    } else {
      error = json?.error?.message ?? "暂时无法加载文章";
    }
  } catch {
    error = "暂时无法加载文章";
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">文章</h1>
          <p className="mt-1 text-sm text-neutral-600">公开可见的文章列表</p>
        </div>

        {error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : posts.length === 0 ? (
          <div className="rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">
            还没有文章。
          </div>
        ) : (
          <div className="grid gap-4">
            {posts.map((p) => (
              <a
                key={p.id}
                href={`/posts/${encodeURIComponent(p.slug)}`}
                className="block rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur transition hover:-translate-y-0.5 hover:shadow-[0_16px_56px_rgba(0,0,0,0.10)]"
              >
                <div className="text-xs text-neutral-500">作者 #{p.authorId}</div>
                <div className="mt-1 text-xl font-semibold tracking-tight text-neutral-900">{p.title}</div>
                <div className="mt-2 text-sm text-neutral-700">{p.summary?.trim() ? p.summary : "暂无摘要"}</div>
              </a>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

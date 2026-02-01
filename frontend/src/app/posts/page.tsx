import { appConfig } from "../../config/appConfig";

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
  const baseRaw = appConfig.internalApiBase;
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  return `${base}${path}`;
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
    <main className="min-h-[calc(100vh-4rem)] px-4 py-12 sm:px-6 lg:px-8 bg-neutral-50/50">
      <div className="mx-auto max-w-7xl">
        <div className="mb-12 text-center">
          <h1 className="text-4xl font-bold tracking-tight text-neutral-900 sm:text-5xl">
            探索文章
          </h1>
          <p className="mt-4 text-lg text-neutral-600 max-w-2xl mx-auto">
            发现最新的技术见解、教程和思考。
          </p>
        </div>

        {error ? (
          <div className="mx-auto max-w-md rounded-2xl border border-rose-200 bg-rose-50 p-6 text-center text-rose-900">
            <p>{error}</p>
          </div>
        ) : posts.length === 0 ? (
          <div className="mx-auto max-w-md rounded-2xl border border-neutral-200 bg-white p-12 text-center text-neutral-500">
            <p className="text-lg">还没有文章。</p>
          </div>
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {posts.map((p) => {
              const tags = p.tagsCsv ? p.tagsCsv.split(",").filter(Boolean) : [];
              return (
                <a
                  key={p.id}
                  href={`/posts/${encodeURIComponent(p.slug)}`}
                  className="group flex flex-col overflow-hidden rounded-2xl border border-neutral-200 bg-white shadow-sm transition-all hover:-translate-y-1 hover:shadow-md"
                >
                  <div className="flex flex-1 flex-col p-6">
                    <div className="flex items-center justify-between gap-2">
                      <span className="inline-flex items-center rounded-full bg-neutral-100 px-2.5 py-0.5 text-xs font-medium text-neutral-800">
                        {p.category || "未分类"}
                      </span>
                      <span className="text-xs text-neutral-500">
                        {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : ""}
                      </span>
                    </div>
                    
                    <h3 className="mt-4 text-xl font-bold tracking-tight text-neutral-900 group-hover:text-neutral-700">
                      {p.title}
                    </h3>
                    
                    <p className="mt-3 flex-1 text-sm leading-relaxed text-neutral-600 line-clamp-3">
                      {p.summary?.trim() ? p.summary : "暂无摘要"}
                    </p>

                    <div className="mt-6 flex items-center justify-between pt-4 border-t border-neutral-100">
                      <div className="flex items-center gap-2">
                        <div className="h-6 w-6 rounded-full bg-neutral-200 flex items-center justify-center text-[10px] text-neutral-600 font-medium">
                          {p.authorId}
                        </div>
                        <span className="text-xs text-neutral-500">作者 #{p.authorId}</span>
                      </div>
                      
                      {tags.length > 0 && (
                        <div className="flex gap-1">
                          {tags.slice(0, 2).map((tag) => (
                            <span key={tag} className="inline-flex items-center rounded-md bg-neutral-50 px-2 py-1 text-xs font-medium text-neutral-600 ring-1 ring-inset ring-neutral-500/10">
                              {tag}
                            </span>
                          ))}
                          {tags.length > 2 && (
                            <span className="inline-flex items-center rounded-md bg-neutral-50 px-2 py-1 text-xs font-medium text-neutral-600 ring-1 ring-inset ring-neutral-500/10">
                              +{tags.length - 2}
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </a>
              );
            })}
          </div>
        )}
      </div>
    </main>
  );
}

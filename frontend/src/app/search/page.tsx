type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type SearchItem = {
  id: number;
  authorId: number;
  title: string;
  slug: string;
  summary: string | null;
  tags: string[];
  category: string | null;
  createdAt: string | null;
};

function apiUrl(path: string) {
  const baseRaw = process.env.NEXT_PUBLIC_API_BASE ?? "";
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default async function SearchPage({
  searchParams,
}: {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}) {
  const sp = await searchParams;
  const q = typeof sp.q === "string" ? sp.q : "";

  let results: SearchItem[] = [];
  let error: string | null = null;

  if (q.trim()) {
    try {
      const qs = new URLSearchParams({ q });
      const res = await fetch(apiUrl(`/api/v1/search?${qs.toString()}`), { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<SearchItem[]>;
      if (json?.success && Array.isArray(json.data)) {
        results = json.data;
      } else {
        error = json?.error?.message ?? "搜索失败";
      }
    } catch {
      error = "暂时无法搜索";
    }
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">搜索</h1>
          <p className="mt-1 text-sm text-neutral-600">支持标题/摘要/标签（MVP）</p>
        </div>

        <form action="/search" className="flex items-center gap-2 rounded-3xl border border-black/10 bg-white/70 p-2 shadow-sm backdrop-blur">
          <input
            name="q"
            defaultValue={q}
            placeholder="输入关键词，比如：java / spring / atlas"
            className="w-full rounded-2xl bg-transparent px-4 py-3 text-sm text-neutral-900 outline-none placeholder:text-neutral-500"
          />
          <button
            type="submit"
            className="shrink-0 rounded-2xl bg-black px-5 py-3 text-sm font-medium text-white"
          >
            搜索
          </button>
        </form>

        {!q.trim() ? (
          <div className="mt-6 rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">
            输入关键词开始搜索。
          </div>
        ) : error ? (
          <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : results.length === 0 ? (
          <div className="mt-6 rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">
            没有找到结果。
          </div>
        ) : (
          <div className="mt-6 grid gap-4">
            {results.map((p) => (
              <a
                key={p.id}
                href={`/posts/${encodeURIComponent(p.slug)}`}
                className="block rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur transition hover:-translate-y-0.5 hover:shadow-[0_16px_56px_rgba(0,0,0,0.10)]"
              >
                <div className="text-xs text-neutral-500">作者 #{p.authorId}</div>
                <div className="mt-1 text-xl font-semibold tracking-tight text-neutral-900">{p.title}</div>
                <div className="mt-2 text-sm text-neutral-700">{p.summary?.trim() ? p.summary : "暂无摘要"}</div>
                {p.tags?.length ? (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {p.tags.slice(0, 6).map((t) => (
                      <span key={t} className="rounded-full bg-black/5 px-2 py-0.5 text-xs text-neutral-700">
                        {t}
                      </span>
                    ))}
                  </div>
                ) : null}
              </a>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

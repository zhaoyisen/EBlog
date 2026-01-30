type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type TagCount = { tag: string; count: number };

function apiUrl(path: string) {
  const baseRaw = process.env.NEXT_PUBLIC_API_BASE ?? "";
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default async function TagsPage() {
  let tags: TagCount[] = [];
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl("/api/v1/tags"), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<TagCount[]>;
    if (json?.success && Array.isArray(json.data)) {
      tags = json.data;
    } else {
      error = json?.error?.message ?? "暂时无法加载标签";
    }
  } catch {
    error = "暂时无法加载标签";
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">标签</h1>
          <p className="mt-1 text-sm text-neutral-600">标签默认小写规范化（MVP）</p>
        </div>

        {error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : tags.length === 0 ? (
          <div className="rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">还没有标签。</div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {tags.map((t) => (
              <a
                key={t.tag}
                href={`/tags/${encodeURIComponent(t.tag)}`}
                className="rounded-full border border-black/10 bg-white/70 px-3 py-1 text-sm text-neutral-800 shadow-sm hover:bg-white"
              >
                {t.tag}
                <span className="ml-2 text-xs text-neutral-500">{t.count}</span>
              </a>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

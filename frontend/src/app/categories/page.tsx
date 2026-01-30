type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type CategoryCount = { category: string; count: number };

function apiUrl(path: string) {
  const baseRaw = process.env.NEXT_PUBLIC_API_BASE ?? "";
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default async function CategoriesPage() {
  let categories: CategoryCount[] = [];
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl("/api/v1/categories"), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<CategoryCount[]>;
    if (json?.success && Array.isArray(json.data)) {
      categories = json.data;
    } else {
      error = json?.error?.message ?? "暂时无法加载分类";
    }
  } catch {
    error = "暂时无法加载分类";
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">分类</h1>
          <p className="mt-1 text-sm text-neutral-600">分类为自由文本（MVP）</p>
        </div>

        {error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : categories.length === 0 ? (
          <div className="rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">还没有分类。</div>
        ) : (
          <div className="grid gap-2">
            {categories.map((c) => (
              <div key={c.category} className="rounded-2xl border border-black/10 bg-white/70 p-4">
                <div className="flex items-center justify-between gap-4">
                  <div className="text-sm font-medium text-neutral-900">{c.category}</div>
                  <div className="text-xs text-neutral-500">{c.count}</div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

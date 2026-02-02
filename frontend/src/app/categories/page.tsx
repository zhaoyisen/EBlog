import Link from "next/link";
import { appConfig } from "../../config/appConfig";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type Category = {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  postCount: number;
  createdAt: string;
};

function apiUrl(path: string) {
  const baseRaw = appConfig.internalApiBase;
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  return `${base}${path}`;
}

export default async function CategoriesPage() {
  let categories: Category[] = [];
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl("/api/v1/categories"), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<Category[]>;
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
          <p className="mt-1 text-sm text-neutral-600">浏览不同技术领域的文章分类</p>
        </div>

        {error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : categories.length === 0 ? (
          <div className="rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">还没有分类。</div>
        ) : (
          <div className="grid gap-3">
            {categories.map((c) => (
              <Link
                key={c.id}
                href={`/categories/${c.slug}`}
                className="rounded-2xl border border-black/10 bg-white/70 p-4 hover:border-black/20 hover:bg-white/90 transition-all"
              >
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <div className="text-sm font-medium text-neutral-900">{c.name}</div>
                    {c.description && (
                      <div className="text-xs text-neutral-500 mt-1">{c.description}</div>
                    )}
                  </div>
                  <div className="text-xs text-neutral-500 whitespace-nowrap">{c.postCount} 篇文章</div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

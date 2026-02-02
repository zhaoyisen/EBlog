import Link from "next/link";
import { appConfig } from "../../config/appConfig";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type Tag = {
  id: number;
  name: string;
  slug: string;
  postCount: number;
  createdAt: string;
};

function apiUrl(path: string) {
  const baseRaw = appConfig.internalApiBase;
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  return `${base}${path}`;
}

export default async function TagsPage() {
  let tags: Tag[] = [];
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl("/api/v1/tags"), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<Tag[]>;
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
          <p className="mt-1 text-sm text-neutral-600">通过标签发现感兴趣的文章</p>
        </div>

        {error ? (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
        ) : tags.length === 0 ? (
          <div className="rounded-2xl border border-black/10 bg-white/70 p-6 text-sm text-neutral-700">还没有标签。</div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {tags.map((t) => (
              <Link
                key={t.id}
                href={`/tags/${t.slug}`}
                className="rounded-full border border-black/10 bg-white/70 px-3 py-1.5 text-sm text-neutral-800 shadow-sm hover:bg-white hover:border-black/20 transition-all"
              >
                #{t.name}
                <span className="ml-2 text-xs text-neutral-500">{t.postCount}</span>
              </Link>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

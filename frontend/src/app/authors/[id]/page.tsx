type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type AuthorView = {
  id: number;
  nickname: string | null;
  avatarUrl: string | null;
  bio: string | null;
  createdAt: string | null;
};

function apiUrl(path: string) {
  const baseRaw = process.env.NEXT_PUBLIC_API_BASE ?? "";
  const base = baseRaw.endsWith("/") ? baseRaw.slice(0, -1) : baseRaw;
  const prefix = base === "/api" ? "" : base;
  return `${prefix}${path}`;
}

export default async function AuthorPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  let author: AuthorView | null = null;
  let error: string | null = null;

  try {
    const res = await fetch(apiUrl(`/api/v1/authors/${id}`), { cache: "no-store" });
    const json = (await res.json()) as ApiResponse<AuthorView>;
    if (json?.success && json.data) {
      author = json.data;
    } else {
      error = json?.error?.message ?? "作者不存在";
    }
  } catch {
    error = "暂时无法加载作者信息";
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="flex items-start gap-4">
            <div className="h-14 w-14 shrink-0 overflow-hidden rounded-2xl bg-gradient-to-br from-amber-200 to-rose-200" />
            <div className="min-w-0">
              <h1 className="text-2xl font-semibold tracking-tight text-neutral-900">
                {author?.nickname?.trim() ? author.nickname : `作者 #${id}`}
              </h1>
              <p className="mt-1 text-sm text-neutral-600">公开作者页（未登录可访问）</p>
            </div>
          </div>

          {error ? (
            <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">
              {error}
            </div>
          ) : (
            <div className="mt-6 grid gap-3">
              <div className="rounded-2xl border border-black/10 bg-white p-4">
                <div className="text-xs text-neutral-500">简介</div>
                <div className="mt-1 text-sm text-neutral-900">
                  {author?.bio?.trim() ? author.bio : "这个作者还没有填写简介。"}
                </div>
              </div>
              <div className="rounded-2xl border border-black/10 bg-white p-4">
                <div className="text-xs text-neutral-500">文章</div>
                <div className="mt-1 text-sm text-neutral-700">
                  文章列表将在完成文章模型后展示。
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}

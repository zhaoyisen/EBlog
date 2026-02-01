import { appConfig } from "../../config/appConfig";
import PostCard from "../../components/PostCard";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type PostSummary = {
  id: number;
  authorId: number;
  authorName?: string | null;
  authorAvatar?: string | null;
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
          <div className="mx-auto max-w-md rounded-2xl border border-rose-200 bg-rose-50 p-8 text-center text-rose-900">
            <svg className="mx-auto h-12 w-12 text-rose-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <p className="font-medium">{error}</p>
          </div>
        ) : posts.length === 0 ? (
          <div className="mx-auto max-w-md rounded-2xl border border-neutral-200 bg-white p-12 text-center text-neutral-500">
            <svg className="mx-auto h-16 w-16 text-neutral-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p className="text-lg font-medium">还没有文章</p>
            <p className="mt-2 text-sm text-neutral-400">快去创建第一篇文章吧！</p>
          </div>
        ) : (
          <>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {posts.map((post) => (
                <PostCard key={post.id} post={post} />
              ))}
            </div>

            {/* 分页 */}
            <div className="mt-12 flex items-center justify-center gap-2">
              <button
                disabled
                className="px-4 py-2 text-sm font-medium text-neutral-500 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                上一页
              </button>
              <button className="px-4 py-2 text-sm font-medium text-white bg-neutral-900 border border-transparent rounded-lg hover:bg-neutral-800 transition-colors">
                1
              </button>
              <button
                disabled
                className="px-4 py-2 text-sm font-medium text-neutral-500 bg-white border border-neutral-300 rounded-lg hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                下一页
              </button>
            </div>
          </>
        )}
      </div>
    </main>
  );
}

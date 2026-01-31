import Link from "next/link";

export default function Home() {
  return (
    <div className="min-h-screen bg-[radial-gradient(1200px_circle_at_20%_10%,theme(colors.slate.200),transparent_55%),radial-gradient(900px_circle_at_80%_20%,theme(colors.amber.200),transparent_50%),linear-gradient(to_bottom,theme(colors.zinc.50),theme(colors.zinc.100))]">
      <header className="mx-auto flex w-full max-w-5xl items-center justify-between px-5 py-5">
        <div className="flex items-center gap-3">
          <div className="grid h-9 w-9 place-items-center rounded-xl bg-zinc-900 text-zinc-50">
            E
          </div>
          <div>
            <div className="text-sm font-semibold tracking-tight">EBlog</div>
            <div className="text-xs text-zinc-600">记录、分享、沉淀</div>
          </div>
        </div>
        <nav className="flex items-center gap-2 text-sm">
          <Link className="rounded-full px-3 py-2 text-zinc-700 hover:bg-white/70" href="/posts">
            文章
          </Link>
          <Link className="rounded-full px-3 py-2 text-zinc-700 hover:bg-white/70" href="/authors">
            作者
          </Link>
          <Link className="rounded-full bg-zinc-900 px-4 py-2 font-medium text-zinc-50 hover:bg-zinc-800" href="/login">
            登录
          </Link>
        </nav>
      </header>

      <main className="mx-auto w-full max-w-5xl px-5 pb-16 pt-10">
        <section className="grid gap-8 lg:grid-cols-12">
          <div className="lg:col-span-7">
            <h1 className="text-4xl font-bold tracking-tight text-zinc-900 sm:text-5xl">
              EBlog
            </h1>
            <p className="mt-4 text-base leading-7 text-zinc-700">
              一个面向开发者的多用户技术博客平台：公开阅读，邀请码注册；支持 Markdown 投稿、评论、收藏与点赞；
              发布后异步审核，违规内容可事后下架。
            </p>

            <div className="mt-6 flex flex-col gap-3 sm:flex-row">
              <Link
                className="inline-flex items-center justify-center rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-zinc-50 shadow-sm hover:bg-zinc-800"
                href="/posts"
              >
                立即开始
              </Link>
              <Link
                className="inline-flex items-center justify-center rounded-xl border border-zinc-200 bg-white/70 px-5 py-3 text-sm font-medium text-zinc-900 hover:bg-white"
                href="/posts"
              >
                查看最新文章
              </Link>
            </div>

            <div className="mt-8 grid gap-3 sm:grid-cols-3">
              <div className="rounded-2xl border border-white/60 bg-white/70 p-4">
                <div className="text-xs font-medium text-zinc-600">投稿格式</div>
                <div className="mt-1 font-semibold text-zinc-900">Markdown</div>
                <div className="mt-1 text-xs text-zinc-600">管理员可用 MDX（基础组件）</div>
              </div>
              <div className="rounded-2xl border border-white/60 bg-white/70 p-4">
                <div className="text-xs font-medium text-zinc-600">审核机制</div>
                <div className="mt-1 font-semibold text-zinc-900">规则 + 人工</div>
                <div className="mt-1 text-xs text-zinc-600">大模型后续可插拔</div>
              </div>
              <div className="rounded-2xl border border-white/60 bg-white/70 p-4">
                <div className="text-xs font-medium text-zinc-600">交互功能</div>
                <div className="mt-1 font-semibold text-zinc-900">评论 / 收藏</div>
                <div className="mt-1 text-xs text-zinc-600">登录后可操作</div>
              </div>
            </div>
          </div>

          <aside className="lg:col-span-5">
            <div className="rounded-3xl border border-white/60 bg-white/70 p-6 shadow-sm">
              <div className="text-sm font-semibold text-zinc-900">开发状态</div>
              <p className="mt-2 text-sm text-zinc-700">
                当前项目正在初始化中：先搭建 Docker 基础设施、后端 Spring Boot 与前端 Next.js 脚手架，
                然后再实现认证、审核、投稿、评论等核心链路。
              </p>
              <div className="mt-4 rounded-2xl bg-zinc-950 p-4 font-mono text-xs text-zinc-100">
                <div className="text-zinc-400"># 后续任务会补齐命令</div>
                <div>docker compose -f infra/docker-compose.yml up -d</div>
                <div>mvn -f backend/pom.xml test</div>
                <div>npm -C frontend run dev</div>
              </div>
            </div>
          </aside>
        </section>
      </main>
    </div>
  );
}

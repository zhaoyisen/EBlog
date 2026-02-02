import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import type { ReactNode } from "react";

type AdminShellProps = {
  title: string;
  description?: string;
  showBack?: boolean;
  children: ReactNode;
};

export function AdminShell({ title, description, showBack = true, children }: AdminShellProps) {
  return (
    <main className="min-h-[calc(100vh-4rem)] bg-[radial-gradient(900px_circle_at_10%_10%,theme(colors.slate.200),transparent_55%),radial-gradient(800px_circle_at_90%_20%,theme(colors.amber.200),transparent_55%),linear-gradient(to_bottom,theme(colors.zinc.50),theme(colors.zinc.100))] px-5 py-10">
      <div className="mx-auto w-full max-w-5xl">
        <div className="mb-8">
          {showBack && (
            <Link
              href="/admin"
              className="inline-flex items-center gap-2 text-sm font-medium text-neutral-600 hover:text-neutral-900 transition-colors mb-4"
            >
              <ArrowLeft className="w-4 h-4" />
              返回管理首页
            </Link>
          )}
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">{title}</h1>
          {description ? <p className="mt-2 text-sm text-neutral-600">{description}</p> : null}
        </div>
        {children}
      </div>
    </main>
  );
}

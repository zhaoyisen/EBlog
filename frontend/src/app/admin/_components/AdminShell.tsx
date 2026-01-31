import type { ReactNode } from "react";

type AdminShellProps = {
  title: string;
  description?: string;
  children: ReactNode;
};

export function AdminShell({ title, description, children }: AdminShellProps) {
  return (
    <main className="min-h-[calc(100vh-4rem)] bg-[radial-gradient(900px_circle_at_10%_10%,theme(colors.slate.200),transparent_55%),radial-gradient(800px_circle_at_90%_20%,theme(colors.amber.200),transparent_55%),linear-gradient(to_bottom,theme(colors.zinc.50),theme(colors.zinc.100))] px-5 py-10">
      <div className="mx-auto w-full max-w-5xl">
        <div className="mb-8">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">{title}</h1>
          {description ? <p className="mt-2 text-sm text-neutral-600">{description}</p> : null}
        </div>
        {children}
      </div>
    </main>
  );
}

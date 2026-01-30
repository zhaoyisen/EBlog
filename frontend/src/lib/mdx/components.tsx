import React from "react";
import { Tab, Tabs } from "./Tabs";

export function Callout({ type, title, children }: { type?: string; title?: string; children: React.ReactNode }) {
  const t = (type ?? "note").toLowerCase();
  const tone =
    t === "warn" || t === "warning"
      ? "border-amber-200 bg-amber-50 text-amber-950"
      : t === "danger" || t === "error"
        ? "border-rose-200 bg-rose-50 text-rose-950"
        : "border-sky-200 bg-sky-50 text-sky-950";

  return (
    <div className={"my-4 rounded-2xl border p-4 " + tone}>
      {title ? <div className="text-sm font-semibold">{title}</div> : null}
      <div className="prose mt-2 max-w-none">{children}</div>
    </div>
  );
}

export function Details({ summary, children }: { summary?: string; children: React.ReactNode }) {
  return (
    <details className="my-4 rounded-2xl border border-black/10 bg-white p-4">
      <summary className="cursor-pointer text-sm font-medium text-neutral-900">
        {summary?.trim() ? summary : "展开"}
      </summary>
      <div className="prose mt-3 max-w-none">{children}</div>
    </details>
  );
}

export function Figure({ caption, children }: { caption?: string; children: React.ReactNode }) {
  return (
    <figure className="my-4 rounded-2xl border border-black/10 bg-white p-4">
      <div className="prose max-w-none">{children}</div>
      {caption ? <figcaption className="mt-2 text-xs text-neutral-600">{caption}</figcaption> : null}
    </figure>
  );
}

export const mdxComponents = {
  Callout,
  Details,
  Figure,
  Tabs,
  Tab,
};

"use client";

import React from "react";

export function Tabs({ children }: { children: React.ReactNode }) {
  type TabElement = React.ReactElement<{ label?: string }>;
  const tabs = React.Children.toArray(children)
    .filter(Boolean)
    .filter(React.isValidElement) as TabElement[];
  const labels = tabs.map((t) => String(t.props.label ?? "Tab"));
  const [active, setActive] = React.useState(0);

  return (
    <div className="mt-4 rounded-2xl border border-black/10 bg-white">
      <div className="flex flex-wrap gap-2 border-b border-black/10 p-2">
        {labels.map((label, i) => (
          <button
            key={label + i}
            type="button"
            onClick={() => setActive(i)}
            className={
              "rounded-xl px-3 py-1 text-sm transition " +
              (i === active ? "bg-black text-white" : "bg-black/5 text-neutral-800 hover:bg-black/10")
            }
          >
            {label}
          </button>
        ))}
      </div>
      <div className="p-4">{tabs[active]}</div>
    </div>
  );
}

export function Tab({ children }: { label?: string; children: React.ReactNode }) {
  return <div className="prose max-w-none">{children}</div>;
}

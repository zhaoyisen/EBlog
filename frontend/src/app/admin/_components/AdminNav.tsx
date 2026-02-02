import Link from "next/link";

const links = [
  { href: "/admin/users", label: "用户管理" },
  { href: "/admin/categories", label: "分类管理" },
  { href: "/admin/tags", label: "标签管理" },
  { href: "/admin/invite-codes", label: "邀请码" },
  { href: "/admin/moderation", label: "审核中心" },
];

export function AdminNav() {
  return (
    <nav className="flex flex-wrap items-center gap-2">
      {links.map((link) => (
        <Link
          key={link.href}
          href={link.href}
          className="rounded-full border border-black/10 bg-white/80 px-4 py-2 text-sm font-medium text-neutral-900 shadow-sm transition hover:-translate-y-0.5 hover:bg-white"
        >
          {link.label}
        </Link>
      ))}
    </nav>
  );
}

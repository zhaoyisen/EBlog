"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState, useEffect } from "react";
import { useAuth } from "../lib/auth/AuthProvider";
import { Menu, X, PenLine, User, LogOut, Shield, BookOpen, Users } from "lucide-react";
import { cn } from "../lib/utils";

export function Navbar() {
  const { isAuthenticated, userRole, logout } = useAuth();
  const pathname = usePathname();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Close mobile menu when route changes
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [pathname]);

  const handleLogout = async () => {
    await logout();
    window.location.href = "/";
  };

  const navLinks = [
    { href: "/posts", label: "文章", icon: BookOpen },
    { href: "/authors", label: "作者", icon: Users },
  ];

  return (
    <header
      className={cn(
        "sticky top-0 z-50 w-full transition-all duration-300",
        scrolled
          ? "border-b border-border/40 bg-background/80 backdrop-blur-md supports-[backdrop-filter]:bg-background/60"
          : "bg-transparent"
      )}
    >
      <div className="container mx-auto flex h-16 items-center justify-between px-4">
        <div className="flex items-center gap-6">
          <Link href="/" className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground font-bold">
              E
            </div>
            <span className="text-lg font-bold tracking-tight">EBlog</span>
          </Link>
          
          <nav className="hidden md:flex items-center gap-6 text-sm font-medium">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={cn(
                  "transition-colors hover:text-foreground/80",
                  pathname === link.href ? "text-foreground" : "text-foreground/60"
                )}
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>

        <div className="hidden md:flex items-center gap-4">
          {isAuthenticated ? (
            <>
              {(userRole === "ADMIN" || userRole === "ROLE_ADMIN") && (
                <Link
                  href="/admin"
                  className="flex items-center gap-2 text-sm font-medium text-foreground/60 hover:text-foreground"
                >
                  <Shield className="h-4 w-4" />
                  <span>后台</span>
                </Link>
              )}
              <Link
                href="/posts/new"
                className="flex items-center gap-2 text-sm font-medium text-foreground/60 hover:text-foreground"
              >
                <PenLine className="h-4 w-4" />
                <span>写文章</span>
              </Link>
              <Link
                href="/profile"
                className="flex items-center gap-2 text-sm font-medium text-foreground/60 hover:text-foreground"
              >
                <User className="h-4 w-4" />
                <span>我的</span>
              </Link>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 rounded-full bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
              >
                <LogOut className="h-4 w-4" />
                <span>登出</span>
              </button>
            </>
          ) : (
            <Link
              href="/login"
              className="rounded-full bg-primary px-6 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              登录
            </Link>
          )}
        </div>

        <button
          className="md:hidden"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        >
          {isMobileMenuOpen ? (
            <X className="h-6 w-6" />
          ) : (
            <Menu className="h-6 w-6" />
          )}
        </button>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="md:hidden border-t border-border bg-background px-4 py-6 space-y-4">
          <nav className="flex flex-col gap-4">
            {navLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className={cn(
                  "flex items-center gap-2 text-sm font-medium",
                  pathname === link.href ? "text-foreground" : "text-foreground/60"
                )}
              >
                <link.icon className="h-4 w-4" />
                {link.label}
              </Link>
            ))}
            {isAuthenticated ? (
              <>
                <div className="h-px bg-border my-2" />
                {(userRole === "ADMIN" || userRole === "ROLE_ADMIN") && (
                  <Link
                    href="/admin"
                    className="flex items-center gap-2 text-sm font-medium text-foreground/60"
                  >
                    <Shield className="h-4 w-4" />
                    后台
                  </Link>
                )}
                <Link
                  href="/posts/new"
                  className="flex items-center gap-2 text-sm font-medium text-foreground/60"
                >
                  <PenLine className="h-4 w-4" />
                  写文章
                </Link>
                <Link
                  href="/profile"
                  className="flex items-center gap-2 text-sm font-medium text-foreground/60"
                >
                  <User className="h-4 w-4" />
                  我的
                </Link>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-2 text-sm font-medium text-destructive"
                >
                  <LogOut className="h-4 w-4" />
                  登出
                </button>
              </>
            ) : (
              <Link
                href="/login"
                className="flex items-center gap-2 text-sm font-medium text-primary"
              >
                登录
              </Link>
            )}
          </nav>
        </div>
      )}
    </header>
  );
}

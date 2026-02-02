"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import PostCard from "@/components/PostCard";
import { Megaphone, Hash, Users, ArrowRight, Sparkles, Clock, UserCheck, PenLine } from "lucide-react";
import { cn } from "@/lib/utils";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"latest" | "featured" | "following">("latest");
  const [posts, setPosts] = useState<any[]>([]);
  const [popularTags, setPopularTags] = useState<any[]>([]);
  const [activeUsers, setActiveUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [sidebarLoading, setSidebarLoading] = useState(false);

  // Fetch main posts
  useEffect(() => {
    async function fetchPosts() {
      setLoading(true);
      try {
        const res = await fetch("/api/v1/posts?page=0&size=10");
        if (res.ok) {
          const data = await res.json();
          let postsList = [];
          if (Array.isArray(data.data)) {
             postsList = data.data;
          } else if (data.data && data.data.content) {
             postsList = data.data.content;
          } else if (data.content) {
             postsList = data.content;
          } else if (Array.isArray(data)) {
             postsList = data;
          }
          setPosts(postsList);
        } else {
          console.error("Failed to fetch posts");
        }
      } catch (err) {
        console.error("Error fetching posts:", err);
      } finally {
        setLoading(false);
      }
    }

    fetchPosts();
  }, [activeTab]);

  // Fetch popular tags
  useEffect(() => {
    async function fetchPopularTags() {
      setSidebarLoading(true);
      try {
        const res = await fetch("/api/v1/tags/popular");
        if (res.ok) {
          const data = await res.json();
          setPopularTags(data.data || []);
        } else {
          console.error("Failed to fetch popular tags");
        }
      } catch (err) {
        console.error("Error fetching popular tags:", err);
      } finally {
        setSidebarLoading(false);
      }
    }

    fetchPopularTags();
  }, []);

  // Fetch active users
  useEffect(() => {
    async function fetchActiveUsers() {
      setSidebarLoading(true);
      try {
        const res = await fetch("/api/v1/users/active");
        if (res.ok) {
          const data = await res.json();
          setActiveUsers(data.data || []);
        } else {
          console.error("Failed to fetch active users");
        }
      } catch (err) {
        console.error("Error fetching active users:", err);
      } finally {
        setSidebarLoading(false);
      }
    }

    fetchActiveUsers();
  }, []);

  return (
    <div className="min-h-screen bg-background">
      {/* Hero Section - Compact */}
      <div className="bg-muted/30 border-b border-border/40">
        <div className="container mx-auto px-4 py-12">
           <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
              欢迎来到 EBlog 社区
            </h1>
           <p className="mt-4 text-lg text-muted-foreground max-w-2xl">
              探索最新的技术文章，分享你的开发经验，与志同道合的开发者交流。
            </p>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Main Content (3 cols) */}
          <div className="lg:col-span-3 space-y-6">
            {/* Tabs */}
            <div className="flex items-center border-b border-border/40">
              <button
                onClick={() => setActiveTab("latest")}
                className={cn(
                  "flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors",
                  activeTab === "latest"
                    ? "border-primary text-primary"
                    : "border-transparent text-muted-foreground hover:text-foreground"
                )}
              >
                <Clock className="h-4 w-4" />
                最新发布
              </button>
              <button
                onClick={() => setActiveTab("featured")}
                className={cn(
                  "flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors",
                  activeTab === "featured"
                    ? "border-primary text-primary"
                    : "border-transparent text-muted-foreground hover:text-foreground"
                )}
              >
                <Sparkles className="h-4 w-4" />
                精选推荐
              </button>
              <button
                onClick={() => setActiveTab("following")}
                className={cn(
                  "flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors",
                  activeTab === "following"
                    ? "border-primary text-primary"
                    : "border-transparent text-muted-foreground hover:text-foreground"
                )}
              >
                <UserCheck className="h-4 w-4" />
                我的关注
              </button>
            </div>

            {/* Post List */}
            <div className="space-y-4">
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <PostCard key={i} post={{} as any} isLoading={true} />
                ))
              ) : posts.length === 0 ? (
                <div className="text-center text-muted-foreground py-12">
                  暂时没有文章。既然来了，不如写一篇？
                </div>
              ) : (
                posts.map((post, index) => (
                  <Link key={post.id} href={`/posts/${post.slug}`} className="block">
                    <PostCard post={post} />
                  </Link>
                ))
              )}
            </div>
          </div>

          {/* Sidebar (1 col) */}
          <div className="lg:col-span-1 space-y-6">
            {/* Announcements */}
            <div className="rounded-xl border border-border bg-card text-card-foreground shadow-sm overflow-hidden">
              <div className="p-5 pb-3 border-b border-border/40 flex items-center gap-2">
                <Megaphone className="h-5 w-5 text-primary" />
                <h3 className="font-semibold">社区公告</h3>
              </div>
              <div className="p-5 pt-3 space-y-3">
                <div className="text-center text-sm text-muted-foreground">
                  <PenLine className="h-8 w-8 mx-auto mb-2 opacity-50" />
                  暂无公告
                </div>
              </div>
            </div>

            {/* Tags Cloud */}
            <div className="rounded-xl border border-border bg-card text-card-foreground shadow-sm overflow-hidden">
              <div className="p-5 pb-3 border-b border-border/40 flex items-center gap-2">
                <Hash className="h-5 w-5 text-primary" />
                <h3 className="font-semibold">热门标签</h3>
              </div>
              <div className="p-5 pt-3">
                {sidebarLoading ? (
                  <div className="text-center text-sm text-muted-foreground py-4">加载中...</div>
                ) : popularTags.length === 0 ? (
                  <div className="text-center text-sm text-muted-foreground py-4">暂无标签</div>
                ) : (
                  <div className="flex flex-wrap gap-2">
                    {popularTags.map((tag) => (
                      <Link
                        key={tag.id || tag.name}
                        href={`/tags/${tag.slug || tag.name}`}
                        className="inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-full bg-muted hover:bg-muted/80 transition-colors text-foreground"
                      >
                        #{tag.name}
                        <span className="ml-1.5 text-muted-foreground">{tag.postCount || 0}</span>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Active Authors */}
            <div className="rounded-xl border border-border bg-card text-card-foreground shadow-sm overflow-hidden">
              <div className="p-5 pb-3 border-b border-border/40 flex items-center gap-2">
                <Users className="h-5 w-5 text-primary" />
                <h3 className="font-semibold">活跃作者</h3>
              </div>
              <div className="p-5 pt-3">
                {sidebarLoading ? (
                  <div className="text-center text-sm text-muted-foreground py-4">加载中...</div>
                ) : activeUsers.length === 0 ? (
                  <div className="text-center text-sm text-muted-foreground py-4">暂无活跃用户</div>
                ) : (
                  <div className="space-y-3">
                    {activeUsers.map((user) => (
                      <Link
                        key={user.id || user.username}
                        href={`/users/${user.username}`}
                        className="flex items-center gap-3 p-2 rounded-lg hover:bg-muted/50 transition-colors group"
                      >
                        <div className="h-9 w-9 rounded-full bg-primary/10 flex items-center justify-center text-primary text-sm font-medium">
                          {user.displayName?.[0] || user.username?.[0] || "?"}
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="font-medium text-sm truncate text-foreground group-hover:text-primary transition-colors">
                            {user.displayName || user.username}
                          </div>
                          <div className="text-xs text-muted-foreground">
                            {user.postCount || 0} 篇文章
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

"use client";

import Link from "next/link";
import React, { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../lib/auth/AuthProvider";
import { UserList } from "../../components/UserList";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type UserView = {
  id: number;
  email: string;
  nickname: string;
  avatarUrl: string;
  bio: string;
  createdAt: string;
};

type TabId = "profile" | "posts" | "following" | "followers" | "settings";

type MyPostView = {
  id: number;
  title: string;
  slug: string;
  summary: string;
  category: string;
  status: "DRAFT" | "PUBLISHED" | "ARCHIVED";
  createdAt: string;
  updatedAt: string;
};

export default function ProfilePage() {
  const { accessToken, ready, apiFetch, logout } = useAuth();

  const [activeTab, setActiveTab] = useState<TabId>("profile");
  const [user, setUser] = useState<UserView | null>(null);
  const [myPosts, setMyPosts] = useState<MyPostView[]>([]);
  const [postsLoading, setPostsLoading] = useState(false);
  
  const [following, setFollowing] = useState<UserView[]>([]);
  const [followers, setFollowers] = useState<UserView[]>([]);
  const [socialLoading, setSocialLoading] = useState(false);
  
  // Edit Profile State
  const [nickname, setNickname] = useState("");
  const [bio, setBio] = useState("");
  const [avatar, setAvatar] = useState("");
  
  // Password State
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const res = await apiFetch("/api/v1/me", { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<UserView>;
      if (json?.success && json.data) {
        setUser(json.data);
        setNickname(json.data.nickname || "");
        setBio(json.data.bio || "");
        setAvatar(json.data.avatarUrl || "");
      } else {
        setError(json?.error?.message ?? "加载用户信息失败");
      }
    } catch {
      setError("暂时无法加载用户信息");
    } finally {
      setLoading(false);
    }
  }, [apiFetch]);

  const fetchMyPosts = useCallback(async () => {
    setPostsLoading(true);
    try {
      const res = await apiFetch("/api/v1/posts/my?limit=50", { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<MyPostView[]>;
      if (json?.success && json.data) {
        setMyPosts(json.data);
      }
    } catch {
      console.error("Failed to fetch posts");
    } finally {
      setPostsLoading(false);
    }
  }, [apiFetch]);

  const fetchFollowing = useCallback(async () => {
    if (!user) return;
    setSocialLoading(true);
    try {
      const res = await apiFetch(`/api/v1/users/${user.id}/following`, { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<UserView[]>;
      if (json?.success && json.data) {
        setFollowing(json.data);
      }
    } catch {
      console.error("Failed to fetch following");
    } finally {
      setSocialLoading(false);
    }
  }, [apiFetch, user]);

  const fetchFollowers = useCallback(async () => {
    if (!user) return;
    setSocialLoading(true);
    try {
      const res = await apiFetch(`/api/v1/users/${user.id}/followers`, { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<UserView[]>;
      if (json?.success && json.data) {
        setFollowers(json.data);
      }
    } catch {
      console.error("Failed to fetch followers");
    } finally {
      setSocialLoading(false);
    }
  }, [apiFetch, user]);

  useEffect(() => {
    if (!ready) return;
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }
    void fetchProfile();
  }, [accessToken, fetchProfile, ready]);

  useEffect(() => {
    if (!ready || !accessToken) return;

    if (activeTab === "posts") {
      void fetchMyPosts();
    } else if (activeTab === "following") {
      void fetchFollowing();
    } else if (activeTab === "followers") {
      void fetchFollowers();
    }
  }, [activeTab, fetchMyPosts, fetchFollowing, fetchFollowers, ready, accessToken]);

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setLoading(true);
    setError("");
    setSuccessMsg("");

    try {
      const uploadRes = await apiFetch("/api/v1/uploads/presign", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          filename: file.name,
          contentType: file.type || "image/png",
        }),
      });

      const uploadJson = (await uploadRes.json()) as ApiResponse<{
        objectKey: string;
        uploadUrl: string;
        publicUrl: string;
      }>;

      if (uploadJson?.success && uploadJson.data) {
        await fetch(uploadJson.data.uploadUrl, {
          method: "PUT",
          body: file,
        });
        setAvatar(uploadJson.data.publicUrl);
        
        // Auto-save avatar update
        await updateProfile(uploadJson.data.publicUrl);
      } else {
        setError(uploadJson?.error?.message ?? "头像上传失败");
      }
    } catch {
      setError("头像上传失败");
    } finally {
      setLoading(false);
    }
  };

  const updateProfile = async (newAvatarUrl?: string) => {
    setLoading(true);
    setError("");
    setSuccessMsg("");

    try {
      const res = await apiFetch("/api/v1/me", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nickname: nickname.trim(),
          bio: bio.trim(),
          avatarUrl: newAvatarUrl || avatar,
        }),
      });

      const json = (await res.json()) as ApiResponse<UserView>;
      if (json?.success && json.data) {
        setUser(json.data);
        setSuccessMsg("个人信息已更新");
      } else {
        setError(json?.error?.message ?? "更新信息失败");
      }
    } catch {
      setError("更新信息失败");
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async () => {
    if (newPassword !== confirmPassword) {
      setError("两次输入的密码不一致");
      return;
    }
    if (newPassword.length < 6) {
      setError("密码长度至少为6位");
      return;
    }

    setLoading(true);
    setError("");
    setSuccessMsg("");

    try {
      const res = await apiFetch("/api/v1/me/password", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          current_password: currentPassword,
          new_password: newPassword,
        }),
      });

      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        setSuccessMsg("密码修改成功");
        setCurrentPassword("");
        setNewPassword("");
        setConfirmPassword("");
      } else {
        setError(json?.error?.message ?? "修改密码失败");
      }
    } catch {
      setError("修改密码失败");
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    void (async () => {
      await logout();
      window.location.href = "/login";
    })();
  };

  if (!ready || !user) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200 border-t-zinc-900"></div>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-zinc-50/50 px-4 py-12 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-4xl">
        {/* Header Section */}
        <div className="mb-12 flex flex-col items-center text-center sm:flex-row sm:items-start sm:text-left gap-8">
          <div className="relative group">
            <div className="h-32 w-32 overflow-hidden rounded-full border-4 border-white shadow-xl">
              {avatar ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img src={avatar} alt={nickname} className="h-full w-full object-cover" />
              ) : (
                <div className="flex h-full w-full items-center justify-center bg-gradient-to-br from-zinc-800 to-zinc-900 text-4xl font-bold text-white">
                  {nickname?.charAt(0)?.toUpperCase() || "U"}
                </div>
              )}
            </div>
            <label className="absolute bottom-0 right-0 flex h-10 w-10 cursor-pointer items-center justify-center rounded-full bg-zinc-900 text-white shadow-lg transition hover:bg-zinc-700">
              <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0118.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <input type="file" accept="image/*" onChange={handleAvatarUpload} className="hidden" disabled={loading} />
            </label>
          </div>
          
          <div className="flex-1 pt-2">
            <h1 className="text-3xl font-bold tracking-tight text-zinc-900">{nickname}</h1>
            <div className="mt-2 flex flex-wrap items-center justify-center sm:justify-start gap-4 text-sm text-zinc-500">
              <span className="flex items-center gap-1">
                <span className="font-mono text-xs bg-zinc-100 px-2 py-1 rounded">ID: {user.id}</span>
              </span>
              <span className="flex items-center gap-1">
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                {user.email || "Email hidden"}
              </span>
            </div>
            <p className="mt-4 max-w-xl text-zinc-600 leading-relaxed">
              {user.bio || "暂无简介"}
            </p>
          </div>
        </div>

        {/* Tabs Navigation */}
        <div className="mb-8 border-b border-zinc-200">
          <nav className="-mb-px flex space-x-8" aria-label="Tabs">
            {[
              { id: "profile", label: "编辑资料" },
              { id: "posts", label: "我的文章" },
              { id: "following", label: "我的关注" },
              { id: "followers", label: "我的粉丝" },
              { id: "settings", label: "账户设置" },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as TabId)}
                className={`
                  whitespace-nowrap border-b-2 py-4 px-1 text-sm font-medium transition-colors
                  ${activeTab === tab.id
                    ? "border-zinc-900 text-zinc-900"
                    : "border-transparent text-zinc-500 hover:border-zinc-300 hover:text-zinc-700"}
                `}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>

        {/* Feedback Messages */}
        {error && (
          <div className="mb-6 rounded-lg bg-rose-50 p-4 text-sm text-rose-600 border border-rose-100">
            {error}
          </div>
        )}
        {successMsg && (
          <div className="mb-6 rounded-lg bg-emerald-50 p-4 text-sm text-emerald-600 border border-emerald-100">
            {successMsg}
          </div>
        )}

        {/* Tab Content */}
        <div className="min-h-[400px]">
          {activeTab === "profile" && (
            <div className="max-w-2xl space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
              <div>
                <label className="block text-sm font-medium text-zinc-700">昵称</label>
                <input
                  type="text"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  className="mt-1 block w-full rounded-lg border border-zinc-300 px-3 py-2 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 sm:text-sm"
                  placeholder="您的昵称"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-zinc-700">个人简介</label>
                <textarea
                  rows={4}
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  className="mt-1 block w-full rounded-lg border border-zinc-300 px-3 py-2 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 sm:text-sm"
                  placeholder="介绍一下自己..."
                />
              </div>
              <div className="pt-4">
                <button
                  onClick={() => updateProfile()}
                  disabled={loading}
                  className="rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-zinc-800 focus:outline-none focus:ring-2 focus:ring-zinc-500 focus:ring-offset-2 disabled:opacity-50"
                >
                  {loading ? "保存中..." : "保存修改"}
                </button>
              </div>
            </div>
          )}

          {activeTab === "posts" && (
            <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
              {myPosts.length === 0 && !postsLoading ? (
                <div className="rounded-2xl border border-dashed border-zinc-300 bg-zinc-50 p-12 text-center">
                  <svg className="mx-auto h-12 w-12 text-zinc-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                  </svg>
                  <h3 className="mt-2 text-sm font-medium text-zinc-900">暂无文章</h3>
                  <p className="mt-1 text-sm text-zinc-500">您还没有发布任何文章。</p>
                  <div className="mt-6">
                    <Link
                      href="/posts/new"
                      className="inline-flex items-center rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-zinc-800"
                    >
                      <svg className="-ml-1 mr-2 h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                      </svg>
                      写文章
                    </Link>
                  </div>
                </div>
              ) : (
                <div className="space-y-6">
                  {/* Drafts Section */}
                  {myPosts.filter(p => p.status === "DRAFT").length > 0 && (
                    <div>
                      <h3 className="text-sm font-medium text-zinc-500 mb-4">草稿 ({myPosts.filter(p => p.status === "DRAFT").length})</h3>
                      <div className="space-y-3">
                        {myPosts.filter(p => p.status === "DRAFT").map(post => (
                          <div key={post.id} className="rounded-lg border border-zinc-200 bg-white p-4 shadow-sm hover:shadow-md transition-shadow">
                            <div className="flex items-start justify-between gap-4">
                              <div className="flex-1 min-w-0">
                                <h4 className="font-medium text-zinc-900 truncate">{post.title || "无标题"}</h4>
                                <p className="mt-1 text-sm text-zinc-500 line-clamp-2">{post.summary || "暂无摘要"}</p>
                                <div className="mt-2 flex items-center gap-3 text-xs text-zinc-400">
                                  <span>最后编辑: {new Date(post.updatedAt).toLocaleDateString()}</span>
                                  {post.category && <span>• {post.category}</span>}
                                </div>
                              </div>
                              <div className="flex gap-2 shrink-0">
                                <Link
                                  href={`/posts/${post.id}/edit`}
                                  className="inline-flex items-center rounded-md bg-zinc-100 px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-200 transition-colors"
                                >
                                  编辑
                                </Link>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Published Section */}
                  {myPosts.filter(p => p.status === "PUBLISHED").length > 0 && (
                    <div>
                      <h3 className="text-sm font-medium text-zinc-500 mb-4">已发布 ({myPosts.filter(p => p.status === "PUBLISHED").length})</h3>
                      <div className="space-y-3">
                        {myPosts.filter(p => p.status === "PUBLISHED").map(post => (
                          <div key={post.id} className="rounded-lg border border-zinc-200 bg-white p-4 shadow-sm hover:shadow-md transition-shadow">
                            <div className="flex items-start justify-between gap-4">
                              <div className="flex-1 min-w-0">
                                <h4 className="font-medium text-zinc-900 truncate">{post.title}</h4>
                                <p className="mt-1 text-sm text-zinc-500 line-clamp-2">{post.summary || "暂无摘要"}</p>
                                <div className="mt-2 flex items-center gap-3 text-xs text-zinc-400">
                                  <span>发布于: {new Date(post.createdAt).toLocaleDateString()}</span>
                                  {post.category && <span>• {post.category}</span>}
                                </div>
                              </div>
                              <div className="flex gap-2 shrink-0">
                                <Link
                                  href={`/posts/${post.slug}`}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="inline-flex items-center rounded-md bg-zinc-100 px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-200 transition-colors"
                                >
                                  查看
                                </Link>
                                <Link
                                  href={`/posts/${post.id}/edit`}
                                  className="inline-flex items-center rounded-md bg-zinc-100 px-3 py-1.5 text-sm font-medium text-zinc-700 hover:bg-zinc-200 transition-colors"
                                >
                                  编辑
                                </Link>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {activeTab === "following" && (
             <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
               {socialLoading ? (
                 <div className="py-12 text-center">
                   <div className="inline-block h-6 w-6 animate-spin rounded-full border-2 border-neutral-200 border-t-neutral-900"></div>
                 </div>
               ) : (
                 <UserList 
                   users={following} 
                   emptyMessage="您还没有关注任何人"
                   onUserUpdate={fetchFollowing}
                 />
               )}
             </div>
          )}

          {activeTab === "followers" && (
             <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
               {socialLoading ? (
                 <div className="py-12 text-center">
                   <div className="inline-block h-6 w-6 animate-spin rounded-full border-2 border-neutral-200 border-t-neutral-900"></div>
                 </div>
               ) : (
                 <UserList 
                   users={followers} 
                   emptyMessage="您还没有粉丝"
                   onUserUpdate={fetchFollowers}
                 />
               )}
             </div>
          )}

          {activeTab === "settings" && (
            <div className="max-w-2xl space-y-10 animate-in fade-in slide-in-from-bottom-4 duration-500">
              <div className="space-y-6">
                <h3 className="text-lg font-medium leading-6 text-zinc-900">修改密码</h3>
                <div className="grid gap-6">
                  <div>
                    <label className="block text-sm font-medium text-zinc-700">当前密码</label>
                    <input
                      type="password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      className="mt-1 block w-full rounded-lg border border-zinc-300 px-3 py-2 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 sm:text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-zinc-700">新密码</label>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      className="mt-1 block w-full rounded-lg border border-zinc-300 px-3 py-2 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 sm:text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-zinc-700">确认新密码</label>
                    <input
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      className="mt-1 block w-full rounded-lg border border-zinc-300 px-3 py-2 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 sm:text-sm"
                    />
                  </div>
                </div>
                <button
                  onClick={handlePasswordChange}
                  disabled={loading}
                  className="rounded-lg bg-white border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 shadow-sm hover:bg-zinc-50 focus:outline-none focus:ring-2 focus:ring-zinc-500 focus:ring-offset-2 disabled:opacity-50"
                >
                  {loading ? "修改中..." : "更新密码"}
                </button>
              </div>

              <div className="border-t border-zinc-200 pt-10">
                <h3 className="text-lg font-medium leading-6 text-rose-600">危险区域</h3>
                <div className="mt-4">
                  <button
                    onClick={handleLogout}
                    className="rounded-lg bg-rose-50 px-4 py-2 text-sm font-medium text-rose-600 hover:bg-rose-100 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:ring-offset-2"
                  >
                    退出登录
                  </button>
                  <p className="mt-2 text-sm text-zinc-500">
                    退出当前账户登录状态。
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}

"use client";

import React, { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../lib/auth/AuthProvider";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type UserView = {
  id: number;
  nickname: string;
  avatarUrl: string;
  bio: string;
  createdAt: string;
};

export default function ProfilePage() {
  const { accessToken, ready, apiFetch, logout } = useAuth();

  const [view, setView] = useState<"view" | "edit">("view");
  const [nickname, setNickname] = useState("");
  const [bio, setBio] = useState("");
  const [avatar, setAvatar] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPasswordSection, setShowPasswordSection] = useState(false);

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch("/api/v1/me", { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<UserView>;
      if (json?.success && json.data) {
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

  useEffect(() => {
    if (!ready) {
      return;
    }
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }
    void fetchProfile();
  }, [accessToken, fetchProfile, ready]);

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) {
      setError("请选择文件");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const uploadRes = await apiFetch("/api/v1/uploads/presign", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          filename: file.name,
          contentType: file.type || "image/png",
        }),
      });

      const uploadJson = (await uploadRes.json()) as ApiResponse<{
        objectKey: string;
        uploadUrl: string;
        publicUrl: string;
        contentType: string;
        maxBytes: number;
      }>;

      if (uploadJson?.success && uploadJson.data) {
        await fetch(uploadJson.data.uploadUrl, {
          method: "PUT",
          body: file,
        });
        setAvatar(uploadJson.data.publicUrl);
        setError("头像更新成功");
      } else {
        setError(uploadJson?.error?.message ?? "头像上传失败");
      }
    } catch {
      setError("头像上传失败");
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch("/api/v1/me", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          nickname: nickname.trim(),
          bio: bio.trim(),
        }),
      });

      const json = (await res.json()) as ApiResponse<{ id: number; nickname: string; bio: string }>;
      if (json?.success && json.data) {
        setNickname(json.data.nickname);
        setBio(json.data.bio);
        setError("信息更新成功");
        setView("view");
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

    try {
      const res = await apiFetch("/api/v1/me/password", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          current_password: currentPassword,
          new_password: newPassword,
        }),
      });

      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        setError("密码修改成功");
        setShowPasswordSection(false);
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

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-bold tracking-tight text-neutral-900">
            个人中心
          </h1>
          <p className="mt-2 text-sm text-neutral-600">
            查看和编辑您的个人信息
          </p>
        </div>

        {error && (
          <div className="rounded-2xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-900">
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-4 border-zinc-200"></div>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="rounded-2xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
              <div className="flex items-center gap-4 mb-6">
                {avatar ? (
                  // eslint-disable-next-line @next/next/no-img-element -- 远程头像来源不固定，暂不使用 next/image
                  <img
                    src={avatar}
                    alt={nickname || "用户头像"}
                    className="h-24 w-24 rounded-full object-cover"
                  />
                ) : (
                  <div className="h-24 w-24 rounded-full bg-gradient-to-br from-amber-200 to-rose-200 flex items-center justify-center text-2xl font-bold text-white">
                    {nickname?.charAt(0) || "U"}
                  </div>
                )}
                <div className="min-w-0 flex-1">
                  <div className="text-lg font-semibold text-neutral-900">
                    {nickname || "未命名用户"}
                  </div>
                  <div className="text-sm text-neutral-600">
                    {bio?.trim() ? bio : "这个用户还没有填写简介。"}
                  </div>
                </div>
                <label className="cursor-pointer rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white hover:bg-zinc-800">
                  更换头像
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarUpload}
                    className="hidden"
                    disabled={loading}
                  />
                </label>
              </div>

              {view === "edit" ? (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-neutral-900">昵称</label>
                    <input
                      type="text"
                      value={nickname}
                      onChange={(e) => setNickname(e.target.value)}
                      placeholder="请输入昵称"
                      className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-neutral-900">简介</label>
                    <textarea
                      value={bio}
                      onChange={(e) => setBio(e.target.value)}
                      rows={4}
                      placeholder="请输入个人简介"
                      className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    />
                  </div>

                  <div className="flex gap-4">
                    <button
                      onClick={handleSave}
                      disabled={loading}
                      className="flex-1 rounded-xl bg-green-600 px-5 py-3 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
                    >
                      {loading ? "保存中..." : "保存"}
                    </button>
                    <button
                      onClick={() => setView("view")}
                      disabled={loading}
                      className="flex-1 rounded-xl border border-black/10 bg-white px-5 py-3 text-sm font-medium text-zinc-900 hover:bg-zinc-50 disabled:opacity-50"
                    >
                      取消
                    </button>
                  </div>
                </div>
              ) : (
                <button
                  onClick={() => setView("edit")}
                  className="w-full rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white hover:bg-zinc-800"
                >
                  编辑资料
                </button>
              )}
            </div>

            <div className="rounded-2xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
              <button
                onClick={() => setShowPasswordSection(!showPasswordSection)}
                className="w-full rounded-xl border border-amber-500 bg-amber-50 px-5 py-3 text-sm font-medium text-amber-900 hover:bg-amber-100"
              >
                {showPasswordSection ? "取消修改密码" : "修改密码"}
              </button>

              {showPasswordSection && (
                <div className="mt-6 space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-neutral-900">当前密码</label>
                    <input
                      type="password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      placeholder="请输入当前密码"
                      className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-neutral-900">新密码</label>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="请输入新密码（至少6位）"
                      className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-neutral-900">确认新密码</label>
                    <input
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="再次输入新密码"
                      className="mt-1 w-full rounded-xl border border-black/10 bg-white px-4 py-3 outline-none focus:ring-2 focus:ring-black/10 focus:ring-offset-2"
                    />
                  </div>

                  <button
                    onClick={handlePasswordChange}
                    disabled={loading}
                    className="w-full rounded-xl bg-green-600 px-5 py-3 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
                  >
                    {loading ? "修改中..." : "确认修改"}
                  </button>
                </div>
              )}
            </div>

            <button
              onClick={handleLogout}
              className="w-full rounded-xl border border-rose-200 bg-rose-50 px-5 py-3 text-sm font-medium text-rose-900 hover:bg-rose-100"
            >
              登出
            </button>
          </div>
        )}
      </div>
    </main>
  );
}

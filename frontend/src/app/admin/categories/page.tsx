"use client";

import React, { useCallback, useEffect, useState } from "react";
import { useAuth } from "../../../lib/auth/AuthProvider";
import { AdminNav } from "../_components/AdminNav";
import { AdminShell } from "../_components/AdminShell";
import { Plus, Edit, Trash2, Save } from "lucide-react";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type Category = {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  postCount: number;
  createdAt: string;
};

type CategoryFormData = {
  name: string;
  description: string;
  slug: string;
};

export default function AdminCategoriesPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState<CategoryFormData>({ name: "", description: "", slug: "" });

  const formatDate = (value: string | null) => {
    if (!value) return "-";
    return value.replace("T", " ").replace("Z", "").substring(0, 16);
  };

  const fetchCategories = useCallback(async () => {
    setLoading(true);
    setError("");

    try {
      const res = await apiFetch("/api/v1/categories", { cache: "no-store" });
      const json = (await res.json()) as ApiResponse<Category[]>;
      if (json?.success && Array.isArray(json.data)) {
        setCategories(json.data);
        return;
      }
      setError(json?.error?.message ?? "加载分类列表失败");
    } catch {
      setError("加载分类列表失败");
    } finally {
      setLoading(false);
    }
  }, [apiFetch]);

  useEffect(() => {
    if (!ready) return;
    if (!accessToken) {
      window.location.href = "/login";
      return;
    }
    void fetchCategories();
  }, [accessToken, fetchCategories, ready]);

  const resetFormData = () => {
    setFormData({ name: "", description: "", slug: "" });
    setEditingId(null);
  };

  const handleCreate = () => {
    resetFormData();
    setShowCreateModal(true);
  };

  const handleEdit = (category: Category) => {
    setFormData({
      name: category.name,
      description: category.description || "",
      slug: category.slug,
    });
    setEditingId(category.id);
    setShowCreateModal(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      setError("分类名称不能为空");
      return;
    }

    setLoading(true);
    setError("");

    const isUpdate = editingId !== null;

    try {
      const payload = {
        name: formData.name.trim(),
        description: formData.description.trim(),
        slug: formData.slug.trim(),
      };

      const url = isUpdate ? `/api/v1/categories/${editingId}` : "/api/v1/categories";
      const method = isUpdate ? "PUT" : "POST";

      const res = await apiFetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const json = (await res.json()) as ApiResponse<Category>;
      if (json?.success) {
        await fetchCategories();
        setShowCreateModal(false);
        resetFormData();
        return;
      }
      setError(json?.error?.message ?? (isUpdate ? "更新分类失败" : "创建分类失败"));
    } catch {
      setError(isUpdate ? "更新分类失败" : "创建分类失败");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("确定要删除这个分类吗？")) return;

    setLoading(true);
    setError("");

    try {
      const res = await apiFetch(`/api/v1/categories/${id}`, {
        method: "DELETE",
      });
      const json = (await res.json()) as ApiResponse<unknown>;
      if (json?.success) {
        await fetchCategories();
        return;
      }
      setError(json?.error?.message ?? "删除分类失败");
    } catch {
      setError("删除分类失败");
    } finally {
      setLoading(false);
    }
  };

  if (!ready) {
    return (
      <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
        <div className="mx-auto flex max-w-3xl items-center justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-zinc-200"></div>
        </div>
      </main>
    );
  }

  if (!accessToken) return null;

  return (
    <AdminShell title="分类管理" description="管理文章分类，包括创建、编辑和删除。">
      <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
        <AdminNav />
      </div>

      {error ? (
        <div className="mt-6 rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-900">{error}</div>
      ) : null}

      <div className="mt-6">
        <div className="flex items-center justify-between mb-4">
          <div className="text-sm text-neutral-600">共 {categories.length} 个分类</div>
          <button
            onClick={handleCreate}
            className="inline-flex items-center gap-2 rounded-xl bg-neutral-900 px-4 py-2 text-sm font-medium text-white hover:bg-neutral-800 transition-colors"
          >
            <Plus className="w-4 h-4" />
            新建分类
          </button>
        </div>

        <div className="overflow-hidden rounded-3xl border border-black/10 bg-white/70 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <div className="grid grid-cols-[1.5fr_2fr_1fr_1fr_1.2fr] gap-4 border-b border-black/10 bg-white/80 px-6 py-3 text-xs font-semibold text-neutral-600">
            <div>名称</div>
            <div>描述</div>
            <div>文章数</div>
            <div>创建时间</div>
            <div>操作</div>
          </div>

          {loading ? (
            <div className="px-6 py-8 text-sm text-neutral-600">加载中...</div>
          ) : categories.length === 0 ? (
            <div className="px-6 py-8 text-sm text-neutral-600">暂无分类数据。</div>
          ) : (
            <div className="divide-y divide-black/10">
              {categories.map((category) => (
                <div key={category.id} className="grid grid-cols-[1.5fr_2fr_1fr_1fr_1.2fr] gap-4 px-6 py-4 text-sm text-neutral-800">
                  <div>
                    <div className="font-semibold text-neutral-900">{category.name}</div>
                    <div className="text-xs text-neutral-500">{category.slug}</div>
                  </div>
                  <div className="text-xs text-neutral-600 truncate">{category.description || "-"}</div>
                  <div className="text-xs text-neutral-500">{category.postCount}</div>
                  <div className="text-xs text-neutral-500">{formatDate(category.createdAt)}</div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleEdit(category)}
                      className="rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-1.5 text-xs font-medium text-neutral-700 hover:bg-neutral-100 transition-colors"
                    >
                      <Edit className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => handleDelete(category.id)}
                      className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-1.5 text-xs font-medium text-rose-700 hover:bg-rose-100 transition-colors"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Create/Edit Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="w-full max-w-lg rounded-3xl border border-black/10 bg-white p-8 shadow-2xl">
            <h2 className="text-2xl font-bold text-neutral-900 mb-6">
              {editingId ? "编辑分类" : "新建分类"}
            </h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label htmlFor="name" className="block text-sm font-medium text-neutral-900 mb-1">
                  分类名称 <span className="text-rose-500">*</span>
                </label>
                <input
                  id="name"
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="例如：前端开发"
                  className="block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm"
                  disabled={loading}
                  required
                />
              </div>

              <div>
                <label htmlFor="slug" className="block text-sm font-medium text-neutral-900 mb-1">
                  URL 别名
                </label>
                <input
                  id="slug"
                  type="text"
                  value={formData.slug}
                  onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
                  placeholder="例如：frontend"
                  className="block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm"
                  disabled={loading}
                />
              </div>

              <div>
                <label htmlFor="description" className="block text-sm font-medium text-neutral-900 mb-1">
                  描述
                </label>
                <textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="简短描述这个分类..."
                  rows={3}
                  className="block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm resize-none"
                  disabled={loading}
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    resetFormData();
                  }}
                  className="flex-1 rounded-xl border border-neutral-300 px-4 py-2.5 text-sm font-medium text-neutral-700 hover:bg-neutral-50 transition-colors"
                  disabled={loading}
                >
                  取消
                </button>
                <button
                  type="submit"
                  className="flex-1 inline-flex items-center justify-center gap-2 rounded-xl bg-neutral-900 px-4 py-2.5 text-sm font-medium text-white hover:bg-neutral-800 transition-colors"
                  disabled={loading}
                >
                  <Save className="w-4 h-4" />
                  {loading ? "保存中..." : editingId ? "更新" : "创建"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AdminShell>
  );
}

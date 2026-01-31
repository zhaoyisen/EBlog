"use client";

import React from "react";
import { useAuth } from "../../lib/auth/AuthProvider";

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
};

type PresignResponse = {
  bucket: string;
  objectKey: string;
  uploadUrl: string;
  publicUrl: string;
  contentType: string;
  maxBytes: number;
};

export default function UploadPage() {
  const { accessToken, ready, apiFetch } = useAuth();

  const [file, setFile] = React.useState<File | null>(null);
  const [busy, setBusy] = React.useState(false);
  const [log, setLog] = React.useState<string>("");
  const [publicUrl, setPublicUrl] = React.useState<string>("");

  React.useEffect(() => {
    if (ready && !accessToken) {
      window.location.href = "/login";
    }
  }, [accessToken, ready]);

  async function onUpload() {
    if (!file) {
      setLog("请选择文件");
      return;
    }
    if (!ready || !accessToken) {
      setLog("请先登录");
      return;
    }

    setBusy(true);
    setLog("");
    setPublicUrl("");

    try {
      const presignRes = await apiFetch("/api/v1/uploads/presign", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          filename: file.name,
          contentType: file.type || "application/octet-stream",
          sizeBytes: file.size,
        }),
      });

      const presignJson = (await presignRes.json()) as ApiResponse<PresignResponse>;
      if (!presignJson.success || !presignJson.data) {
        setLog(presignJson.error?.message ?? "获取上传地址失败");
        return;
      }

      const { uploadUrl, publicUrl: url, contentType } = presignJson.data;
      setLog("已获取上传地址，开始上传...");

      const putRes = await fetch(uploadUrl, {
        method: "PUT",
        headers: {
          "Content-Type": contentType,
        },
        body: file,
      });

      if (!putRes.ok) {
        setLog(`上传失败：HTTP ${putRes.status}`);
        return;
      }

      setPublicUrl(url);
      setLog("上传成功");
    } catch (e) {
      setLog("上传失败（网络或配置问题）");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="min-h-[calc(100vh-4rem)] px-5 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-6">
          <h1 className="text-3xl font-semibold tracking-tight text-neutral-900">上传</h1>
          <p className="mt-1 text-sm text-neutral-600">MVP：登录后生成预签名 URL（MinIO）</p>
        </div>

        <div className="rounded-3xl border border-black/10 bg-white/70 p-6 shadow-[0_10px_40px_rgba(0,0,0,0.08)] backdrop-blur">
          <label className="mt-5 block text-sm font-medium text-neutral-900">文件</label>
          <input
            type="file"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            className="mt-2 block w-full text-sm"
          />

          <button
            type="button"
            onClick={onUpload}
            disabled={busy}
            className="mt-5 inline-flex items-center justify-center rounded-2xl bg-black px-5 py-3 text-sm font-medium text-white disabled:opacity-50"
          >
            {busy ? "上传中..." : "开始上传"}
          </button>

          {log ? (
            <div className="mt-4 rounded-2xl border border-black/10 bg-white p-4 text-sm text-neutral-800">{log}</div>
          ) : null}

          {publicUrl ? (
            <div className="mt-4 rounded-2xl border border-black/10 bg-white p-4">
              <div className="text-xs text-neutral-500">公开 URL</div>
              <a className="mt-1 block break-all text-sm text-blue-700 underline" href={publicUrl} target="_blank" rel="noreferrer">
                {publicUrl}
              </a>
              <div className="mt-3 text-xs text-neutral-500">Markdown 引用</div>
              <pre className="mt-1 whitespace-pre-wrap rounded-xl bg-black/5 p-3 text-sm text-neutral-900">{`![](${publicUrl})`}</pre>
            </div>
          ) : null}
        </div>
      </div>
    </main>
  );
}

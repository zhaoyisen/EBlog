"use client";

import { useState, useEffect, useCallback } from "react";
import { useAuth } from "@/lib/auth/AuthProvider";
import { toast } from "sonner";
import { UserPlus, UserCheck } from "lucide-react";
import { parseJwt } from "@/lib/auth/auth";

interface FollowButtonProps {
  targetUserId: number;
  initialIsFollowing?: boolean;
  className?: string;
  onFollowChange?: (isFollowing: boolean) => void;
}

export function FollowButton({ targetUserId, initialIsFollowing, className, onFollowChange }: FollowButtonProps) {
  const { isAuthenticated, apiFetch, accessToken } = useAuth();
  const [isFollowing, setIsFollowing] = useState(initialIsFollowing || false);
  const [loading, setLoading] = useState(false);
  const [checking, setChecking] = useState(!initialIsFollowing && isAuthenticated);

  const currentUserId = accessToken ? Number(parseJwt(accessToken)?.sub) : null;

  const checkStatus = useCallback(async () => {
    // If we already know the status, or user is not logged in, or it's myself
    if (!isAuthenticated || !currentUserId || initialIsFollowing !== undefined || currentUserId === targetUserId) {
      setChecking(false);
      return;
    }

    try {
      // Fetch my following list to check if targetUserId is present
      // Note: This is not ideal for large following lists, but works given current API limitations
      const res = await apiFetch(`/api/v1/users/${currentUserId}/following`);
      const json = await res.json();
      if (json.success && Array.isArray(json.data)) {
        const following = json.data.some((u: any) => u.id === targetUserId);
        setIsFollowing(following);
      }
    } catch (e) {
      console.error("Failed to check follow status", e);
    } finally {
      setChecking(false);
    }
  }, [apiFetch, currentUserId, isAuthenticated, targetUserId, initialIsFollowing]);

  useEffect(() => {
    if (initialIsFollowing === undefined) {
      checkStatus();
    } else {
      setChecking(false);
    }
  }, [checkStatus, initialIsFollowing]);

  const handleToggle = async (e: React.MouseEvent) => {
    e.preventDefault(); // Prevent parent link clicks
    e.stopPropagation();

    if (!isAuthenticated) {
      window.location.href = "/login";
      return;
    }
    
    const prev = isFollowing;
    setIsFollowing(!prev); // Optimistic
    setLoading(true);

    try {
      const method = prev ? "DELETE" : "POST";
      const res = await apiFetch(`/api/v1/users/${targetUserId}/follow`, { method });
      const json = await res.json();
      
      if (!json.success) {
        throw new Error(json.error?.message || "Operation failed");
      }
      
      toast.success(prev ? "已取消关注" : "已关注");
      onFollowChange?.(!prev);
    } catch (e: any) {
      setIsFollowing(prev);
      toast.error(e.message || "操作失败");
    } finally {
      setLoading(false);
    }
  };

  if (currentUserId === targetUserId) return null;

  if (checking) return <div className="w-16 h-7 bg-neutral-100 rounded-full animate-pulse" />;

  return (
    <button
      onClick={handleToggle}
      disabled={loading}
      className={`
        inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all
        ${isFollowing 
          ? "bg-neutral-100 text-neutral-600 hover:bg-rose-50 hover:text-rose-600 border border-neutral-200" 
          : "bg-neutral-900 text-white hover:bg-neutral-800 shadow-sm"}
        ${loading ? "opacity-70 cursor-wait" : ""}
        ${className || ""}
      `}
    >
      {isFollowing ? (
        <>
          <UserCheck className="w-3.5 h-3.5" />
          <span>已关注</span>
        </>
      ) : (
        <>
          <UserPlus className="w-3.5 h-3.5" />
          <span>关注</span>
        </>
      )}
    </button>
  );
}

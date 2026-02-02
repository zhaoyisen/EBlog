"use client";

import { FollowButton } from "./FollowButton";
import { UserBadge } from "./UserBadge";
import Link from "next/link";

interface User {
  id: number;
  nickname: string;
  avatarUrl?: string;
  bio?: string;
}

interface UserListProps {
  users: User[];
  emptyMessage?: string;
  onUserUpdate?: () => void;
}

export function UserList({ users, emptyMessage = "暂无用户", onUserUpdate }: UserListProps) {
  if (users.length === 0) {
    return (
      <div className="py-12 text-center border border-dashed border-neutral-200 rounded-xl bg-neutral-50">
        <p className="text-neutral-500 text-sm">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2">
      {users.map((user) => (
        <div key={user.id} className="flex items-center justify-between p-4 bg-white border border-neutral-100 rounded-xl shadow-sm hover:shadow-md transition-all">
          <Link href={`/users/${user.id}`} className="flex-1 min-w-0 flex items-center gap-3">
             <UserBadge name={user.nickname} avatar={user.avatarUrl} size="md" />
             {user.bio && <p className="hidden sm:block text-xs text-neutral-500 truncate max-w-[120px]">{user.bio}</p>}
          </Link>
          <FollowButton 
            targetUserId={user.id} 
            onFollowChange={onUserUpdate}
          />
        </div>
      ))}
    </div>
  );
}

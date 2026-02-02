import React from 'react';
import { cn } from '@/lib/utils';

interface UserBadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  name: string;
  avatar?: string | null;
  size?: 'sm' | 'md';
}

export function UserBadge({ name, avatar, size = 'sm', className, ...props }: UserBadgeProps) {
  const sizeClasses = {
    sm: {
      container: 'gap-2',
      avatar: 'h-6 w-6',
      text: 'text-xs',
    },
    md: {
      container: 'gap-2.5',
      avatar: 'h-8 w-8',
      text: 'text-sm',
    },
  };

  const { container, avatar: avatarSize, text } = sizeClasses[size];

  return (
    <div className={cn("flex items-center group", container, className)} {...props}>
      {avatar ? (
        <img
          src={avatar}
          alt={name}
          className={cn("rounded-full object-cover ring-1 ring-neutral-200 dark:ring-neutral-700", avatarSize)}
        />
      ) : (
        <div
          className={cn(
            "rounded-full bg-gradient-to-br from-neutral-200 to-neutral-300 dark:from-neutral-700 dark:to-neutral-600 flex items-center justify-center font-medium text-neutral-700 dark:text-neutral-200",
            avatarSize,
            size === 'sm' ? 'text-[10px]' : 'text-xs'
          )}
        >
          {name.charAt(0).toUpperCase()}
        </div>
      )}
      <span className={cn("font-medium text-neutral-600 dark:text-neutral-400 group-hover:text-neutral-900 dark:group-hover:text-neutral-200 transition-colors truncate", text)}>
        {name}
      </span>
    </div>
  );
}

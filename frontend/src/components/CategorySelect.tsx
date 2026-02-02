import React from "react";

interface Category {
  id: number;
  name: string;
  slug: string;
}

interface CategorySelectProps {
  value: string;
  onChange: (value: string) => void;
  categories: Category[];
  disabled?: boolean;
}

export default function CategorySelect({
  value,
  onChange,
  categories,
  disabled = false,
}: CategorySelectProps) {
  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      disabled={disabled}
      className="mt-2 block w-full rounded-lg border-neutral-200 bg-neutral-50 px-4 py-3 text-neutral-900 placeholder:text-neutral-400 focus:border-neutral-900 focus:bg-white focus:ring-0 sm:text-sm disabled:opacity-50 disabled:cursor-not-allowed"
    >
      <option value="">选择分类（可选）</option>
      {categories.map((category) => (
        <option key={category.id} value={category.name}>
          {category.name}
        </option>
      ))}
    </select>
  );
}

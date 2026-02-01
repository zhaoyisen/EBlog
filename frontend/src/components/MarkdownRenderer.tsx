'use client';

import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';
import 'github-markdown-css/github-markdown-light.css';
import 'highlight.js/styles/github.css';
import { useEffect, useRef } from 'react';

interface MarkdownRendererProps {
  content: string;
  className?: string;
}

export default function MarkdownRenderer({ content, className }: MarkdownRendererProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // 确保代码块高亮样式正确应用
    if (typeof window !== 'undefined' && (window as any).hljs) {
      (window as any).hljs.highlightAll();
    }
  }, [content]);

  return (
    <div ref={containerRef} className={`markdown-body ${className || ''}`}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight]}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}

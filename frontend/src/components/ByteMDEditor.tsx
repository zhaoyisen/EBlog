'use client';

import { Editor } from '@bytemd/react';
import gfm from '@bytemd/plugin-gfm';
import highlight from '@bytemd/plugin-highlight';
import frontmatter from '@bytemd/plugin-frontmatter';
import zhHans from 'bytemd/locales/zh_Hans.json';
import 'bytemd/dist/index.css';
import 'github-markdown-css/github-markdown-light.css';
import 'highlight.js/styles/github.css'; // 引入代码高亮样式

// 插件列表
const plugins = [
  gfm(),
  highlight(),
  frontmatter(),
];

interface Props {
  value: string;
  onChange: (value: string) => void;
  uploadImages?: (files: File[]) => Promise<{ url: string; title?: string; alt?: string }[]>;
  placeholder?: string;
  className?: string;
}

export default function ByteMDEditor({ value, onChange, uploadImages, placeholder, className }: Props) {
  return (
    <div className={`bytemd-container ${className || ''}`}>
      <style jsx global>{`
        .bytemd-container {
          /* 确保容器本身有高度上下文 */
          display: flex;
          flex-direction: column;
        }
        .bytemd-container .bytemd {
          height: 600px; /* 增加默认高度 */
          min-height: 500px;
          border: none;
          border-radius: 0.5rem;
        }
        /* 全屏模式下覆盖默认样式 */
        .bytemd-fullscreen.bytemd {
          height: 100vh !important;
          z-index: 9999;
        }
        /* 优化预览区域样式 */
        .markdown-body {
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
          line-height: 1.6;
          padding: 2rem;
        }
        .markdown-body ul {
          list-style: disc;
        }
        .markdown-body ol {
          list-style: decimal;
        }
        /* 修复 toolbar 在某些情况下的样式 */
        .bytemd-toolbar {
          background-color: #fff;
          border-bottom: 1px solid #e5e7eb;
        }
      `}</style>
      <Editor
        value={value}
        plugins={plugins}
        onChange={onChange}
        uploadImages={uploadImages}
        locale={zhHans}
        placeholder={placeholder}
        editorConfig={{
          // CodeMirror 配置
          lineNumbers: true,
        }}
      />
    </div>
  );
}

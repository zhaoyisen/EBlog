import React from "react";
import { compile, run } from "@mdx-js/mdx";
import remarkGfm from "remark-gfm";
import * as runtime from "react/jsx-runtime";
import { mdxComponents } from "./components";
import { rehypeLinkPolicy } from "./rehypeLinkPolicy";
import { remarkSafeMdx } from "./remarkSafeMdx";

// MDX component maps are intentionally permissive: different components use different props.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ComponentsMap = Record<string, React.ComponentType<any>>;

type MdxProps = { components?: ComponentsMap };

type MdxModule = { default: React.ComponentType<MdxProps> };

const cache = new Map<string, React.ComponentType<MdxProps>>();

export async function validateMdxOrThrow(source: string) {
  await compile(source, {
    outputFormat: "function-body",
    providerImportSource: "@mdx-js/react",
    remarkPlugins: [remarkGfm, remarkSafeMdx],
    rehypePlugins: [rehypeLinkPolicy],
  });
}

export async function renderMdxCached(key: string, source: string) {
  const hit = cache.get(key);
  if (hit) {
    const Hit = hit;
    return <Hit components={mdxComponents} />;
  }

  const file = await compile(source, {
    outputFormat: "function-body",
    providerImportSource: "@mdx-js/react",
    remarkPlugins: [remarkGfm, remarkSafeMdx],
    rehypePlugins: [rehypeLinkPolicy],
  });

  const code = String(file);
  const mod = (await run(code, { ...runtime, Fragment: React.Fragment })) as unknown as MdxModule;
  const Content = mod.default;
  cache.set(key, Content);
  return <Content components={mdxComponents} />;
}

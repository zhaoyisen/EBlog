import { visit } from "unist-util-visit";

type MdxJsxAttribute = {
  type: string;
  name: string;
  value?: unknown;
};

type MdxJsxNode = {
  type: string;
  name?: string | null;
  attributes?: MdxJsxAttribute[];
};

const ALLOWED_COMPONENTS = new Set(["Callout", "Details", "Tabs", "Tab", "Figure"]);

const ALLOWED_ATTRS: Record<string, Set<string>> = {
  Callout: new Set(["type", "title"]),
  Details: new Set(["summary"]),
  Tabs: new Set([]),
  Tab: new Set(["label"]),
  Figure: new Set(["caption"]),
};

export function remarkSafeMdx() {
  return function transformer(tree: unknown) {
    visit(tree as unknown as { type: string }, (node: unknown) => {
      const n = asNode(node);
      const t = n.type;

      if (t === "mdxjsEsm") {
        throw new Error("MDX: esm import/export is not allowed");
      }

      if (t === "mdxFlowExpression" || t === "mdxTextExpression") {
        throw new Error("MDX: expressions are not allowed");
      }

      if (t === "html") {
        throw new Error("MDX: raw HTML is not allowed");
      }

      if (t === "mdxJsxFlowElement" || t === "mdxJsxTextElement") {
        validateJsx(n as MdxJsxNode);
      }
    });
  };
}

type AnyNode = { type?: string; name?: string | null; attributes?: unknown };

function asNode(value: unknown): AnyNode {
  if (!value || typeof value !== "object") {
    return {};
  }
  const v = value as Record<string, unknown>;
  return {
    type: typeof v.type === "string" ? v.type : undefined,
    name: typeof v.name === "string" ? v.name : v.name === null ? null : undefined,
    attributes: v.attributes,
  };
}

function validateJsx(node: MdxJsxNode) {
  const name = node.name ?? "";
  if (!ALLOWED_COMPONENTS.has(name)) {
    throw new Error(`MDX: component <${name || "(anonymous)"}> is not allowed`);
  }

  const allowed = ALLOWED_ATTRS[name] ?? new Set<string>();
  const attrs = (node.attributes ?? []) as MdxJsxAttribute[];
  for (const attr of attrs) {
    if (!attr || attr.type !== "mdxJsxAttribute") {
      throw new Error("MDX: unsupported attribute syntax");
    }

    if (attr.name.toLowerCase().startsWith("on")) {
      throw new Error("MDX: event handler attributes are not allowed");
    }

    if (!allowed.has(attr.name)) {
      throw new Error(`MDX: attribute '${attr.name}' not allowed on <${name}>`);
    }

    // Disallow any expression values.
    const v = (attr as { value?: unknown }).value;
    if (v && typeof v === "object" && "type" in (v as Record<string, unknown>)) {
      const vt = (v as Record<string, unknown>).type;
      if (typeof vt === "string" && vt.includes("Expression")) {
        throw new Error("MDX: attribute value expressions are not allowed");
      }
    }
    if (v && typeof v === "object" && "value" in (v as Record<string, unknown>)) {
      const vt = (v as Record<string, unknown>).type;
      if (typeof vt === "string" && vt.includes("Expression")) {
        throw new Error("MDX: attribute value expressions are not allowed");
      }
    }
    if (v && typeof v === "object" && "data" in (v as Record<string, unknown>)) {
      const vt = (v as Record<string, unknown>).type;
      if (typeof vt === "string" && vt.includes("Expression")) {
        throw new Error("MDX: attribute value expressions are not allowed");
      }
    }
    if (v && typeof v === "object" && "expression" in (v as Record<string, unknown>)) {
      throw new Error("MDX: attribute value expressions are not allowed");
    }
  }
}

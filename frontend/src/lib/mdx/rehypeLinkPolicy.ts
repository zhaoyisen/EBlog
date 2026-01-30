function isAllowedUrl(url: string) {
  const v = url.trim().toLowerCase();
  if (v.startsWith("javascript:")) return false;
  if (v.startsWith("data:")) return false;
  if (v.startsWith("vbscript:")) return false;
  // allow relative, hash, mailto, http(s)
  if (v.startsWith("http://") || v.startsWith("https://") || v.startsWith("mailto:")) return true;
  if (v.startsWith("/") || v.startsWith("#")) return true;
  return false;
}

export function rehypeLinkPolicy() {
  return function transformer(tree: unknown) {
    walk(tree, (node) => {
      if (node.type !== "element") return;
      const el = node as HastElement;
      const tag = el.tagName;
      const props = (el.properties ??= {});

      if (tag === "a") {
        const href = typeof props.href === "string" ? props.href : "";
        if (href && !isAllowedUrl(href)) {
          delete props.href;
        }
        props.rel = "nofollow ugc noopener noreferrer";
      }

      if (tag === "img") {
        const src = typeof props.src === "string" ? props.src : "";
        if (src && !(src.startsWith("http://") || src.startsWith("https://") || src.startsWith("/"))) {
          delete props.src;
        }
      }
    });
  };
}

type HastElement = {
  type: "element";
  tagName: string;
  properties?: Record<string, unknown>;
  children?: unknown;
};

type HastNode = HastElement | { type: string; children?: unknown };

function isNode(value: unknown): value is HastNode {
  return !!value && typeof value === "object" && "type" in (value as Record<string, unknown>);
}

function walk(node: unknown, fn: (node: HastNode) => void) {
  if (Array.isArray(node)) {
    for (const item of node) {
      walk(item, fn);
    }
    return;
  }

  if (!isNode(node)) {
    return;
  }
  fn(node);

  const maybeChildren = (node as Record<string, unknown>).children;
  if (maybeChildren) {
    walk(maybeChildren, fn);
  }
}

import { describe, expect, it } from "vitest";
import { validateMdxOrThrow } from "../lib/mdx/renderMdx";

describe("mdx safety", () => {
  it("rejects import/export", async () => {
    await expect(validateMdxOrThrow("import x from 'y'\n\n# hi")).rejects.toThrow();
    await expect(validateMdxOrThrow("export const a = 1\n\n# hi")).rejects.toThrow();
  });

  it("rejects expressions", async () => {
    await expect(validateMdxOrThrow("# hi {1+1}")).rejects.toThrow();
  });

  it("rejects non-whitelisted JSX", async () => {
    await expect(validateMdxOrThrow("<div>no</div>")).rejects.toThrow();
    await expect(validateMdxOrThrow("<Iframe src=\"https://x\" />")).rejects.toThrow();
  });

  it("rejects JSX attribute expressions", async () => {
    await expect(validateMdxOrThrow("<Callout title={1}>x</Callout>")).rejects.toThrow();
    await expect(validateMdxOrThrow("<Callout onClick={alert}>x</Callout>")).rejects.toThrow();
  });

  it("accepts whitelisted components with string attrs", async () => {
    await expect(validateMdxOrThrow("<Callout type=\"note\" title=\"t\">hello</Callout>")).resolves.toBeUndefined();
    await expect(validateMdxOrThrow("<Details summary=\"s\">ok</Details>")).resolves.toBeUndefined();
    await expect(validateMdxOrThrow("<Tabs><Tab label=\"A\">a</Tab></Tabs>")).resolves.toBeUndefined();
  });
});

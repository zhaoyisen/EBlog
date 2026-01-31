import { sanitizeProxyHeaders } from "../lib/http/sanitizeProxyHeaders";

describe("sanitizeProxyHeaders", () => {
  it("removes www-authenticate header", () => {
    const headers = new Headers();
    headers.set("Content-Type", "application/json");
    headers.set("WWW-Authenticate", "Basic realm=\"api\"");

    const result = sanitizeProxyHeaders(headers);

    expect(result.has("www-authenticate")).toBe(false);
    expect(result.get("content-type")).toBe("application/json");
  });
});

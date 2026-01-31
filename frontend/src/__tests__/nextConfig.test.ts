import nextConfig from "../../next.config";

describe("next.config", () => {
  it("does not define global rewrites", () => {
    expect(nextConfig.rewrites).toBeUndefined();
  });
});

import type { NextConfig } from "next";
import { appConfig } from "./src/config/appConfig";

const nextConfig: NextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  async rewrites() {
    const base = appConfig.apiProxyTarget.endsWith("/")
      ? appConfig.apiProxyTarget.slice(0, -1)
      : appConfig.apiProxyTarget;
    return [{ source: "/api/:path*", destination: `${base}/api/:path*` }];
  },
};

export default nextConfig;

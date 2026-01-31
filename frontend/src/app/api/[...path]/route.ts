import { NextRequest, NextResponse } from "next/server";
import { appConfig } from "../../../config/appConfig";
import { sanitizeProxyHeaders } from "../../../lib/http/sanitizeProxyHeaders";

type RouteParams = {
  params: Promise<{ path: string[] }>;
};

async function proxy(request: NextRequest, params: { path: string[] }) {
  const base = appConfig.apiProxyTarget.endsWith("/")
    ? appConfig.apiProxyTarget.slice(0, -1)
    : appConfig.apiProxyTarget;
  const path = params.path.join("/");
  const target = `${base}/api/${path}${request.nextUrl.search}`;

  const headers = new Headers(request.headers);
  headers.delete("host");

  const init: RequestInit = {
    method: request.method,
    headers,
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.arrayBuffer();
  }

  const response = await fetch(target, init);

  const nextRes = new NextResponse(await response.arrayBuffer(), {
    status: response.status,
  });

  // 需要特别处理 set-cookie：多条 cookie 不能被合并为单一 header。
  const sanitizedHeaders = sanitizeProxyHeaders(response.headers);
  sanitizedHeaders.forEach((value, key) => {
    nextRes.headers.set(key, value);
  });

  const getSetCookie = (response.headers as unknown as { getSetCookie?: () => string[] }).getSetCookie;
  const setCookies = getSetCookie ? getSetCookie.call(response.headers) : [];
  if (setCookies.length > 0) {
    for (const cookie of setCookies) {
      nextRes.headers.append("set-cookie", cookie);
    }
  } else {
    const single = response.headers.get("set-cookie");
    if (single) {
      nextRes.headers.set("set-cookie", single);
    }
  }

  return nextRes;
}

export async function GET(request: NextRequest, { params }: RouteParams) {
  return proxy(request, await params);
}

export async function POST(request: NextRequest, { params }: RouteParams) {
  return proxy(request, await params);
}

export async function PUT(request: NextRequest, { params }: RouteParams) {
  return proxy(request, await params);
}

export async function PATCH(request: NextRequest, { params }: RouteParams) {
  return proxy(request, await params);
}

export async function DELETE(request: NextRequest, { params }: RouteParams) {
  return proxy(request, await params);
}

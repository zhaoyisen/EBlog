import { NextRequest, NextResponse } from "next/server";
import { appConfig } from "../../../config/appConfig";

type RouteParams = {
  params: { path: string[] };
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
  const responseHeaders = new Headers(response.headers);

  return new NextResponse(await response.arrayBuffer(), {
    status: response.status,
    headers: responseHeaders,
  });
}

export async function GET(request: NextRequest, { params }: RouteParams) {
  return proxy(request, params);
}

export async function POST(request: NextRequest, { params }: RouteParams) {
  return proxy(request, params);
}

export async function PUT(request: NextRequest, { params }: RouteParams) {
  return proxy(request, params);
}

export async function PATCH(request: NextRequest, { params }: RouteParams) {
  return proxy(request, params);
}

export async function DELETE(request: NextRequest, { params }: RouteParams) {
  return proxy(request, params);
}

export function sanitizeProxyHeaders(headers: Headers): Headers {
  const nextHeaders = new Headers();

  headers.forEach((value, key) => {
    const lowerKey = key.toLowerCase();
    if (lowerKey === "set-cookie" || lowerKey === "www-authenticate") {
      return;
    }
    nextHeaders.set(key, value);
  });

  return nextHeaders;
}

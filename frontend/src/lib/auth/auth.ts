export function getCookie(name: string): string | null {
  if (typeof document === "undefined") {
    return null;
  }

  const prefix = `${encodeURIComponent(name)}=`;
  const parts = document.cookie.split(";");
  for (const part of parts) {
    const trimmed = part.trim();
    if (trimmed.startsWith(prefix)) {
      return decodeURIComponent(trimmed.slice(prefix.length));
    }
  }
  return null;
}

export function getCsrfToken(): string | null {
  // Spring Security CookieCsrfTokenRepository 默认 cookie 名。
  return getCookie("XSRF-TOKEN");
}

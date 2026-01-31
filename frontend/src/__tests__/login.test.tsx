import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import { AuthProvider } from "../lib/auth/AuthProvider";
import LoginPage from "../app/login/page";

describe("LoginPage", () => {
  it("does not store access token in localStorage", async () => {
    const fetchMock = vi.fn().mockImplementation((input: RequestInfo | URL) => {
      const url = String(input);
      if (url.includes("/api/v1/auth/login")) {
        return Promise.resolve({
          ok: true,
          status: 200,
          headers: new Headers(),
          json: async () => ({ success: true, data: { accessToken: "token-123" } }),
        });
      }
      // 其他请求（例如初始化 health/refresh）不影响本测试。
      return Promise.resolve({
        ok: false,
        status: 401,
        headers: new Headers(),
        json: async () => ({ success: false }),
      });
    });
    globalThis.fetch = fetchMock as unknown as typeof fetch;

    const storage = new Map<string, string>();
    Object.defineProperty(window, "localStorage", {
      value: {
        getItem: (key: string) => storage.get(key) ?? null,
        setItem: (key: string, value: string) => storage.set(key, value),
        removeItem: (key: string) => storage.delete(key),
        clear: () => storage.clear(),
      },
      writable: true,
    });

    const setItemSpy = vi.spyOn(window.localStorage, "setItem");

    Object.defineProperty(window, "location", {
      value: { href: "" },
      writable: true,
    });

    const user = userEvent.setup();
    render(
      <AuthProvider>
        <LoginPage />
      </AuthProvider>
    );

    await user.type(screen.getByPlaceholderText("your@email.com"), "test@example.com");
    await user.type(screen.getByPlaceholderText("your password"), "Password123");
    await user.click(screen.getByRole("button", { name: "登录" }));

    await waitFor(() => {
      expect(setItemSpy).not.toHaveBeenCalled();
    });
  });
});

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import LoginPage from "../app/login/page";

describe("LoginPage", () => {
  it("stores access token on successful login", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      json: async () => ({ success: true, data: { accessToken: "token-123" } }),
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

    Object.defineProperty(window, "location", {
      value: { href: "" },
      writable: true,
    });

    const user = userEvent.setup();
    render(<LoginPage />);

    await user.type(screen.getByPlaceholderText("your@email.com"), "test@example.com");
    await user.type(screen.getByPlaceholderText("your password"), "Password123");
    await user.click(screen.getByRole("button", { name: "登录" }));

    await waitFor(() => {
      expect(localStorage.getItem("access_token")).toBe("token-123");
    });
  });
});

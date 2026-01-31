import React from "react";
import { render, waitFor } from "@testing-library/react";
import { vi } from "vitest";
import AdminPage from "../app/admin/page";

vi.mock("../lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    accessToken: null,
    ready: true,
  }),
}));

describe("AdminPage", () => {
  it("redirects to login when unauthenticated", async () => {
    Object.defineProperty(window, "location", {
      value: { href: "" },
      writable: true,
    });

    render(<AdminPage />);

    await waitFor(() => {
      expect(window.location.href).toBe("/login");
    });
  });
});

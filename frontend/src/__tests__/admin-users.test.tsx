import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import UsersPage from "../app/admin/users/page";

const apiFetch = vi.fn();

vi.mock("../lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    accessToken: "token",
    ready: true,
    apiFetch,
  }),
}));

describe("Admin Users Page", () => {
  it("posts ban request with reason", async () => {
    apiFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          success: true,
          data: [
            {
              userId: 1,
              nickname: "Alice",
              role: "USER",
              isBanned: false,
              bannedReason: null,
              createdAt: "2025-01-01T00:00:00Z",
            },
          ],
        }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true }),
      });

    const user = userEvent.setup();
    render(<UsersPage />);

    await waitFor(() => {
      expect(screen.getByText("Alice")).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText("封禁原因"), "spam");
    await user.click(screen.getByRole("button", { name: "封禁" }));

    await waitFor(() => {
      expect(apiFetch).toHaveBeenCalledWith(
        "/api/v1/admin/users/ban/1",
        expect.objectContaining({
          method: "POST",
        })
      );
    });

    const [, requestInit] = apiFetch.mock.calls[1];
    expect(requestInit?.body).toContain("spam");
  });
});

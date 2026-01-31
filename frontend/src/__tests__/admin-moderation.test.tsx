import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import ModerationPage from "../app/admin/moderation/page";

const apiFetch = vi.fn();

vi.mock("../lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    accessToken: "token",
    ready: true,
    apiFetch,
  }),
}));

describe("Admin Moderation Page", () => {
  it("approves post with reason", async () => {
    apiFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          success: true,
          data: [
            {
              postId: 99,
              title: "Hello",
              authorId: 2,
              moderationStatus: "NEEDS_REVIEW",
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
    render(<ModerationPage />);

    await waitFor(() => {
      expect(screen.getByText("Hello")).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText("审核原因"), "ok");
    await user.click(screen.getByRole("button", { name: "通过" }));

    await waitFor(() => {
      expect(apiFetch).toHaveBeenCalledWith(
        "/api/v1/admin/moderation/approve/99",
        expect.objectContaining({ method: "POST" })
      );
    });

    const approveCall = apiFetch.mock.calls.find((call) => call[0] === "/api/v1/admin/moderation/approve/99");
    expect(approveCall).toBeTruthy();
    const requestInit = approveCall?.[1];
    expect(requestInit?.body).toContain("ok");
  });
});

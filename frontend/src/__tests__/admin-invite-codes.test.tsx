import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi } from "vitest";
import InviteCodesPage from "../app/admin/invite-codes/page";

const apiFetch = vi.fn();

vi.mock("../lib/auth/AuthProvider", () => ({
  useAuth: () => ({
    accessToken: "token",
    ready: true,
    apiFetch,
  }),
}));

describe("Admin Invite Codes Page", () => {
  it("creates invite codes with count and maxUses", async () => {
    apiFetch
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: [] }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: { codes: ["abc"] } }),
      });

    const user = userEvent.setup();
    render(<InviteCodesPage />);

    await user.clear(screen.getByLabelText("生成数量"));
    await user.type(screen.getByLabelText("生成数量"), "2");
    await user.clear(screen.getByLabelText("最大使用次数"));
    await user.type(screen.getByLabelText("最大使用次数"), "3");
    await user.click(screen.getByRole("button", { name: "批量生成" }));

    await waitFor(() => {
      expect(apiFetch).toHaveBeenCalledWith(
        "/api/v1/admin/invite-codes/batch-create",
        expect.objectContaining({ method: "POST" })
      );
    });

    const [, requestInit] = apiFetch.mock.calls[1];
    expect(requestInit?.body).toContain("\"count\":2");
    expect(requestInit?.body).toContain("\"maxUses\":3");
  });
});

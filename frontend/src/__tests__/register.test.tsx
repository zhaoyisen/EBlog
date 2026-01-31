import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RegisterPage from "../app/register/page";

describe("RegisterPage", () => {
  it("shows error when required fields are missing", async () => {
    const user = userEvent.setup();
    render(<RegisterPage />);

    await user.click(screen.getByRole("button", { name: "注册" }));

    expect(screen.getByText("请填写完整信息")).toBeInTheDocument();
  });
});

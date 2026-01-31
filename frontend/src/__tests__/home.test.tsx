import React from "react";
import { render, screen } from "@testing-library/react";
import { vi } from "vitest";
import Home from "../app/page";

const useAuthMock = vi.fn();

vi.mock("../lib/auth/AuthProvider", () => ({
  useAuth: () => useAuthMock(),
}));

describe("Home", () => {
  it("renders Chinese title", () => {
    useAuthMock.mockReturnValue({ accessToken: null, ready: true, logout: vi.fn() });
    render(<Home />);
    expect(screen.getByRole("heading", { name: "EBlog" })).toBeInTheDocument();
  });

  it("shows profile entry when logged in", () => {
    useAuthMock.mockReturnValue({ accessToken: "token", ready: true, logout: vi.fn() });
    render(<Home />);
    expect(screen.getByRole("link", { name: "个人中心" })).toBeInTheDocument();
  });
});

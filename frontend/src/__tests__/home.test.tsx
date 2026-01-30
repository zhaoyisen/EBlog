import React from "react";
import { render, screen } from "@testing-library/react";
import Home from "../app/page";

describe("Home", () => {
  it("renders Chinese title", () => {
    render(<Home />);
    expect(screen.getByRole("heading", { name: "EBlog" })).toBeInTheDocument();
  });
});

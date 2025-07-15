import { render, screen } from "@testing-library/react";
import LanguageIcon from "./LanguageIcon";
import { userStore } from "../stores/userStore";

// Mock Zustand
jest.mock("../stores/userStore", () => ({
  userStore: jest.fn(),
}));

describe("LanguageIcon", () => {
  beforeEach(() => {
    userStore.mockReturnValue({
      locale: "pt",
      setLanguage: jest.fn(),
    });
  });

  test("renderiza o botão com o ícone de idioma", () => {
    render(<LanguageIcon />);
    const button = screen.getByRole("button", { name: /selecionar idioma/i });
    expect(button).toBeInTheDocument();
  });
});

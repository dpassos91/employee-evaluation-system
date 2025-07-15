import { render, screen, fireEvent } from "@testing-library/react";
import AvatarCell from "./AvatarCell";

// Mock da API de perfil
jest.mock("../api/profileAPI", () => ({
  profileAPI: {
    getPhoto: (filename) => `/fake-api/photo/${filename}`,
  },
}));

describe("AvatarCell", () => {
  test("renderiza imagem de avatar via profileAPI", () => {
    render(<AvatarCell avatar="foto.jpg" name="Joana" />);
    const img = screen.getByAltText("Joana");
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute("src", "/fake-api/photo/foto.jpg");
  });

  test("usa imagem padrão se avatar não for fornecido", () => {
    render(<AvatarCell avatar="" name="Sem Avatar" />);
    const img = screen.getByAltText("Sem Avatar");
    expect(img).toBeInTheDocument();
    expect(img.src).toContain("profile_icon.png"); // src acaba resolvido como caminho absoluto
  });

  test("fallback para /default_avatar.png ao falhar carregamento", () => {
    render(<AvatarCell avatar="invalido.jpg" name="Erro" />);
    const img = screen.getByAltText("Erro");

    // Simula erro de carregamento da imagem
    fireEvent.error(img);

    expect(img).toHaveAttribute("src", "/default_avatar.png");
  });
});

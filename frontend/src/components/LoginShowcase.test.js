import { render, screen } from "@testing-library/react";
import LoginShowcase from "./LoginShowcase";

describe("LoginShowcase", () => {
  test("renderiza logotipo e slogan", () => {
    render(<LoginShowcase />);
    
    // Verifica se a imagem do logo está presente
    const logoImg = screen.getByAltText(/skillpath logo/i);
    expect(logoImg).toBeInTheDocument();

    // Verifica se o slogan aparece
    expect(screen.getByText(/learn\. evolve\. lead\./i)).toBeInTheDocument();
  });
});

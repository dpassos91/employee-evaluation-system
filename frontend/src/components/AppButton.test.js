import { render, screen, fireEvent } from "@testing-library/react";
import AppButton from "./AppButton";

describe("AppButton", () => {
  test("renderiza com texto e aciona onClick", () => {
    const handleClick = jest.fn();
    render(<AppButton onClick={handleClick}>Clica aqui</AppButton>);

    const button = screen.getByText("Clica aqui");
    expect(button).toBeInTheDocument();

    fireEvent.click(button);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  test("aplica a classe do variant 'primary' por padrão", () => {
    render(<AppButton>Botão</AppButton>);
    const button = screen.getByText("Botão");
    expect(button).toHaveClass("bg-red-600");
  });

  test("aplica a classe do variant 'secondary'", () => {
    render(<AppButton variant="secondary">Secundário</AppButton>);
    const button = screen.getByText("Secundário");
    expect(button).toHaveClass("bg-gray-200");
  });

  test("aceita className adicional", () => {
    render(<AppButton className="text-xl">Grande</AppButton>);
    const button = screen.getByText("Grande");
    expect(button).toHaveClass("text-xl");
  });
});

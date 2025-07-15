import { render, screen, fireEvent } from "@testing-library/react";
import Modal from "./Modal";

describe("Modal component", () => {
  const onCloseMock = jest.fn();

  const defaultProps = {
    isOpen: true,
    onClose: onCloseMock,
    title: "Exemplo de Modal",
    actions: [
      { label: "Cancelar", variant: "secondary", onClick: onCloseMock },
      { label: "Guardar", variant: "primary", onClick: jest.fn() }
    ],
    children: <p>Conteúdo do modal</p>
  };

  test("renderiza corretamente com título e conteúdo", () => {
    render(<Modal {...defaultProps} />);
    expect(screen.getByText("Exemplo de Modal")).toBeInTheDocument();
    expect(screen.getByText("Conteúdo do modal")).toBeInTheDocument();
    expect(screen.getByText("Cancelar")).toBeInTheDocument();
    expect(screen.getByText("Guardar")).toBeInTheDocument();
  });

  test("chama onClose ao clicar no botão de fechar (×)", () => {
    render(<Modal {...defaultProps} />);
    const closeButton = screen.getByLabelText(/close modal/i);
    fireEvent.click(closeButton);
    expect(onCloseMock).toHaveBeenCalled();
  });
});

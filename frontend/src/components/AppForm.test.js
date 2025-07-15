import { render, screen, fireEvent } from "@testing-library/react";
import AppForm from "./AppForm";

jest.mock("./AppButton", () => {
  return {
    __esModule: true,
    default: (props) => <button {...props}>{props.children}</button>,
  };
});

describe("AppForm", () => {
  test("renderiza filhos (inputs ou conteúdo)", () => {
    render(
      <AppForm>
        <label htmlFor="name">Nome</label>
        <input id="name" />
      </AppForm>
    );

    expect(screen.getByLabelText("Nome")).toBeInTheDocument();
  });

  test("chama onSubmit quando o formulário é submetido", () => {
    const onSubmit = jest.fn();
    render(
      <AppForm onSubmit={onSubmit}>
        <input />
        <button type="submit">Submeter</button>
      </AppForm>
    );

    fireEvent.click(screen.getByText("Submeter"));
    expect(onSubmit).toHaveBeenCalled();
  });

  test("não chama onSubmit se isLoading for true", () => {
    const onSubmit = jest.fn();
    render(
      <AppForm onSubmit={onSubmit} isLoading={true}>
        <input />
        <button type="submit">Submeter</button>
      </AppForm>
    );

    fireEvent.click(screen.getByText("Submeter"));
    expect(onSubmit).not.toHaveBeenCalled();
  });

  test("exibe mensagem de erro e sucesso", () => {
    render(
      <AppForm error="Erro global" success="Sucesso!">
        <div>Conteúdo</div>
      </AppForm>
    );

    expect(screen.getByText("Erro global")).toBeInTheDocument();
    expect(screen.getByText("Sucesso!")).toBeInTheDocument();
  });

  test("renderiza botões de ação", () => {
    render(
      <AppForm
        actions={[
          { label: "Guardar", onClick: jest.fn(), variant: "primary" },
          { label: "Cancelar", onClick: jest.fn(), variant: "secondary" },
        ]}
      >
        <div>Conteúdo</div>
      </AppForm>
    );

    expect(screen.getByText("Guardar")).toBeInTheDocument();
    expect(screen.getByText("Cancelar")).toBeInTheDocument();
  });
});

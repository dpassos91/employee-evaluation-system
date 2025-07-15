import { render, screen, fireEvent } from "@testing-library/react";
import { AppTableFilters } from "./AppTableFilters";

describe("AppTableFilters", () => {
  test("renderiza um input com placeholder", () => {
    render(
      <AppTableFilters
        filters={[
          {
            type: "input",
            value: "abc",
            onChange: jest.fn(),
            placeholder: "Pesquisar...",
          },
        ]}
      />
    );
    expect(screen.getByPlaceholderText("Pesquisar...")).toBeInTheDocument();
  });

  test("renderiza um select com opções", () => {
    render(
      <AppTableFilters
        filters={[
          {
            type: "select",
            value: "opt1",
            onChange: jest.fn(),
            options: [
              { value: "opt1", label: "Opção 1" },
              { value: "opt2", label: "Opção 2" },
            ],
          },
        ]}
      />
    );
    expect(screen.getByDisplayValue("Opção 1")).toBeInTheDocument();
    expect(screen.getByText("Opção 2")).toBeInTheDocument();
  });

  test("renderiza um filtro customizado", () => {
    render(
      <AppTableFilters
        filters={[
          {
            type: "custom",
            render: () => <span>Filtro Personalizado</span>,
          },
        ]}
      />
    );
    expect(screen.getByText("Filtro Personalizado")).toBeInTheDocument();
  });

  test("renderiza ações se fornecidas", () => {
    render(
      <AppTableFilters
        filters={[]}
        actions={<button>Exportar</button>}
      />
    );
    expect(screen.getByText("Exportar")).toBeInTheDocument();
  });

  test("dispara onChange do input", () => {
    const onChange = jest.fn();
    render(
      <AppTableFilters
        filters={[
          {
            type: "input",
            value: "",
            onChange,
            placeholder: "Buscar",
          },
        ]}
      />
    );
    fireEvent.change(screen.getByPlaceholderText("Buscar"), {
      target: { value: "test" },
    });
    expect(onChange).toHaveBeenCalled();
  });
});

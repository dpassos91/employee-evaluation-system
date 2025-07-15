import { render, screen } from "@testing-library/react";
import { AppTable } from "./AppTable";

describe("AppTable", () => {
  const columns = [
    { header: "Nome", accessor: "name" },
    { header: "Idade", accessor: "age" },
  ];

  const data = [
    { id: 1, name: "Maria", age: 25 },
    { id: 2, name: "João", age: 30 },
  ];

  test("renderiza o cabeçalho corretamente", () => {
    render(<AppTable columns={columns} data={data} loading={false} emptyMessage="Nenhum dado" />);
    expect(screen.getByText("Nome")).toBeInTheDocument();
    expect(screen.getByText("Idade")).toBeInTheDocument();
  });

  test("renderiza as linhas de dados corretamente", () => {
    render(<AppTable columns={columns} data={data} loading={false} emptyMessage="Nenhum dado" />);
    expect(screen.getByText("Maria")).toBeInTheDocument();
    expect(screen.getByText("25")).toBeInTheDocument();
    expect(screen.getByText("João")).toBeInTheDocument();
    expect(screen.getByText("30")).toBeInTheDocument();
  });

  test("mostra mensagem de carregamento se loading for true", () => {
    render(<AppTable columns={columns} data={[]} loading={true} emptyMessage="Nenhum dado" />);
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  test("mostra mensagem vazia se não houver dados", () => {
    render(<AppTable columns={columns} data={[]} loading={false} emptyMessage="Nenhum dado" />);
    expect(screen.getByText("Nenhum dado")).toBeInTheDocument();
  });

  test("utiliza função render personalizada se definida", () => {
    const customColumns = [
      {
        header: "Nome em maiúsculas",
        render: (row) => row.name.toUpperCase(),
      },
    ];
    render(<AppTable columns={customColumns} data={[{ name: "ana" }]} loading={false} emptyMessage="-" />);
    expect(screen.getByText("ANA")).toBeInTheDocument();
  });
});

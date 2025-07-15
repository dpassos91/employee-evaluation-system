import { render, screen, fireEvent } from "@testing-library/react";
import InputField from "./InputField";
import { IntlProvider } from "react-intl";

describe("InputField", () => {
  const renderWithIntl = (ui) =>
    render(<IntlProvider locale="pt">{ui}</IntlProvider>);

  test("renderiza com o label correto e valor inicial", () => {
    renderWithIntl(
      <InputField
        id="input.label"
        defaultLabel="Nome"
        name="nome"
        value="David"
        onChange={() => {}}
      />
    );

    expect(screen.getByLabelText("Nome")).toBeInTheDocument();
    expect(screen.getByDisplayValue("David")).toBeInTheDocument();
  });

  test("chama onChange ao escrever no campo", () => {
    const mockChange = jest.fn();
    renderWithIntl(
      <InputField
        id="input.email"
        defaultLabel="Email"
        name="email"
        value=""
        onChange={mockChange}
      />
    );

    const input = screen.getByLabelText("Email");
    fireEvent.change(input, { target: { value: "teste@example.com" } });
    expect(mockChange).toHaveBeenCalledTimes(1);
  });
});

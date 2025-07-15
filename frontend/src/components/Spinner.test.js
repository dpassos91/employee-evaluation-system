import React from "react";
import { render, screen } from "@testing-library/react";
import Spinner from "./Spinner";
import { IntlProvider } from "react-intl";

const messages = {
  "app.loading": "Loading..."
};

function renderWithIntl(ui) {
  return render(
    <IntlProvider locale="en" messages={messages}>
      {ui}
    </IntlProvider>
  );
}

describe("Spinner component", () => {
  test("renders with default props", () => {
    renderWithIntl(<Spinner />);

    // Verifica se o logo está presente
    const logo = screen.getByAltText(/logo/i);
    expect(logo).toBeInTheDocument();

    // Verifica se o texto padrão aparece
    expect(screen.getByText(/loading.../i)).toBeInTheDocument();

    // Verifica minHeight do container
    const container = screen.getByRole("status");
    expect(container).toHaveStyle({ minHeight: "50vh" });

    // Verifica a duração da animação no logo
    expect(logo).toHaveStyle({ animationDuration: "2.2s" });
  });

  test("renders custom message and without logo", () => {
    renderWithIntl(
      <Spinner
        messageId="custom.message"
        messageDefault="Custom Loading..."
        minHeight="100px"
        spinDuration="1s"
        showLogo={false}
      />
    );

    // Logo não deve estar no documento
    const logos = screen.queryAllByAltText(/logo/i);
    expect(logos).toHaveLength(0);

    // Texto customizado
    expect(screen.getByText(/custom loading.../i)).toBeInTheDocument();

    const container = screen.getByRole("status");
    expect(container).toHaveStyle({ minHeight: "100px" });
  });
});

import { render, screen } from "@testing-library/react";
import AuthFormLayout from "./AuthFormLayout";
import { IntlProvider } from "react-intl";

describe("AuthFormLayout", () => {
  const renderWithIntl = (ui) =>
    render(<IntlProvider locale="en">{ui}</IntlProvider>);

  test("renderiza o título com o ID e defaultMessage", () => {
    renderWithIntl(
      <AuthFormLayout titleId="auth.title" defaultTitle="Bem-vindo!">
        <div>Conteúdo do formulário</div>
      </AuthFormLayout>
    );

    expect(screen.getByText("Bem-vindo!")).toBeInTheDocument();
    expect(screen.getByText("Conteúdo do formulário")).toBeInTheDocument();
  });

  test("renderiza logo e slogan em dispositivos pequenos", () => {
    renderWithIntl(
      <AuthFormLayout titleId="titulo" defaultTitle="Acesso">
        <div>Entrar</div>
      </AuthFormLayout>
    );

    const logo = screen.getByAltText("SkillPath logo");
    expect(logo).toBeInTheDocument();

    expect(screen.getByText(/Learn\. Evolve\. Lead\./i)).toBeInTheDocument();
  });

  test("renderiza extraLinks se fornecido", () => {
    renderWithIntl(
      <AuthFormLayout
        titleId="x"
        defaultTitle="Formulário"
        extraLinks={<a href="/registar">Criar conta</a>}
      >
        <div>Form</div>
      </AuthFormLayout>
    );

    expect(screen.getByText("Criar conta")).toBeInTheDocument();
  });
});

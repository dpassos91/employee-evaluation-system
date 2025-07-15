import { render, screen } from "@testing-library/react";
import PageLayout from "./PageLayout";

// ⚠️ IMPORTANTE: mock vem antes do import do componente
jest.mock("../stores/userStore", () => ({
  userStore: () => ({ user: { id: 1, name: "Fake User" } }),
}));

jest.mock("./Sidebar", () => () => <div data-testid="mock-sidebar" />);
jest.mock("../components/NotificationIcon", () => () => <div data-testid="mock-notification" />);
jest.mock("../components/MessageIcon", () => () => <div data-testid="mock-message" />);
jest.mock("../components/LanguageIcon", () => () => <div data-testid="mock-language" />);
jest.mock("../components/PageFadeIn", () => ({ children }) => <div>{children}</div>);
jest.mock("../hooks/useSessionMonitor", () => ({
  useSessionMonitor: jest.fn(),
}));

describe("PageLayout", () => {
  test("renderiza o título e conteúdo fornecido", () => {
    render(
      <PageLayout title="Página de Teste" subtitle="Subtítulo">
        <p>Conteúdo principal</p>
      </PageLayout>
    );

    expect(screen.getByText("Página de Teste")).toBeInTheDocument();
    expect(screen.getByText("Subtítulo")).toBeInTheDocument();
    expect(screen.getByText("Conteúdo principal")).toBeInTheDocument();
    expect(screen.getByTestId("mock-sidebar")).toBeInTheDocument();
    expect(screen.getByTestId("mock-notification")).toBeInTheDocument();
    expect(screen.getByTestId("mock-message")).toBeInTheDocument();
    expect(screen.getByTestId("mock-language")).toBeInTheDocument();
  });
});

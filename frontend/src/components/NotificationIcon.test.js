import { render, screen } from "@testing-library/react";
import NotificationIcon from "./NotificationIcon";

// Ignora toda a lógica de Zustand — assume badge como 0
jest.mock("../stores/notificationStore", () => ({
  useNotificationStore: () => ({
    counts: {},
    fetchCounts: jest.fn(),
    fetchNonMessageNotifications: jest.fn(),
    notifications: [],
  }),
}));

describe("NotificationIcon (simples)", () => {
  test("renderiza o botão de notificações", () => {
    render(<NotificationIcon />);
    const button = screen.getByRole("button", { name: /notifications/i });
    expect(button).toBeInTheDocument();
  });
});

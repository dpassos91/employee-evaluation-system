import { useNavigate } from "react-router-dom";
import { authAPI } from "../api/authAPI";
import { userStore } from "../stores/userStore";
import { useIntl } from "react-intl";
import { toast } from "react-toastify";

/**
 * Custom hook to manage authentication logic (login, logout).
 * Integrates with Zustand userStore, sessionStorage, and react-toastify for notifications.
 *
 * @returns {Object} { login, logout }
 */
export function useAuth() {
  const navigate = useNavigate();
  const { formatMessage } = useIntl();

  // Aceder a métodos do Zustand store se precisares (podes alargar conforme necessário)
  const updateName = userStore((state) => state.updateName);

  const { setUser, clearUser } = userStore.getState();

  /**
   * Attempts to log in a user using provided credentials.
   * On success, stores sessionToken and updates userStore.
   * Shows a toast notification and redirects to dashboard.
   *
   * @param {Object} credentials - { email, password }
   * @returns {boolean} true if login successful, false otherwise
   */
  const login = async (credentials) => {
    try {
      // Chama a API e obtém a resposta do backend
      const data = await authAPI.loginUser(credentials);
      // Guarda o token
      sessionStorage.setItem("authToken", data.sessionToken);

      // Guarda o user autenticado no userStore (com role, nomes, etc.)
      setUser({
        id: data.id,
        email: data.email,
        role: data.role,
        primeiroNome: data.primeiroNome,
        ultimoNome: data.ultimoNome,
      });

      toast.success(formatMessage({
        id: "auth.login.success",
        defaultMessage: "Login efetuado com sucesso!"
      }));

      navigate("/dashboard");
      return true;
    } catch (error) {
  // Conta não confirmada (status 403 + mensagem personalizada)
  if (error.status === 403 && error.message.includes("not yet been confirmed")) {
    toast.info(formatMessage({
      id: 'auth.login.unconfirmed',
      defaultMessage: 'A sua conta ainda não foi confirmada! Por favor, verifique o seu email.'
    }));
  } else {
    toast.error(formatMessage({
      id: 'auth.login.failed',
      defaultMessage: 'Login falhou! Por favor verifique as suas credenciais.'
    }));
  }
      return false;
    }
  };

  /**
   * Logs out the user: invalidates session server-side and clears storage/state.
   * Shows a toast notification on success or error.
   */
const logout = async () => {
  try {
    await authAPI.logoutUser();
    toast.success(
  formatMessage({
    id: "auth.logout.success",
    defaultMessage: "Logout efetuado com sucesso!"
  }),
  {
    onClose: () => navigate("/login"),
    autoClose: 2000 // ou o tempo que quiseres, em ms
  }
);
  } catch (error) {
    if (error?.status && [401, 403].includes(error.status)) {
      // Sessão já inválida, continua normalmente
    } else {
      toast.error(formatMessage({
        id: "auth.logout.failed",
        defaultMessage: "Houve um problema ao fazer logout. Por favor tente novamente."
      }));
    }
  } finally {
    userStore.setState({ username: "" });
    sessionStorage.removeItem("authToken");
    localStorage.removeItem("userData");
    sessionStorage.removeItem("user-store");
  }
};

  return { login, logout };
}


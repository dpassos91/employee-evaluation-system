import { useNavigate } from "react-router-dom";
import { authAPI } from "../api/authAPI";
import { userStore } from "../stores/userStore";
import { useIntl } from "react-intl";

/**
 * Custom hook to manage authentication logic (login, logout).
 * Integrates with Zustand userStore and sessionStorage.
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

      alert(formatMessage({
        id: "auth.login.success",
        defaultMessage: "Login efetuado com sucesso!"
      }));

      navigate("/dashboard");
      return true;
    } catch (error) {
      alert(formatMessage({
        id: 'auth.login.failed',
        defaultMessage: 'Login falhou! Por favor verifique as suas credenciais.'
      }));
      return false;
    }
  };


  /**
   * Logs out the user: invalidates session server-side and clears storage/state.
   */
  const logout = async () => {
    try {
      await authAPI.logoutUser();

      // Limpa dados no store e no sessionStorage/localStorage
      userStore.setState({ username: "" });
      sessionStorage.removeItem("authToken");
      localStorage.removeItem("userData");
      sessionStorage.removeItem("user-store");

      alert(formatMessage({
        id: "auth.logout.success",
        defaultMessage: "Logout efetuado com sucesso!"
      }));

      navigate("/login");
    } catch (error) {
      alert(formatMessage({
        id: "auth.logout.failed",
        defaultMessage: "Houve um problema ao fazer logout. Por favor tente novamente."
      }));
    }
  };

  return { login, logout };
}

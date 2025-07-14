import { useEffect } from "react";
import { sessionAPI } from "../api/sessionAPI";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

/**
 * Custom hook to periodically validate the user's session token.
 * If the token is expired or invalid, logs out the user and redirects to login.
 *
 * @param {boolean} enabled - Whether to activate session monitoring (default: true).
 * @param {number} intervalMs - Interval in milliseconds to check session validity (default: 30s).
 */
export const useSessionMonitor = (enabled = true, intervalMs = 30000) => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!enabled) return;

    const intervalId = setInterval(async () => {
      try {
        await sessionAPI.checkSessionStatus(); // Valid session, do nothing
      } catch (error) {
        if (error.status === 401) {
          const token = sessionStorage.getItem("authToken");

          if (token) {
            // Cleanup only if the user is still logged in
            sessionStorage.removeItem("authToken");
            localStorage.removeItem("userData");

            toast.warning("Sessão expirada. Por favor, faça login novamente.");
            navigate("/login");
          }
        } else {
          console.error("Erro ao verificar sessão:", error.message || error);
        }
      }
    }, intervalMs);

    return () => clearInterval(intervalId);
  }, [enabled, intervalMs, navigate]);
};

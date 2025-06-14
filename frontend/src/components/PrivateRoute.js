import { Navigate } from "react-router-dom";
import { userStore } from "../stores/userStore";

export function PrivateRoute({ children }) {
  const { user } = userStore();
  // Se não está autenticado, redireciona para login
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  // Senão, mostra o conteúdo da rota
  return children;
}
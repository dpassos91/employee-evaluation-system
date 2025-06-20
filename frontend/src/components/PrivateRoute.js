import { Navigate, useLocation } from "react-router-dom";
import { userStore } from "../stores/userStore";

export function PrivateRoute({ children }) {
  const { user, profileComplete } = userStore();
  const location = useLocation();

  // Se não está autenticado, redireciona para login
  if (!user || !user.id) {
    return <Navigate to="/login" replace />;
  }

  // Loader opcional: enquanto ainda não se sabe o estado do perfil
  if (profileComplete === null) {
    // Podes trocar por um spinner/loader mais elaborado
    return <div>Loading...</div>;
  }

  // Se perfil está incompleto e não está na rota /profile, força o preenchimento
  const isProfileRoute = location.pathname === "/profile";
  if (profileComplete === false && !isProfileRoute) {
    return <Navigate to="/profile" replace />;
  }

  // Senão, mostra o conteúdo da rota
  return children;
}

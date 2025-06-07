import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";

import NotificationIcon from "./components/NotificationIcon";
import MessageIcon from "./components/MessageIcon";
import LanguageIcon from "./components/LanguageIcon";

import { userStore } from "./stores/userStore";
import LoginPage from "./pages/LoginPage";

export default function App() {
  const user = userStore((state) => state.user);

  return (
    <Router>
      {/* Ícones fixos no topo direito */}
     <div className="fixed top-10 right-[95px] z-50">
  <div className="flex gap-4 items-center">
    {user && (
      <>
        <NotificationIcon />
        <MessageIcon />
      </>
    )}
    <LanguageIcon />
  </div>
</div>

      {/* Rotas da aplicação */}
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        {/* Redirecionamento por omissão para o login (temporário, até haver mais páginas) */}
        <Route path="*" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

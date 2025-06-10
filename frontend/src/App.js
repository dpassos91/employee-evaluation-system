import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { IntlProvider } from "react-intl";

import NotificationIcon from "./components/NotificationIcon";
import MessageIcon from "./components/MessageIcon";
import LanguageIcon from "./components/LanguageIcon";

import { userStore } from "./stores/userStore";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import RedefinePasswordPage from "./pages/RedefinePasswordPage";
import DashboardPage from "./pages/DashboardPage";

export default function App() {
  const {
    user,
    locale,
    translations
  } = userStore();

  return (
    <IntlProvider
      locale={locale}
      messages={translations}
      onError={(err) => {
        if (err.code === "MISSING_TRANSLATION") {
          console.warn("Erro de tradução:", err.message);
        }
      }}
    >
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
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgotpassword" element={<ForgotPasswordPage />} />
          <Route path="/redefinepassword" element={<RedefinePasswordPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          {/* Redireciona para /login se não estiver autenticado */}
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
      </Router>
    </IntlProvider>
  );
}


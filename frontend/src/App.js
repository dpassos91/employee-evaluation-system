import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { IntlProvider } from "react-intl";

import { PrivateRoute } from "./components/PrivateRoute";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import { userStore } from "./stores/userStore";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import RedefinePasswordPage from "./pages/RedefinePasswordPage";
import DashboardPage from "./pages/DashboardPage";
import ProfilePage from "./pages/ProfilePage";
import UsersListPage from "./pages/UsersListPage";

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
        

        {/* Rotas da aplicação */}
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgotpassword" element={<ForgotPasswordPage />} />
          <Route path="/redefinepassword" element={<RedefinePasswordPage />} />
          <Route
    path="/dashboard"
    element={
      <PrivateRoute>
        <DashboardPage />
      </PrivateRoute>
    }
    />
          <Route
    path="/profile"
    element={
      <PrivateRoute>
        <ProfilePage />
      </PrivateRoute>
    }
    />
          <Route
    path="/userslist"
    element={
      <PrivateRoute>
        <UsersListPage />
      </PrivateRoute>
    }
    />
          {/* Redireciona para /login se não estiver autenticado */}
          <Route path="*" element={<Navigate to="/login" />} />
        </Routes>
        <ToastContainer position="top-center" />
      </Router>
    </IntlProvider>
  );
}
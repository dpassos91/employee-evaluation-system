import NotificationIcon from "./components/NotificationIcon";
import MessageIcon from "./components/MessageIcon";
import LanguageIcon from "./components/LanguageIcon";
import { userStore } from "./stores/userStore";
import LoginPage from "./pages/LoginPage";

export default function App() {
  const user = userStore((state) => state.user);

  return (
    <div className="relative min-h-screen bg-secondary">
      {/* Ícones no topo direito */}
      <div className="fixed top-6 right-6 z-50 flex gap-4 items-center">
        {user && (
          <>
            <NotificationIcon />
            <MessageIcon />
          </>
        )}
        <LanguageIcon />
      </div>

      <LoginPage />
    </div>
  );
}

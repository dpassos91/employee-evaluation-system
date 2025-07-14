import Sidebar from "./Sidebar";
import NotificationIcon from "../components/NotificationIcon";
import MessageIcon from "../components/MessageIcon";
import LanguageIcon from "../components/LanguageIcon";
import { userStore } from "../stores/userStore";
import PageFadeIn from "../components/PageFadeIn"; // importa aqui!
import { useSessionMonitor } from "../hooks/useSessionMonitor";

/**
 * PageLayout
 * Layout wrapper for all main app pages.
 * Applies sidebar, top icons, header, and fade-in animation to content.
 *
 * Props:
 * - title: string or ReactNode (page title)
 * - subtitle: string or ReactNode (optional subtitle)
 * - children: ReactNode (page content)
 */
export default function PageLayout({ title, subtitle, children }) {
  const { user } = userStore();


// ⚠️ Ativa o monitor apenas se houver user/token

  const token = sessionStorage.getItem("authToken");

  useSessionMonitor(!!token); // ativa apenas se estiver logado

  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <main className="relative flex-1 p-4 pt-8 transition-all
        lg:ml-64 lg:px-[105px] lg:pt-16
        sm:px-8
      ">
        {/* Icons integrados no topo direito do conteúdo */}
        <div className="
          absolute 
          top-4 right-4
          sm:top-6 sm:right-8
          lg:top-10 lg:right-[95px]
          z-50
        ">
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

        {/* Header comum */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold">{title}</h2>
            {subtitle && <p className="text-gray-600">{subtitle}</p>}
          </div>
        </div>

        {/* Fade-in animado para o conteúdo principal */}
        <PageFadeIn>
          {children}
        </PageFadeIn>
      </main>
    </div>
  );
}




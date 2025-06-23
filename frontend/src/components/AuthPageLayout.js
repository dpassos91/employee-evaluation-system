import LoginShowcase from "./LoginShowcase";
import MediaType from "./media/MediaType";
import { userStore } from "../stores/userStore";
import LanguageIcon from "../components/LanguageIcon";

export default function AuthPageLayout({ children }) {
  const mediatype = userStore((state) => state.mediatype);
  MediaType(); // atualiza tipo de ecrã

  return (
    // 1. Adiciona "relative" ao container principal
    <div className="flex flex-col md:flex-row min-h-screen bg-secondary relative">
      {/* 2. LanguageIcon SEMPRE visível, no topo direito */}
      <div className="absolute 
          top-4 right-4
          sm:top-6 sm:right-8
          lg:top-10 lg:right-[95px]
          z-50">
        <LanguageIcon />
      </div>

      {mediatype.isDesktopOrLaptop && (
        <div className="w-[65%] bg-primary clip-diagonal relative">
          <LoginShowcase />
        </div>
      )}

      <div className="w-full md:w-[50%] flex items-center justify-center px-4 py-8">
        {children}
      </div>
    </div>
  );
}


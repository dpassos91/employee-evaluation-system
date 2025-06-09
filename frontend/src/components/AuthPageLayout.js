import LoginShowcase from "./LoginShowcase";
import MediaType from "./media/MediaType";
import { userStore } from "../stores/userStore";

export default function AuthPageLayout({ children }) {
  const mediatype = userStore((state) => state.mediatype);
  MediaType(); // atualiza tipo de ecrã

  return (
    <div className="flex flex-col md:flex-row min-h-screen bg-secondary">
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

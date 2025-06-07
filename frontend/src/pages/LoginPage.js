import LoginShowcase from "../components/LoginShowcase";
import LoginForm from "../components/LoginForm";
import MediaType from "../components/media/MediaType";
import { userStore } from "../stores/userStore";

export default function LoginPage() {
  const mediatype = userStore((state) => state.mediatype);
  MediaType(); // ativa a atualização do tipo de ecrã

  return (
   <div className="flex flex-col md:flex-row min-h-screen bg-secondary">
  {/* Trapézio (desktop only) */}
  {mediatype.isDesktopOrLaptop && (
    <div className="w-[65%] bg-primary clip-diagonal relative">
      <LoginShowcase />
    </div>
  )}

  {/* Login container */}
  <div className="w-full md:w-[50%] flex items-center justify-center px-4 py-8">
    <LoginForm />
  </div>
</div>
  );
}



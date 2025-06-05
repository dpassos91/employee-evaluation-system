import LoginShowcase from "../components/LoginShowcase";
import LoginForm from "../components/LoginForm";
import MediaType from "../components/media/MediaType";
import { userStore } from "../stores/userStore";

export default function LoginPage() {
  const mediatype = userStore((state) => state.mediatype);
  MediaType(); // ativa a atualização do tipo de ecrã

  return (
    <div className="flex h-screen bg-secondary">
      {/* Trapézio visível apenas em desktop/laptop */}
      {mediatype.isDesktopOrLaptop && (
        <div className="w-[70%] bg-primary clip-diagonal relative">
          <LoginShowcase />
        </div>
      )}

      {/* Formulário ocupa o espaço restante */}
      <div className="w-full md:w-1/2 flex items-center justify-center">
        <LoginForm />
      </div>
    </div>
  );
}


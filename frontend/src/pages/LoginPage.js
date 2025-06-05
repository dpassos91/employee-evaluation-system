import LoginShowcase from "../components/LoginShowcase";
import LoginForm from "../components/LoginForm";
import MediaType from "../components/media/MediaType";
import { userStore } from "../stores/userStore";

export default function LoginPage() {
  const mediatype = userStore((state) => state.mediatype);
  MediaType(); // ativa a atualização do tipo de ecrã

  return (
    <div className="flex h-screen bg-secondary overflow-hidden">
      {/* Trapézio visível apenas em desktop/laptop */}
      {mediatype.isDesktopOrLaptop && (
        <div className="w-[70%] bg-primary clip-diagonal relative">
          <LoginShowcase />
        </div>
      )}

      {/* Formulário ocupa o espaço restante */}
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <LoginForm />
      </div>
    </div>
  );
}


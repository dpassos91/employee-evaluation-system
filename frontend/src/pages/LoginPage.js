import LoginShowcase from "../components/LoginShowcase";
import LoginForm from "../components/LoginForm";

export default function LoginPage() {
  return (
    <div className="flex h-screen bg-secondary">
      <div className="hidden md:block md:w-[70%] bg-primary clip-diagonal relative">
        <LoginShowcase />
      </div>
      <div className="w-full md:w-1/2 flex items-center justify-center">
        <LoginForm />
      </div>
    </div>
  );
}

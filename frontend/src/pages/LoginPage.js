import { useEffect } from "react";
import AuthPageLayout from "../components/AuthPageLayout";
import LoginForm from "../components/forms/LoginForm";
import { toast } from "react-toastify";

export default function LoginPage() {
  useEffect(() => {
    const toastInfo = sessionStorage.getItem("accountConfirmToast");
    if (toastInfo) {
      const { type, message } = JSON.parse(toastInfo);
      if (type === "success") toast.success(message);
      else toast.error(message);
      sessionStorage.removeItem("accountConfirmToast");
    }
  }, []);

  return (
    <AuthPageLayout>
      <LoginForm />
    </AuthPageLayout>
  );
}

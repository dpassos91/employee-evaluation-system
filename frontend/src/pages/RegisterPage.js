import AuthPageLayout from "../components/AuthPageLayout";
import RegisterForm from "../components/forms/RegisterForm";

export default function RegisterPage() {
  return (
    <AuthPageLayout>
      <RegisterForm />
    </AuthPageLayout>
  );
}
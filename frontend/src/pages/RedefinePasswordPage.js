import AuthPageLayout from "../components/AuthPageLayout";
import RedefinePasswordForm from "../components/forms/RedefinePasswordForm";

export default function RecoveryPasswordPage() {
  return (
    <AuthPageLayout>
      <RedefinePasswordForm />
    </AuthPageLayout>
  );
}
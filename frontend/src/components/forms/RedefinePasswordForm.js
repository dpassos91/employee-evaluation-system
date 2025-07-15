import { useState } from "react";
import { useSearchParams, Link, useNavigate } from "react-router-dom";
import { FormattedMessage, useIntl } from "react-intl";
import { toast } from "react-toastify";
import { authAPI } from "../../api/authAPI";
import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";

export default function RedefinePasswordForm() {
  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: ""
  });
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const intl = useIntl();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.newPassword.length < 6) {
      toast.error(intl.formatMessage({ id: "redefinepassword.password.short", defaultMessage: "A password deve ter pelo menos 6 caracteres." }));
      return;
    }
    if (formData.newPassword !== formData.confirmPassword) {
      toast.error(intl.formatMessage({ id: "redefinepassword.password.mismatch", defaultMessage: "As passwords não coincidem." }));
      return;
    }

    const recoveryToken = searchParams.get("recoveryToken");
    if (!recoveryToken) {
      toast.error(intl.formatMessage({ id: "redefinepassword.token.missing", defaultMessage: "Token de recuperação em falta." }));
      return;
    }

    try {
      await authAPI.resetPassword(recoveryToken, formData.newPassword);
      toast.success(intl.formatMessage({ id: "redefinepassword.success", defaultMessage: "Password redefinida com sucesso!" }));
      navigate("/login");
    } catch (error) {
      toast.error(intl.formatMessage({ id: "redefinepassword.error", defaultMessage: "Erro ao redefinir password." }));
    }
  };

  return (
    <AuthFormLayout
      titleId="redefinepassword.title"
      defaultTitle="Redefinir password"
      extraLinks={
        <Link to="/login" className="text-primary hover:underline">
          <FormattedMessage id="goback" defaultMessage="Voltar" />
        </Link>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-left">
        <InputField
          id="password"
          defaultLabel={intl.formatMessage({ id: "redefinepassword.newPasswordLabel", defaultMessage: "Password" })}
          type="password"
          name="newPassword"
          value={formData.newPassword}
          onChange={handleChange}
        />
        <InputField
          id="passwordconfirmation"
          defaultLabel={intl.formatMessage({ id: "redefinepassword.confirmPasswordLabel", defaultMessage: "Confirme a password" })}
          type="password"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
        />
        <SubmitButton
          id="redefinepassword.form.submit"
          defaultLabel={intl.formatMessage({ id: "redefinepassword.submit", defaultMessage: "Redefinir" })}
        />
      </form>
    </AuthFormLayout>
  );
}

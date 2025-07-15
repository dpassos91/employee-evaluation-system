import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link } from "react-router-dom";
import { authAPI } from "../../api/authAPI";
import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";
import { toast } from "react-toastify";

export default function ForgotPasswordForm() {
  const [formData, setFormData] = useState({
    email: ""
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.email) {
      toast.error("Por favor insere um email válido.");
      return;
    }

    setLoading(true);
    try {
      await authAPI.requestPasswordReset(formData.email);
      toast.success("Se o email existir, o link de recuperação será enviado.");
      setFormData({ email: "" });
    } catch (error) {
      toast.error("Erro ao enviar pedido de recuperação.");
      console.error("Erro ao pedir reset de password:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthFormLayout
      titleId="forgotpassword.title"
      defaultTitle="Esqueci-me da password"
      extraLinks={
        <Link to="/login" className="text-primary hover:underline">
          <FormattedMessage id="goback" defaultMessage="Voltar" />
        </Link>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-left">
        <InputField
          id="email"
          defaultLabel="Email"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
          disabled={loading}
        />
        <SubmitButton
          id="forgotpassword.form.submit"
          defaultLabel="Recuperar"
          disabled={loading}
        />
      </form>
    </AuthFormLayout>
  );
}

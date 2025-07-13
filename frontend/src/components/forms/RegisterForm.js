import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link, useNavigate } from "react-router-dom";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";
import { authAPI } from "../../api/authAPI"; // IMPORTANTE

export default function RegisterForm() {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    confirmPassword: ""
  });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // 1. Validação básica
    if (formData.password !== formData.confirmPassword) {
      setError("As passwords não coincidem.");
      return;
    }

    setLoading(true);

    try {
      // 2. Chamar API (só enviar os campos que o backend espera)
      const response = await authAPI.registerUser({
        email: formData.email,
        password: formData.password
      });
      // 3. (Opcional) Redirecionar ou mostrar mensagem de sucesso
      navigate("/login?registered=1");
    } catch (err) {
      setError(
        err.message ||
        "Ocorreu um erro ao registar. Tente novamente."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthFormLayout
      titleId="register.title"
      defaultTitle="Registar"
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
        />
        <InputField
          id="password"
          defaultLabel="Password"
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
        />
        <InputField
          id="passwordconfirmation"
          defaultLabel="Confirme a password"
          type="password"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
        />
        {error && (
          <div className="text-red-500">{error}</div>
        )}
        <SubmitButton
          id="register.form.submit"
          defaultLabel={loading ? "A registar..." : "Registar"}
          disabled={loading}
        />
      </form>
    </AuthFormLayout>
  );
}


import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link } from "react-router-dom";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";

// Importa o custom hook
import { useAuth } from "../../hooks/useAuth"; // <-- ajusta o caminho conforme o teu projeto

export default function LoginForm() {
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [loading, setLoading] = useState(false);

  const { login } = useAuth(); // Hook da autenticação

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const success = await login(formData);
    setLoading(false);

    // (Opcional: feedback ou lógica adicional, caso o login falhe)
    if (!success) {
      // Podes mostrar uma mensagem de erro aqui, se quiseres
    }
  };

  return (
    <AuthFormLayout
      titleId="login.title"
      defaultTitle="Login"
      extraLinks={
        <>
          <Link to="/register" className="text-primary hover:underline">
            <FormattedMessage id="login.form.noAccount" defaultMessage="Não tem conta?" />
          </Link>
          <Link to="/forgotpassword" className="text-primary hover:underline">
            <FormattedMessage id="login.form.forgotPassword" defaultMessage="Esqueceu-se da password?" />
          </Link>
        </>
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
        <SubmitButton
          id="login.form.submit"
          defaultLabel="Login"
          loading={loading}
          disabled={loading}
        />
      </form>
    </AuthFormLayout>
  );
}




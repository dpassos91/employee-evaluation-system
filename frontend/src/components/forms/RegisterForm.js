import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link } from "react-router-dom";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";

export default function RegisterForm() {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    confirmPassword: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Aqui podes incluir lógica de validação e submissão
    console.log("Registo:", formData);
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
        <SubmitButton
          id="register.form.submit"
          defaultLabel="Registar"
        />
      </form>
    </AuthFormLayout>
  );
}

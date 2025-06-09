import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link } from "react-router-dom";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";

export default function LoginForm() {
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Login:", formData);
    // Lógica de autenticação aqui
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
        />
      </form>
    </AuthFormLayout>
  );
}



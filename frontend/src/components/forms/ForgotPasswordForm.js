import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link } from "react-router-dom";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";

export default function ForgotPasswordForm() {
  const [formData, setFormData] = useState({
    email: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Lógica de recuperação de password
    console.log("Recuperar password:", formData);
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
        />
        <SubmitButton
          id="forgotpassword.form.submit"
          defaultLabel="Recuperar"
        />
      </form>
    </AuthFormLayout>
  );
}

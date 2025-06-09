import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link } from "react-router-dom";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";

export default function RedefinePasswordForm() {
  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: ""
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Lógica de redefinição de password
    console.log("Nova password:", formData);
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
          defaultLabel="Password"
          type="password"
          name="newPassword"
          value={formData.newPassword}
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
          id="redefinepassword.form.submit"
          defaultLabel="Redefinir"
        />
      </form>
    </AuthFormLayout>
  );
}

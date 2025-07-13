import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";
import { authAPI } from "../../api/authAPI"; 
import { useIntl } from "react-intl";

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

  const { formatMessage } = useIntl();

const handleSubmit = async (e) => {
  e.preventDefault();
  setError(null);

  if (formData.password !== formData.confirmPassword) {
    setError(
      formatMessage({
        id: "register.error.password_mismatch",
        defaultMessage: "Passwords do not match."
      })
    );
    return;
  }

  setLoading(true);

  try {
    await authAPI.registerUser({
      email: formData.email,
      password: formData.password,
    });

    toast.info(
      formatMessage({
        id: "register.confirmation_required",
        defaultMessage:
          "Registration not complete. Please confirm your account through the email we sent you."
      }),
      { autoClose: 6000 }
    );

    setTimeout(() => {
      navigate("/login");
    }, 2000);

  } catch (err) {
    setError(
      err.message ||
        formatMessage({
          id: "register.error.generic",
          defaultMessage: "An error occurred during registration. Please try again."
        })
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


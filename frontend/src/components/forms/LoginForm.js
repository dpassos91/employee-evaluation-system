/**
 * LoginForm Component
 * 
 * Renders the login form for user authentication.
 * Displays error feedback using a toast notification if login fails.
 * Supports internationalized labels and messages.
 * 
 * Props: none
 * Usage: Used within authentication routes/pages (e.g., /login)
 * 
 * @component
 * @example
 * return (
 *   <LoginForm />
 * )
 */

import { useState } from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";
import { useAuth } from "../../hooks/useAuth";

export default function LoginForm() {
  // State for user input fields (email and password)
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  // State to indicate whether login request is in progress
  const [loading, setLoading] = useState(false);

  // Authentication hook that provides login functionality
  const { login } = useAuth();

  // React-intl hook for translations
  const intl = useIntl();

  /**
   * Handles input changes for both fields.
   * @param {Event} e - The input change event.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  /**
   * Handles form submission and triggers login.
   * Shows an error toast if authentication fails.
   * @param {Event} e - The form submit event.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Attempt login via custom authentication hook
    const success = await login(formData);

    setLoading(false);

    // Show toast notification on failed login
    if (!success) {
      toast.error(
        intl.formatMessage({
          id: "login.error.invalidCredentials",
          defaultMessage: "Invalid email or password."
        })
      );
    }
  };

  return (
    <AuthFormLayout
      titleId="login.title"
      defaultTitle="Login"
      extraLinks={
        <>
          {/* Link to registration page */}
          <Link to="/register" className="text-primary hover:underline">
            <FormattedMessage id="login.form.noAccount" defaultMessage="Don't have an account?" />
          </Link>
          {/* Link to forgot password page */}
          <Link to="/forgotpassword" className="text-primary hover:underline">
            <FormattedMessage id="login.form.forgotPassword" defaultMessage="Forgot your password?" />
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-left">
        {/* Email input field */}
        <InputField
          id="email"
          defaultLabel="Email"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
        />
        {/* Password input field */}
        <InputField
          id="password"
          defaultLabel="Password"
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
        />
        {/* Submit button (shows loading spinner if submitting) */}
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




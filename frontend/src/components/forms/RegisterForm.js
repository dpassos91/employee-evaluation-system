/**
 * RegisterForm Component
 *
 * Handles user registration. Validates input, submits data to backend,
 * and provides user-friendly, internationalized feedback via toasts.
 * Prevents leaking technical errors to the user.
 *
 * @component
 * @example
 * return <RegisterForm />
 */

import { useState } from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

import AuthFormLayout from "../../components/AuthFormLayout";
import InputField from "../../components/InputField";
import SubmitButton from "../../components/SubmitButton";
import { authAPI } from "../../api/authAPI";

export default function RegisterForm() {
  // State for user input fields (email, password, confirmation)
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    confirmPassword: ""
  });

  // State for loading status (to disable submit during API request)
  const [loading, setLoading] = useState(false);

  // React Router hook for navigation after successful registration
  const navigate = useNavigate();

  // Internationalization hook for messages
  const { formatMessage } = useIntl();

  /**
   * Handles input changes for all form fields.
   * @param {Event} e - Input change event.
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  /**
   * Handles form submission: validates fields, calls backend API,
   * and provides toast feedback depending on the result.
   * @param {Event} e - Form submit event.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Frontend validation: password and confirmation must match
    if (formData.password !== formData.confirmPassword) {
      toast.error(
        formatMessage({
          id: "register.error.password_mismatch",
          defaultMessage: "Passwords do not match."
        })
      );
      return;
    }

    setLoading(true);

    try {
      // Call the backend API to register the user
      await authAPI.registerUser({
        email: formData.email,
        password: formData.password,
      });

      // Show informational toast and redirect to login after short delay
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
      // Handle backend error using errorCode, never show technical error messages

      // Try to get the backend response, compatible with both fetch and axios
      const backendError = err?.response?.data || err;

      let message;

      if (
        backendError.errorCode === "EMAIL_ALREADY_EXISTS"
      ) {
        // Show user-friendly toast for existing email
        message = formatMessage({
          id: "register.error.emailInUse",
          defaultMessage: "This email is already registered. Forgot your password?"
        });
      } else {
        // Fallback for all other errors (never show backend details)
        message = formatMessage({
          id: "register.error.generic",
          defaultMessage: "An error occurred during registration. Please try again."
        });
      }

      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthFormLayout
      titleId="register.title"
      defaultTitle="Register"
      extraLinks={
        <Link to="/login" className="text-primary hover:underline">
          <FormattedMessage id="goback" defaultMessage="Back" />
        </Link>
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
        {/* Confirm password input field */}
        <InputField
          id="passwordconfirmation"
          defaultLabel="Confirm password"
          type="password"
          name="confirmPassword"
          value={formData.confirmPassword}
          onChange={handleChange}
        />
        {/* Submit button */}
        <SubmitButton
          id="register.form.submit"
          defaultLabel={loading ? "Registering..." : "Register"}
          disabled={loading}
        />
      </form>
    </AuthFormLayout>
  );
}


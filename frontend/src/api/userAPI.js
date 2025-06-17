import { apiCall } from "./apiConfig";

/**
 * Registers a new user in the system.
 * @param {Object} userData - User registration data (e.g., email, password, username, etc.).
 * @returns {Promise<Object>} The created user data or API response.
 */
const registerUser = async (userData) => {
  return apiCall("/api/users/createUser", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  });
};

/**
 * Confirms a user account using the confirmation token sent by email.
 * @param {string} confirmToken - The confirmation token from the email link.
 * @returns {Promise<Object>} API response indicating success or failure.
 */
const confirmAccount = async (confirmToken) => {
  return apiCall(`/api/users/confirmAccount?confirmToken=${encodeURIComponent(confirmToken)}`, {
    method: "GET"
  });
};

/**
 * Requests a password reset email for the given address.
 * @param {string} email - The email address of the user requesting password reset.
 * @returns {Promise<Object>} API response confirming whether the reset email was sent.
 */
const requestPasswordReset = async (email) => {
  return apiCall("/api/users/request-reset", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
};

/**
 * Resets the user's password using a recovery token.
 * @param {string} recoveryToken - The token received via password recovery email.
 * @param {string} password - The new password to set for the user.
 * @returns {Promise<Object>} API response indicating the result of the password reset.
 */
const resetPassword = async (recoveryToken, password) => {
  return apiCall(`/api/users/reset-password?recoveryToken=${encodeURIComponent(recoveryToken)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ password }),
  });
};

/**
 * Fetches the authenticated user's details using the current session token.
 * Requires the sessionToken to be included in the request header by the auth interceptor.
 * @returns {Promise<Object>} The current user's data.
 */
const getCurrentUser = async () => {
  return apiCall("/api/users/me", {
    method: "GET"
    // sessionToken will be added to headers by the authInterceptor
  });
};

export const userAPI = {
  registerUser,
  confirmAccount,
  requestPasswordReset,
  resetPassword,
  getCurrentUser,
};
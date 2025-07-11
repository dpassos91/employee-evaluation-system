/**
 * Auth API
 * Centralized functions for authentication and user session management.
 * 
 * Usage:
 * import { authAPI } from './authAPI';
 * await authAPI.loginUser({ email, password });
 * await authAPI.logoutUser();
 * await authAPI.requestPasswordReset(email);
 * await authAPI.resetPassword(token, newPassword);
 * await authAPI.validateSession(sessionToken);
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Updates the password for the authenticated user.
 * @param {string} email - The user's email.
 * @param {string} currentPassword - The current password (required for validation).
 * @param {string} newPassword - The new password to set.
 * @param {string} sessionToken - The session token for authorization.
 * @returns {Promise<Object>} The API response.
 */
const changePassword = async (email, currentPassword, newPassword, sessionToken) => {
  return apiCall(`${API_ENDPOINTS.auth.changePassword(email)}`, {
    method: 'PATCH',
    headers: {
      "Content-Type": "application/json",
      sessionToken,
    },
    body: JSON.stringify({
      currentPassword,
      newPassword,
    }),
  });
};

/**
 * Confirms a user account using the confirmation token sent by email.
 * @param {string} confirmToken - The confirmation token from the email link.
 * @returns {Promise<Object>} API response indicating success or failure.
 */
const confirmAccount = async (confirmToken) => {
  return apiCall(
    `${API_ENDPOINTS.auth.confirm(confirmToken)}`,
    { method: "GET" }
  );
};

/**
 * Logs in a user, stores the session token in sessionStorage,
 * and returns the login response with user info and profile status.
 * 
 * @param {Object} credentials - User credentials (e.g., { email, password }).
 * @returns {Promise<Object>} Login response.
 * @throws {Error} If login fails.
 */
const loginUser = async (credentials) => {
  const loginResponse = await apiCall(API_ENDPOINTS.auth.login, {
    method: 'POST',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });

  sessionStorage.setItem('authToken', loginResponse.sessionToken);

  return loginResponse;
};

/**
 * Logs out the authenticated user and removes the session token from sessionStorage.
 * 
 * @returns {Promise<boolean>} True if logout is successful.
 * @throws {Error} If logout fails or token does not exist.
 */
const logoutUser = async () => {
  const token = sessionStorage.getItem('authToken');
  if (!token) {
    throw new Error("Token does not exist in sessionStorage.");
  }
  const result = await apiCall(API_ENDPOINTS.auth.logout, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` }
  });

  if (result && result.message && result.message === "Logout successful!") {
    sessionStorage.removeItem('authToken');
    return true;
  } else {
    throw new Error("Logout failed: " + (result?.message || result));
  }
};

/**
 * Registers a new user in the system.
 * @param {Object} userData - User registration data.
 * @returns {Promise<Object>} The created user data or API response.
 */
const registerUser = async (userData) => {
  return apiCall(API_ENDPOINTS.auth.register, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(userData),
  });
};

/**
 * Requests a password reset for the specified email address.
 * 
 * @param {string} email - The user's email address.
 * @returns {Promise<Object|string>} The API response.
 */
const requestPasswordReset = async (email) => {
  return apiCall(API_ENDPOINTS.auth.requestResetPassword, {
    method: 'POST',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email }),
  });
};

/**
 * Resets the user's password using a recovery token and the new password.
 * 
 * @param {string} token - The password reset (recovery) token.
 * @param {string} newPassword - The new password to set.
 * @returns {Promise<Object|string>} The API response.
 */
const resetPassword = async (token, newPassword) => {
  const url = `${API_ENDPOINTS.auth.resetPassword}?recoveryToken=${encodeURIComponent(token)}`;
  return apiCall(url, {
    method: 'POST',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ password: newPassword }),
  });
};

/**
 * Updates the role and manager of a user by user ID.
 *
 * @param {number|string} userId - The ID of the user to update.
 * @param {string} role - The new role ("USER", "MANAGER", "ADMIN").
 * @param {number|null} managerId - The ID of the new manager (nullable).
 * @returns {Promise<Object>} The API response.
 * @throws {Error} If the update fails.
 */
const updateUserRoleAndManager = async (userId, role, managerId) => {
  const sessionToken = sessionStorage.getItem('authToken');

  if (!sessionToken) throw new Error("No session token found.");

  const response = await apiCall(API_ENDPOINTS.auth.roleUpdate(userId), {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      sessionToken,
    },
    body: JSON.stringify({
      role,
      managerId: managerId || null,
    }),
  });

  return response;
};


/**
 * Validates and refreshes a session token.
 * 
 * @param {string} sessionToken - The session token to validate.
 * @returns {Promise<Object>} Session status info if valid, error if expired or invalid.
 */
const validateSession = async (sessionToken) => {
  return apiCall(API_ENDPOINTS.auth.validateSession, {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
      sessionToken: sessionToken
    }
  });
};

export const authAPI = {
  changePassword,
  confirmAccount,
  loginUser,
  logoutUser,
  registerUser,
  requestPasswordReset,
  resetPassword,
  updateUserRoleAndManager,
  validateSession,
};


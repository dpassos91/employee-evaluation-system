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
 */

import { apiConfig } from './apiConfig.js';
import { userAPI } from './userAPI.js';

const { apiCall, API_ENDPOINTS } = apiConfig;
const { getUserById } = userAPI;

/**
 * Logs in a user, stores the JWT token in sessionStorage,
 * and retrieves full user details.
 * 
 * @param {Object} credentials - User credentials (e.g., { email, password }).
 * @returns {Promise<Object>} An object containing user details and the JWT token.
 * @throws {Error} If login fails.
 */
const loginUser = async (credentials) => {
  const loginResponse = await apiCall(API_ENDPOINTS.auth.login, {
    method: 'POST',
    body: JSON.stringify(credentials),
    // Headers are set automatically by apiCall
  });

  const token = loginResponse.token;
  sessionStorage.setItem('authToken', token);

  // Optionally fetch full user details using the returned userId
  const userDetails = await getUserById(loginResponse.userId);

  return {
    ...userDetails,
    token,
  };
};

/**
 * Logs out the authenticated user and removes the JWT token from sessionStorage.
 * 
 * @returns {Promise<boolean>} True if logout is successful.
 * @throws {Error} If logout fails or token does not exist.
 */
const logoutUser = async () => {
  const token = sessionStorage.getItem('authToken');
  if (!token) {
    throw new Error("Token does not exist in sessionStorage.");
  }

  // Authorization header is automatically handled by apiCall
  const result = await apiCall(API_ENDPOINTS.auth.logout, {
    method: 'POST',
  });

  if (result === "Successfully logged out!") {
    sessionStorage.removeItem('authToken');
    return true;
  } else {
    throw new Error("Logout failed: " + result);
  }
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
    body: JSON.stringify({ email }),
    // Headers are set automatically by apiCall
  });
};

/**
 * Resets the user's password using a reset token and the new password.
 * 
 * @param {string} token - The password reset token.
 * @param {string} newPassword - The new password to set.
 * @returns {Promise<Object|string>} The API response.
 */
const resetPassword = async (token, newPassword) => {
  return apiCall(API_ENDPOINTS.auth.resetPassword, {
    method: 'POST',
    body: JSON.stringify({ token, newPassword }),
  });
};

export const authAPI = {
  loginUser,
  logoutUser,
  requestPasswordReset,
  resetPassword,
};

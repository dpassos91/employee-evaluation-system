/**
 * Settings API
 * Centralized functions for managing system configuration.
 *
 * Usage:
 * import { settingsAPI } from './settingsAPI';
 * await settingsAPI.getTimeouts();
 * await settingsAPI.updateConfirmationTimeout(minutes);
 * await settingsAPI.updateRecoveryTimeout(minutes);
 * await settingsAPI.updateSessionTimeout(minutes);
 * await settingsAPI.promoteToAdmin(email);
 */

import { apiConfig } from './apiConfig';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Fetches current timeout settings from the backend.
 */
const getTimeouts = async () => {
  return apiCall(API_ENDPOINTS.settings.getTimeouts, {
    method: 'GET',
  });
};

/**
 * Updates the confirmation token timeout value (in minutes).
 */
const updateConfirmationTimeout = async (minutes) => {
  return apiCall(API_ENDPOINTS.settings.updateConfirmationTimeout, {
    method: 'PUT',
    headers: {
      'Content-Type': 'text/plain',
      Accept: 'application/json',
    },
    body: String(minutes),
  });
};

/**
 * Updates the recovery token timeout value (in minutes).
 */
const updateRecoveryTimeout = async (minutes) => {
  return apiCall(API_ENDPOINTS.settings.updateRecoveryTimeout, {
    method: 'PUT',
    headers: {
      'Content-Type': 'text/plain',
      Accept: 'application/json',
    },
    body: String(minutes),
  });
};

/**
 * Updates the session timeout value (in minutes).
 */
const updateSessionTimeout = async (minutes) => {
  return apiCall(API_ENDPOINTS.settings.updateSessionTimeout, {
    method: 'PUT',
    headers: {
      'Content-Type': 'text/plain',
      Accept: 'application/json',
    },
    body: String(minutes),
  });
};

/**
 * Promotes a user to ADMIN by their email address.
 */
const promoteToAdmin = async (email) => {
  return apiCall(API_ENDPOINTS.settings.promoteToAdmin(email), {
    method: 'PUT',
  });
};

export const settingsAPI = {
  getTimeouts,
  updateConfirmationTimeout,
  updateRecoveryTimeout,
  updateSessionTimeout,
  promoteToAdmin,
};

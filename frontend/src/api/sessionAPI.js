/**
 * Session API
 * Centralized functions to validate and monitor session tokens.
 *
 * Usage:
 * import { sessionAPI } from './sessionAPI';
 * await sessionAPI.checkSessionStatus();
 */

import { apiConfig } from './apiConfig';
const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Checks if the session token is still valid.
 * Returns SessionStatusDto or throws if expired.
 */
const checkSessionStatus = async () => {
  return apiCall(API_ENDPOINTS.auth.validateSessionStatus, {
    method: 'GET',
    headers: {
      sessionToken: sessionStorage.getItem("authToken"),
    },
    // Do NOT auto-logout here; we control it from hook
    forceLogoutOn401: false,
  });
};

export const sessionAPI = {
  checkSessionStatus,
};
 
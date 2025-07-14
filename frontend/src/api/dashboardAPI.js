import { apiConfig } from "./apiConfig";

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Fetches dashboard summary data for the authenticated user.
 *
 * @param {string} sessionToken - The session token for authentication (optional if already included in apiCall).
 * @returns {Promise<Object>} The dashboard summary DTO.
 * @throws {Error} If the request fails or returns an error status.
 */
export function getDashboardSummary() {
  return apiCall(API_ENDPOINTS.dashboard.dashboardSummary, {
    method: 'GET',
  });
}
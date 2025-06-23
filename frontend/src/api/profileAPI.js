import { apiCall } from "./apiConfig";


/**
 * Profile API
 * Centralized functions for user profile management.
 * 
 * Usage:
 * import { profileAPI } from './profileAPI';
 * await profileAPI.updateProfile(email, profileData, sessionToken);
 * await profileAPI.listUsersByFilters({ filters }, sessionToken);
 * await profileAPI.getUsualWorkplaces();
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Updates the profile for a given user email.
 * @param {string} email - The user's email.
 * @param {Object} profileData - The profile data to update.
 * @param {string} sessionToken - The session token (for authorization).
 * @returns {Promise<Object>} The API response.
 */
const updateProfile = async (email, profileData, sessionToken) => {
  return apiCall(API_ENDPOINTS.profiles.update(email), {
    method: 'PUT',
    headers: {
      "Content-Type": "application/json",
      sessionToken: sessionToken
    },
    body: JSON.stringify(profileData),
  });
};

/**
 * Lists users filtered by the given parameters.
 * @param {Object} filters - Filters (e.g., { profileName, usualLocation, managerName }).
 * @param {string} sessionToken - The session token (for authorization).
 * @returns {Promise<Array>} List of users.
 */
const listUsersByFilters = async (filters, sessionToken) => {
  // Build query params string
  const params = new URLSearchParams();
  if (filters.profileName) params.append('profile-name', filters.profileName);
  if (filters.usualLocation) params.append('usual-work-place', filters.usualLocation);
  if (filters.managerName) params.append('manager-name', filters.managerName);

  const url = `${API_ENDPOINTS.profiles.listUsersByFilters}?${params.toString()}`;
  return apiCall(url, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Gets the list of possible usual workplaces (enum values).
 * @returns {Promise<Array<string>>} List of usual workplace options.
 */
const getUsualWorkplaces = async () => {
  return apiCall(API_ENDPOINTS.profiles.usualWorkplaces, {
    method: 'GET',
  });
};

export const profileAPI = {
  updateProfile,
  listUsersByFilters,
  getUsualWorkplaces,
};
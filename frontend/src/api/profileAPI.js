
/**
 * Profile API
 * Centralized functions for user profile management.
 * 
 * Usage:
 * import { profileAPI } from './profileAPI';
 * await profileAPI.getProfile(email, sessionToken);
 * await profileAPI.getUsualWorkplaces();
 * await profileAPI.listUsersByFilters({ filters }, sessionToken);
 * await profileAPI.updateProfile(email, profileData, sessionToken);
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Obtém o URL da foto de perfil pelo nome do ficheiro.
 * @param {string} fileName - O nome do ficheiro da foto.
 * @returns {string} URL absoluto para a foto.
 */
const getPhoto = (fileName) => {
  if (!fileName || fileName.trim() === "") return null;
  return `${apiConfig.API_ENDPOINTS.profiles.getPhoto(fileName)}?t=${Date.now()}`;
};

/**
 * Fetches the profile of a user by email.
 * @param {string} email - The user's email address.
 * @param {string} sessionToken - The session token for authorization.
 * @returns {Promise<Object>} The user's profile DTO from the backend.
 */
const getProfileByEmail = async (email, sessionToken) => {
  return apiCall(API_ENDPOINTS.profiles.getProfileByEmail(email), {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Fetches the profile of a user by ID.
 * @param {string} id - The user's ID.
 * @param {string} sessionToken - The session token for authorization.
 * @param {object} extraOptions - Additional options (ex: forceLogoutOn401: false)
 * @returns {Promise<Object>} The user's profile DTO from the backend.
 */
const getProfileById = async (userId, sessionToken, extraOptions = {}) => {
  return apiCall(API_ENDPOINTS.profiles.getProfileById(userId), {
    method: 'GET',
    headers: { sessionToken },
    ...extraOptions, // <-- permite passar forceLogoutOn401
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
 * Uploads a profile photo for the user.
 * @param {string} email - The user's email.
 * @param {File} file - The selected photo file.
 * @param {string} sessionToken - The session token.
 * @returns {Promise<Object>} The API response.
 */
const uploadPhoto = async (email, file, sessionToken) => {
  const url = API_ENDPOINTS.profiles.uploadPhoto(email);

  const formData = new FormData();
  formData.append("photo", file, file.name);      // Nome do campo tem de bater com o backend!
  formData.append("fileName", file.name);

  // IMPORTANTE: Não definas 'Content-Type', o browser faz isso por ti com FormData!

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      sessionToken,
      // NÃO ponhas 'Content-Type'
    },
    body: formData,
  });

  if (!response.ok) {
    // Aqui podes fazer melhor handling de erro se quiseres
    throw new Error('Failed to upload photo');
  }

  return await response.json();
};



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

export const profileAPI = {
  getPhoto,
  getProfileByEmail,
  getProfileById,
  getUsualWorkplaces,
  listUsersByFilters,
  updateProfile,
  uploadPhoto,
};
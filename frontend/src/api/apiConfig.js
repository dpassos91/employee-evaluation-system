/**
 * API Configuration
 * Centralized configuration for all API endpoints and helpers.
 * 
 * For usage, import { apiConfig } from 'api/apiConfig';
 */

/** 
 * The base URL for all API requests.
 * @type {string}
 */
const API_BASE_URL = 'https://localhost:8443/grupo7/rest';

/**
 * All API endpoint URLs organized by resource.
 * Fill in each group with the corresponding endpoints as needed.
 * @type {object}
 */
const API_ENDPOINTS = {
  /** Authentication endpoints */
  auth: {
    /** @function Confirm user registration with token */
    confirm: (token) => `${API_BASE_URL}/users/confirm?token=${token}`,
    /** @type {string} User login endpoint */
    login: `${API_BASE_URL}/users/login`,
    /** @type {string} User logout endpoint */
    logout: `${API_BASE_URL}/users/logout`,
    /** @type {string} User registration endpoint */
    register: `${API_BASE_URL}/users/createUser`,
    /** @type {string} Request password reset endpoint */
    requestResetPassword: `${API_BASE_URL}/users/request-reset`,
    /** @type {string} Reset password endpoint */
    resetPassword: `${API_BASE_URL}/users/reset-password`,
    /** @type {string} Validate session endpoint */
    validateSession: `${API_BASE_URL}/users/validate-session`
  },

  /** Course endpoints (to be filled in) */
  courses: {
    // e.g.: list: `${API_BASE_URL}/courses`,
  },

  /** Dashboard endpoint */
  dashboardOverview: `${API_BASE_URL}/dashboard/overview`,

  /** Evaluation endpoints (to be filled in) */
  evaluations: {
    // e.g.: list: `${API_BASE_URL}/evaluations`,
  },

  /** Message endpoints (to be filled in) */
  messages: {
    // e.g.: send: `${API_BASE_URL}/messages/send`,
  },


/** Profile endpoints */
profiles: {
  /** @function Get user profile by email */
  get: (email) => `${API_BASE_URL}/profiles/${encodeURIComponent(email)}`,
  /** @function List users by filters */
  listUsersByFilters: `${API_BASE_URL}/profiles/list-users-by-filters`,
    /** @function Update user profile by email */
  update: (email) => `${API_BASE_URL}/profiles/update/${encodeURIComponent(email)}`,
  /** @type {string} Get list of possible usual workplaces */
  usualWorkplaces: `${API_BASE_URL}/profiles/usualworkplaces`,
},

  /** Settings endpoints */
  settings: {
    /** @type {string} Get current settings */
    getSettings: `${API_BASE_URL}/settings`,
    /** @type {string} Update settings */
    updateSettings: `${API_BASE_URL}/settings`,
  },
};

/**
 * Default options for all API requests.
 * @type {object}
 */
export const DEFAULT_OPTIONS = {
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
};

/**
 * Adds the JWT auth token from sessionStorage to the request headers, if it exists.
 * @param {object} options - The original fetch options.
 * @returns {object} The modified options with the Authorization header if token exists.
 */
export const authInterceptor = (options = {}) => {
  const token = sessionStorage.getItem("authToken");
  if (token) {
    return {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${token}`,
      },
    };
  }
  return options;
};

/**
 * Handles errors from API calls. Currently logs to the console.
 * Replace or extend this for more user-friendly notifications (e.g., toast/snackbar).
 * @param {Error} error - The error object.
 */
export const handleApiError = (error) => {
  console.error("API Error:", error);
  // Extend here for notifications, e.g.:
  // showToast(error.message || "Ocorreu um erro inesperado.");
};

/**
 * Makes a generic API call using fetch, automatically injecting headers and the auth token.
 * Handles session expiration (401), error reporting, and JSON parsing.
 * 
 * @async
 * @param {string} url - The full endpoint URL.
 * @param {object} [options={}] - The fetch options (method, headers, body, etc.).
 * @returns {Promise<object|string|null>} The API response, parsed as JSON if possible, or raw text/null.
 * @throws {Error} If the request fails or the API returns an error status.
 */
export const apiCall = async (url, options = {}) => {
  // Merge options with defaults and add Authorization header if needed
  let finalOptions = {
    ...DEFAULT_OPTIONS,
    ...options,
    headers: {
      ...DEFAULT_OPTIONS.headers,
      ...(options.headers || {}),
    },
  };

  finalOptions = authInterceptor(finalOptions);

  try {
    const response = await fetch(url, finalOptions);

    // Handle expired session (unauthorized)
if (
  response.status === 401 &&
  !(url.endsWith("/logout") || url.includes("/logout"))
) {
  alert("Sessão expirada. Por favor faça login novamente.");
  sessionStorage.removeItem("authToken");
  localStorage.removeItem("userData");
  window.location.href = "/login";
  return;
}

    const contentType = response.headers.get("content-type");
    let responseBody = await response.text();

    if (!response.ok) {
      // Try to extract a message from JSON error response
      let message = responseBody;
      if (contentType && contentType.includes("application/json")) {
        try {
          const data = JSON.parse(responseBody);
          message = data.message || responseBody;
        } catch {
          // leave message as text
        }
      }
      const error = new Error(message || "Erro desconhecido da API");
      error.status = response.status;
      error.body = responseBody;
      throw error;
    }

    if (responseBody.trim() === "") return null;

    if (contentType && contentType.includes("application/json")) {
      try {
        return JSON.parse(responseBody);
      } catch {
        return responseBody; // fallback to text
      }
    }
    return responseBody;
  } catch (error) {
    handleApiError(error);
    throw error;
  }
};

/**
 * The exported API configuration object.
 * Import and use apiConfig.apiCall, apiConfig.API_ENDPOINTS, etc.
 */
export const apiConfig = {
  API_BASE_URL,
  API_ENDPOINTS,
  DEFAULT_OPTIONS,
  authInterceptor,
  apiCall,
  handleApiError,
};


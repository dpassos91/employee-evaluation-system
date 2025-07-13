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
    /** @function Change user password by email */
    changePassword: (email) => `${API_BASE_URL}/users/update/${email}/password`,
    /** @function Confirm user registration with token */
    confirm: (token) => `${API_BASE_URL}/users/confirm?token=${token}`,
    /** @function Get all managers (users with the MANAGER role, active and confirmed) */
    managers: `${API_BASE_URL}/users/managers`,
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
    /** @function Update user role and manager by user id */
    roleUpdate: (id) => `${API_BASE_URL}/users/${id}/role-manager`,
    /** @type {string} Validate session endpoint */
    validateSession: `${API_BASE_URL}/users/validate-session`
  },

  /** Course endpoints*/
  courses: {
    /** @type {string} List all courses (with filters as query params) */
    list: `${API_BASE_URL}/courses`,
    /** @function Get course details by ID */
    get: (id) => `${API_BASE_URL}/courses/${id}`,
    /** @type {string} Create a new course */
    create: `${API_BASE_URL}/courses`,
    /** @function Update a course by ID */
    update: (id) => `${API_BASE_URL}/courses/${id}`,
    /** @function Deactivate (soft delete) a course by ID */
    deactivate: (id) => `${API_BASE_URL}/courses/${id}`,
    /** @type {string} Export the list of courses to CSV */
    export: `${API_BASE_URL}/courses/export/csv`,
    /** @type {string} Register user participation in a course */

    // Associations course-user

    assignToUser: `${API_BASE_URL}/user-courses`,
    /** @function Get a user's training history */
    userHistory: (userId) => `${API_BASE_URL}/user-courses/user/${userId}`,
    /** @function Export a user's training history to CSV */
    userHistoryExport: (userId) => `${API_BASE_URL}/user-courses/user/${userId}/export/csv`,
    /** @function Get a user's training history for a specific year */
    userHistoryYear: (userId, year) => `${API_BASE_URL}/user-courses/user/${userId}/year/${year}`,
    /** @function Export a user's training history for a specific year to CSV */
    userHistoryYearExport: (userId, year) => `${API_BASE_URL}/user-courses/user/${userId}/year/${year}/export/csv`
  },

  /** Dashboard endpoint */
  dashboardOverview: `${API_BASE_URL}/dashboard/overview`,

/** Evaluation endpoints */
evaluations: {
  /** @type {string} List evaluations by filters (paginated) */
  listByFilters: `${API_BASE_URL}/evaluations/list-by-filters`,
  /** @type {string} Get all evaluation states (e.g., IN_EVALUATION, EVALUATED, CLOSED) */
  getAllStates: `${API_BASE_URL}/evaluations/states`,
  /** @type {string} Export evaluations to CSV */
  exportCsv: `${API_BASE_URL}/evaluations/export-csv`,
  /** @function Export a closed evaluation to PDF */
  exportPdf: (id) => `${API_BASE_URL}/evaluations/export-pdf?id=${id}`,
  /** @function Get evaluation history for a user (paginated) */
  getHistory: (email, page = 1) => `${API_BASE_URL}/evaluations/history?email=${encodeURIComponent(email)}&page=${page}`,
  /** @type {string} List evaluation grade options (1 to 4) */
  listGradeOptions: `${API_BASE_URL}/evaluations/list-evaluation-options`,
  /** @function Load an evaluation for a user in the current cycle */
  load: (userId) => `${API_BASE_URL}/evaluations/load-evaluation?userId=${userId}`,
  /** @type {string} Update an evaluation with grade and feedback */
  update: `${API_BASE_URL}/evaluations/update-evaluation`,
  /** @function Reopen an evaluation by ID */
  reopen: (evaluationId) => `${API_BASE_URL}/evaluations/reopen-for-editing/${evaluationId}`,
  /** @function Close an individual evaluation by ID */
  close: (evaluationId) => `${API_BASE_URL}/evaluations/close/${evaluationId}`,
  /** @type {string} Close all evaluations in the current cycle (admin only) */
  bulkClose: `${API_BASE_URL}/evaluations/close-all`,
  /** @function Get evaluation history with filters (paginated) */
  historyWithFilters: `${API_BASE_URL}/evaluations/history-with-filters`,

// Associations evaluations-user
managerDropdown: `${API_BASE_URL}/users/manager-dropdown`,
assignManager: `${API_BASE_URL}/users/assign-manager`,
},

/** Evaluation cycle endpoints */
evaluationCycles: {
  /** @type {string} Create a new evaluation cycle */
  create: `${API_BASE_URL}/evaluations-cycles/create-cycle`,

  /** @type {string} Get list of confirmed users without a manager (admin only) */
  usersWithoutManager: `${API_BASE_URL}/evaluations-cycles/list-users-withouth-manager`,

  /** @type {string} Get list of users with incomplete evaluations (admin only) */
  incompleteEvaluations: `${API_BASE_URL}/evaluations-cycles/list-incomplete-evaluations`,


// Associations evaluation cycles-user

   /** @type {string} Get paginated list of confirmed users without manager (admin only) */
  usersWithoutManagerPaginated: `${API_BASE_URL}/users/users-without-manager-paginated`,
},


  /** Message endpoints*/
 messages: {
  /** @function Gets the conversation (all messages) with another user by their user ID */
  getConversation: (otherUserId) => `${API_BASE_URL}/messages/with/${otherUserId}`,
  /** @type {string} Endpoint to send a new message */
  send: `${API_BASE_URL}/messages`,
  /** @function Marks all messages as read from the given user */
  markAsRead: (otherUserId) => `${API_BASE_URL}/messages/read-from/${otherUserId}`,
  /** @type {string} Gets all sidebar conversations for the authenticated user */
  chatSidebarConversations: `${API_BASE_URL}/messages/conversations`,
},

/** Notification endpoints */
notifications: {
  /** @type {string} Gets all notifications for the authenticated user */
  list: `${API_BASE_URL}/notifications`,
  /** @type {string} Gets all unread notifications */
  unread: `${API_BASE_URL}/notifications/unread`,
  /** @type {string} Marks all notifications as read */
  markAllAsRead: `${API_BASE_URL}/notifications/read`,
  /** @type {string} Marks all MESSAGE-type notifications as read */
  markAllMessageNotificationsAsRead: `${API_BASE_URL}/notifications/read/message`,
  /** @type {string} Gets unread notification counts by type */
  unreadCountByType: `${API_BASE_URL}/notifications/unread/count-by-type`,
  /** @type {string} Gets all unread non-MESSAGE notifications */
  unreadNonMessage: `${API_BASE_URL}/notifications/unread/non-message`,
},

/** Profile endpoints */
profiles: {
  /** @function Export users to CSV */
  exportUsersCsv: `${API_BASE_URL}/profiles/export-users-csv`,
  getPhoto: (fileName) => `${API_BASE_URL}/profiles/photo/${fileName}`,
  /** @function Get user profile by email */
  getProfileByEmail: (email) => `${API_BASE_URL}/profiles/${encodeURIComponent(email)}`,
  /** @function Get user profile by ID */
  getProfileById: (userId) => `${API_BASE_URL}/profiles/by-id/${userId}`,
  /** @function List users by filters */
  listUsersByFilters: `${API_BASE_URL}/profiles/list-users-by-filters`,
  /** @function Upload user profile photo */
  uploadPhoto: (email) => `${API_BASE_URL}/profiles/${email}/upload-photo`,
  /** @function Update user profile by email */
  update: (email) => `${API_BASE_URL}/profiles/update/${encodeURIComponent(email)}`,
  /** @type {string} Get list of possible usual workplaces */
  usualWorkplaces: `${API_BASE_URL}/profiles/usualworkplaces`,
},

  /** Settings endpoints */
settings: {
  getTimeouts: `${API_BASE_URL}/settings/timeouts`,
  updateConfirmationTimeout: `${API_BASE_URL}/settings/confirmation-timeout`,
  updateRecoveryTimeout: `${API_BASE_URL}/settings/recovery-timeout`,
  updateSessionTimeout: `${API_BASE_URL}/settings/session-timeout`,

  //Settings - cycle relationship

  promoteToAdmin: (email) =>
    `${API_BASE_URL}/users/promote-to-admin?email=${encodeURIComponent(email)}`,

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
        token,
        sessionToken: token,
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
/**
 * Makes a generic API call using fetch, with fine control over 401 behavior.
 * By default, forces logout on 401. To disable auto-logout for a call, pass { forceLogoutOn401: false } in options.
 */
export const apiCall = async (url, options = {}) => {
  // If not specified, auto-logout on 401 is enabled
  const forceLogoutOn401 = options.forceLogoutOn401 !== false;

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
      !(url.endsWith("/logout") || url.includes("/logout")) &&
      forceLogoutOn401
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


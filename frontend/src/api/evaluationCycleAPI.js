/**
 * Evaluation Cycle API
 * Centralized functions for managing evaluation cycles.
 *
 * Usage:
 * import { evaluationCycleAPI } from './evaluationCycleAPI';
 * await evaluationCycleAPI.createCycle({ endDate }, sessionToken);
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS, API_BASE_URL } = apiConfig;
/**
 * Creates a new evaluation cycle.
 * Only allowed for admins.
 *
 * @param {object} payload - { endDate: string in ISO format }
 * @param {string} sessionToken - JWT or session token from sessionStorage
 * @returns {Promise<object>} - API response
 */
const createCycle = async (payload, sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluationCycles.create, {
    method: 'POST',
    headers: {
      "Content-Type": "application/json",
      sessionToken,
    },
    body: JSON.stringify(payload),
  });
};

/**
 * Gets the number of confirmed users without a manager assigned.
 * Only accessible to admins.
 *
 * @param {string} sessionToken - JWT or session token from sessionStorage
 * @returns {Promise<object>} - API response containing:
 *   {
 *     numberOfUsersWithoutManager: number,
 *     usersWithoutManager: UserDto[]
 *   }
 */
const getUsersWithoutManager = async (sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluationCycles.usersWithoutManager, {
    method: 'GET',
    headers: {
      sessionToken,
    },
  });
};

/**
 * Gets the number of users with incomplete evaluations in the current cycle.
 * Only accessible to admins.
 *
 * @param {string} sessionToken - JWT or session token from sessionStorage
 * @returns {Promise<object>} - API response containing:
 *   {
 *     totalUsersWithIncompleteEvaluations: number,
 *     users: UserDto[]
 *   }
 */
const getIncompleteEvaluations = async (sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluationCycles.incompleteEvaluations, {
    method: 'GET',
    headers: {
      sessionToken,
    },
  });
};


export const evaluationCycleAPI = {
  createCycle,
  getUsersWithoutManager,
  getIncompleteEvaluations,
};
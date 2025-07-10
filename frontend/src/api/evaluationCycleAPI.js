/**
 * Evaluation Cycle API
 * Centralized functions for managing evaluation cycles.
 *
 * Usage:
 * import { evaluationCycleAPI } from './evaluationCycleAPI';
 * await evaluationCycleAPI.createCycle({ endDate }, sessionToken);
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

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

// Future methods can go here, e.g., listActiveCycles, getCycleStats, etc.

export const evaluationCycleAPI = {
  createCycle,
};

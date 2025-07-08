/**
 * Evaluation API
 * Centralized functions for managing evaluations.
 *
 * Usage:
 * import { evaluationAPI } from './evaluationAPI';
 * await evaluationAPI.listEvaluationsByFilters(filters, sessionToken);
 * await evaluationAPI.getEvaluationStates();
 * await evaluationAPI.exportEvaluationsCsv(filters, sessionToken);
 * await evaluationAPI.exportEvaluationPdf(evaluationId, sessionToken);
 * await evaluationAPI.getEvaluationHistory(email, page, sessionToken);
 * await evaluationAPI.getGradeOptions(sessionToken);
 * await evaluationAPI.loadEvaluation(email, sessionToken);
 * await evaluationAPI.updateEvaluation(payload, sessionToken);
 * await evaluationAPI.reopenEvaluation(evaluationId, sessionToken);
 * await evaluationAPI.closeEvaluation(evaluationId, sessionToken);
 * await evaluationAPI.bulkCloseEvaluations(sessionToken);
 */

import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Lists evaluations based on filters.
 */
const listEvaluationsByFilters = async (filters, sessionToken) => {
  const params = new URLSearchParams();
  if (filters.name) params.append('evaluated-name', filters.name);
  if (filters.evaluationState) params.append('evaluation-state', filters.evaluationState);
  if (filters.grade) params.append('grade', filters.grade);
  if (filters.cycleEndDate) params.append('cycle-end-date', filters.cycleEndDate);
  if (filters.page) params.append('page', filters.page);

  const url = `${API_ENDPOINTS.evaluations.listByFilters}?${params.toString()}`;

  return apiCall(url, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Gets the list of possible evaluation states.
 */
const getEvaluationStates = async () => {
  return apiCall(API_ENDPOINTS.evaluations.getAllStates, {
    method: 'GET',
  });
};

/**
 * Exports filtered evaluations to CSV.
 */
const exportEvaluationsCsv = async (filters, sessionToken) => {
  const params = new URLSearchParams();
  if (filters.name) params.append('name', filters.name);
  if (filters.state) params.append('state', filters.state);
  if (filters.grade) params.append('grade', filters.grade);
  if (filters.cycleEnd) params.append('cycleEnd', filters.cycleEnd);

  const url = `${API_ENDPOINTS.evaluations.exportCsv}?${params.toString()}`;
  return apiCall(url, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Exports a single closed evaluation to PDF.
 */
const exportEvaluationPdf = async (evaluationId, sessionToken) => {
  const url = API_ENDPOINTS.evaluations.exportPdf(evaluationId);
  return apiCall(url, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Gets evaluation history for a user (paginated).
 */
const getEvaluationHistory = async (email, page, sessionToken) => {
  const url = API_ENDPOINTS.evaluations.getHistory(email, page);
  return apiCall(url, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Fetches the list of grade options (1 to 4).
 */
const getGradeOptions = async (sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluations.listGradeOptions, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Loads evaluation data for a specific evaluated user in the active cycle.
 */
const loadEvaluation = async (email, sessionToken) => {
  const url = API_ENDPOINTS.evaluations.load(email);
  return apiCall(url, {
    method: 'GET',
    headers: { sessionToken },
  });
};

/**
 * Updates an evaluation with grade and feedback.
 */
const updateEvaluation = async (payload, sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluations.update, {
    method: 'PUT',
    headers: {
      "Content-Type": "application/json",
      sessionToken,
    },
    body: JSON.stringify(payload),
  });
};

/**
 * Reopens an evaluation for editing (admin only).
 */
const reopenEvaluation = async (evaluationId, sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluations.reopen(evaluationId), {
    method: 'PUT',
    headers: { sessionToken },
  });
};

/**
 * Closes a single evaluation (admin only).
 */
const closeEvaluation = async (evaluationId, sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluations.close(evaluationId), {
    method: 'PUT',
    headers: { sessionToken },
  });
};

/**
 * Closes all evaluations in the active cycle (admin only).
 */
const bulkCloseEvaluations = async (sessionToken) => {
  return apiCall(API_ENDPOINTS.evaluations.bulkClose, {
    method: 'PUT',
    headers: { sessionToken },
  });
};

export const evaluationAPI = {
  listEvaluationsByFilters,
  getEvaluationStates,
  exportEvaluationsCsv,
  exportEvaluationPdf,
  getEvaluationHistory,
  getGradeOptions,
  loadEvaluation,
  updateEvaluation,
  reopenEvaluation,
  closeEvaluation,
  bulkCloseEvaluations,
};

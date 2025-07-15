import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Lists all courses (optionally filtered).
 * @param {Object} [filters] - Optional filters as query params.
 * @returns {Promise<Array>} List of CourseDto.
 */
const listCourses = async (filters = {}) => {
  // Build query string from filters
  const params = new URLSearchParams(filters).toString();
  const url = params ? `${API_ENDPOINTS.courses.list}?${params}` : API_ENDPOINTS.courses.list;
  return apiCall(url, { method: 'GET' });
};

/**
 * Gets course details by ID.
 * @param {number} id - Course ID.
 * @returns {Promise<Object>} CourseDto.
 */
const getCourse = async (id) => {
  return apiCall(API_ENDPOINTS.courses.get(id), { method: 'GET' });
};

/**
 * Creates a new course.
 * @param {Object} data - CreateCourseDto.
 * @returns {Promise<Object>} API response.
 */
const createCourse = async (data) => {
  return apiCall(API_ENDPOINTS.courses.create, {
    method: 'POST',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
};

/**
 * Updates a course by ID.
 * @param {number} id - Course ID.
 * @param {Object} data - UpdateCourseDto.
 * @returns {Promise<Object>} API response.
 */
const updateCourse = async (id, data) => {
  return apiCall(API_ENDPOINTS.courses.update(id), {
    method: 'PUT',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
};

/**
 * Deactivates (soft deletes) a course by ID.
 * @param {number} id - Course ID.
 * @returns {Promise<Object>} API response.
 */
const deactivateCourse = async (id) => {
  return apiCall(API_ENDPOINTS.courses.deactivate(id), {
    method: 'DELETE'
  });
};

/**
 * Exports the list of courses to CSV.
 * @param {Object} [filters] - Optional filters as query params.
 * @returns {Promise<string>} CSV data.
 */
const exportCoursesCsv = async (filters = {}) => {
  const params = new URLSearchParams(filters).toString();
  const url = params ? `${API_ENDPOINTS.courses.export}?${params}` : API_ENDPOINTS.courses.export;
  return apiCall(url, { method: 'GET' });
};

/**
 * Registers user participation in a course.
 * @param {Object} data - CreateUserCourseDto ({ userId, courseId, participationDate }).
 * @returns {Promise<Object>} API response.
 */
const assignCourseToUser = async (data) => {
  return apiCall(API_ENDPOINTS.courses.assignToUser, {
    method: 'POST',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
};

/**
 * Gets a user's full training history.
 * @param {number} userId - User ID.
 * @returns {Promise<Array>} List of UserCourseDto.
 */
const getUserCourseHistory = async (userId) => {
  return apiCall(API_ENDPOINTS.courses.userHistory(userId), { method: 'GET' });
};

/**
 * Exports a user's training history to CSV.
 * @param {number} userId - User ID.
 * @returns {Promise<string>} CSV data.
 */
const exportUserCourseHistoryCsv = async (userId) => {
  return apiCall(API_ENDPOINTS.courses.userHistoryExport(userId), { method: 'GET' });
};

/**
 * Gets a user's training history for a specific year.
 * @param {number} userId - User ID.
 * @param {number} year - Year.
 * @returns {Promise<Array>} List of UserCourseDto.
 */
const getUserCourseHistoryByYear = async (userId, year) => {
  return apiCall(API_ENDPOINTS.courses.userHistoryYear(userId, year), { method: 'GET' });
};

/**
 * Exports a user's training history for a specific year to CSV.
 * @param {number} userId - User ID.
 * @param {number} year - Year.
 * @returns {Promise<string>} CSV data.
 */
const exportUserCourseHistoryByYearCsv = async (userId, year) => {
  return apiCall(API_ENDPOINTS.courses.userHistoryYearExport(userId, year), { method: 'GET' });
};


/**
 * Gets a user's training summary by year (total hours per year).
 * @param {number} userId - The user ID.
 * @returns {Promise<Array>} List of UserCourseYearSummaryDto.
 */
const getUserCourseSummaryByYear = async (userId) => {
  return apiCall(API_ENDPOINTS.courses.summaryByYear, {
    method: 'POST',
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId }),
  });
};

/**
 * Gets all distinct years in which a user has participated in training.
 * @param {number} userId - The user ID.
 * @returns {Promise<Array<number>>} List of years.
 */
const getUserCourseYears = async (userId) => {
  return apiCall(API_ENDPOINTS.courses.userYears(userId), { method: 'GET' });
};















export const courseAPI = {
  listCourses,
  getCourse,
  createCourse,
  updateCourse,
  deactivateCourse,
  exportCoursesCsv,
  assignCourseToUser,
  getUserCourseHistory,
  exportUserCourseHistoryCsv,
  getUserCourseHistoryByYear,
  exportUserCourseHistoryByYearCsv,
  getUserCourseSummaryByYear,
  getUserCourseYears,
};

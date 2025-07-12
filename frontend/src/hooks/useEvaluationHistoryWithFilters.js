import { useState, useEffect, useCallback } from "react";
import { apiConfig } from "../api/apiConfig";

/**
 * Hook to fetch evaluation history with filters.
 *
 * @param {string} email - Evaluated user's email
 * @param {object} filters - { cycleId, cycleEndDate, grade, page }
 * @returns {object} { evaluations, totalPages, totalCount, currentPage, loading, error, refetch }
 */
export function useEvaluationHistoryWithFilters(email, filters) {
  const [evaluations, setEvaluations] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchEvaluations = useCallback(async () => {
    setLoading(true);
    setError(null);

    const params = new URLSearchParams();
    params.append("email", email);
    if (filters.cycleId) params.append("cycle", filters.cycleId);
    if (filters.cycleEndDate) params.append("cycleEndDate", filters.cycleEndDate);
    if (filters.grade) params.append("grade", filters.grade);
    if (filters.page) params.append("page", filters.page);

    try {
      const result = await apiConfig.apiCall(
        `${apiConfig.API_ENDPOINTS.evaluations.historyWithFilters}?${params.toString()}`
      );

      const mapped = (result.evaluations || []).map((e) => ({
        id: e.evaluationId,
        cycleNumber: e.cycleNumber,
        cycleEndDate: e.evaluationDate,
        grade: e.grade,
        feedback: e.feedback,
      }));

      setEvaluations(mapped);
      setTotalPages(result.totalPages || 1);
      setTotalCount(result.totalCount || 0);
      setCurrentPage(result.currentPage || filters.page || 1);
    } catch (err) {
      setError(err.message || "Failed to load evaluations.");
      setEvaluations([]);
    } finally {
      setLoading(false);
    }
  }, [email, filters]);

  useEffect(() => {
    fetchEvaluations();
  }, [fetchEvaluations]);

  return {
    evaluations,
    totalPages,
    totalCount,
    currentPage,
    loading,
    error,
    refetch: fetchEvaluations,
  };
}

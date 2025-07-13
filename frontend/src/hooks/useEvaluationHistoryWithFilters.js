import { useState, useEffect, useCallback } from "react";
import { evaluationAPI } from "../api/evaluationAPI";

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

    try {
      const token = sessionStorage.getItem("authToken");
      const result = await evaluationAPI.getEvaluationHistoryWithFilters(email, filters, token);

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
      setError(err.message || "Erro ao carregar histórico de avaliações.");
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

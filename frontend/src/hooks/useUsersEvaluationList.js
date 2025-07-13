import { useState, useEffect, useCallback } from "react";
import { evaluationAPI } from "../api/evaluationAPI";

/**
 * Custom hook to fetch evaluation data with filters and pagination.
 * 
 * @param {object} filters - { name, evaluationState, cycleEndDate, grade, page }
 * @returns {object} { evaluations, totalPages, totalCount, currentPage, loading, error, refetch }
 */
export function useUsersEvaluationList(filters) {
  const [evaluations, setEvaluations] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // fetch evaluations, useCallback warrantes this funcion only changes when filter change  
const fetchEvaluations = useCallback(async () => {
   setLoading(true);
    setError(null);

    try {
      const token = sessionStorage.getItem("authToken");
      const result = await evaluationAPI.listEvaluationsByFilters(filters, token);

      const mapped = (result.evaluations || []).map(evaluation => ({
        id: evaluation.evaluationId,
        userId: evaluation.evaluatedId,
        evaluated: evaluation.evaluatedName,
        email: evaluation.evaluatedEmail,
        state: evaluation.state,
        grade: evaluation.grade,
        avatar: evaluation.photograph,
        evaluator: evaluation.evaluatorName,
        cycleEnd: evaluation.cycleEndDate,
      }));

      setEvaluations(mapped);
      setTotalPages(result.totalPages || 1);
      setTotalCount(result.totalCount || 0);
      setCurrentPage(result.currentPage || filters.page || 1);
    } catch (err) {
      setError(err.message || "Erro ao obter avaliações de utilizadores");
      setEvaluations([]);
    } finally {
      setLoading(false);
    }
  }, [filters]);

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

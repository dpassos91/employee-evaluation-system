import { useState, useEffect, useCallback } from "react";
import { apiConfig } from "../api/apiConfig";


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

// Build query string parameters based on filters
    const params = new URLSearchParams();
    if (filters.name) params.append("evaluated-name", filters.name);
    if (filters.evaluationState) params.append("evaluation-state", filters.evaluationState);
    if (filters.cycleEndDate) params.append("cycle-end-date", filters.cycleEndDate);
    if (filters.grade) params.append("grade", filters.grade);
    if (filters.page) params.append("page", filters.page);



try {
      const result = await apiConfig.apiCall(
        `${apiConfig.API_ENDPOINTS.profiles.listUsersByFilters}?${params.toString()}`
      );// `${apiConfig.API_ENDPOINTS.evaluations.listEvaluationsByFilters}?${params.toString()}`

      // Map evaluation list to expected format for the table
const mapped = (result.evaluations|| []).map(evaluation => ({
  id: evaluation.evaluationId, 
  evaluated: evaluation.evaluatedName, //the Dto that arrives at the frontend already concatenates first and last name
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

  // Refetch whenever filters change
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

    }
}

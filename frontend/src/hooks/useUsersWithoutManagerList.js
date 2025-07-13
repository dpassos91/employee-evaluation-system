// hooks/useUsersWithoutManagerList.js
import { useState, useEffect, useCallback } from "react";
import { evaluationCycleAPI } from "../api/evaluationCycleAPI";

/**
 * Hook to fetch paginated users without manager, with filters.
 *
 * @param {object} filters - { name?: string, office?: string, page?: number, pageSize?: number }
 * @returns {object} { users, totalCount, totalPages, currentPage, loading, error, refetch }
 */
export function useUsersWithoutManagerList(filters) {
  const [users, setUsers] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(filters.page || 1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const token = sessionStorage.getItem("authToken");
      const result = await evaluationCycleAPI.getUsersWithoutManagerPaginated(filters, token);

      setUsers(result.users || result.usersWithoutManager || []);
      setTotalCount(result.total || 0);
      setTotalPages(Math.ceil((result.total || 0) / (filters.pageSize || 10)));
      setCurrentPage(filters.page || 1);
    } catch (err) {
      setUsers([]);
      setTotalCount(0);
      setError(err.message || "Erro ao carregar utilizadores.");
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  return {
    users,
    totalCount,
    totalPages,
    currentPage,
    loading,
    error,
    refetch: fetchUsers,
  };
}
 
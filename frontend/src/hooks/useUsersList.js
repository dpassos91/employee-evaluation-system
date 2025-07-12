
import { useState, useEffect, useCallback } from "react";
import { apiConfig } from "../api/apiConfig";
import { formatWorkplace } from "../utils/formatWorkplace"; 

/**
 * Custom hook para obter lista de utilizadores com filtros e paginação.
 * @param {object} filters - { name, office, manager, page }
 * @returns {object} { users, totalPages, totalCount, currentPage, loading, error, refetch }
 */
export function useUsersList(filters) {
  const [users, setUsers] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalCount, setTotalCount] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);

    // Build query parameters based on filters
    const params = new URLSearchParams();
    if (filters.name) params.append("profile-name", filters.name);
    if (filters.office) params.append("usual-work-place", filters.office);
    if (filters.manager) params.append("manager-email", filters.manager);
    if (filters.page) params.append("page", filters.page);

    try {
      const result = await apiConfig.apiCall(
        `${apiConfig.API_ENDPOINTS.profiles.listUsersByFilters}?${params.toString()}`
      );

      // Map profiles to expected format for the table
const mapped = (result.profiles || []).map(profile => ({
  id: profile.userId,
  name: [profile.firstName, profile.lastName].filter(Boolean).join(" "),
  office: formatWorkplace(profile.usualWorkplace),
  manager: profile.managerName || "",
  email: profile.email,
  avatar: profile.photograph,
}));

      setUsers(mapped);
      setTotalPages(result.totalPages || 1);
      setTotalCount(result.totalCount || 0);
    } catch (err) {
      setError(err.message || "Erro ao obter utilizadores");
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  console.log("useUsersList: fetch triggered", filters);

  // Refetch whenever filters change
  useEffect(() => {
    fetchUsers();
  }, [filters]);

  return {
    users,
    totalPages,
    totalCount,
    loading,
    error,
    refetch: fetchUsers,
  };
}

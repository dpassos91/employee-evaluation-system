import { useState, useMemo, useEffect } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import { formatWorkplace } from "../utils/formatWorkplace";
import { useUsersWithoutManagerList } from "../hooks/useUsersWithoutManagerList";
import { apiConfig } from "../api/apiConfig";
import { userStore } from "../stores/userStore";
import { toast } from "react-toastify";


export default function UsersWithoutManagerPage() {
  const [nameFilter, setNameFilter] = useState("");
  const [officeFilter, setOfficeFilter] = useState("");
  const [page, setPage] = useState(1);
  const pageSize = 10;

  const user = userStore((state) => state.user);
  const isAdmin = user?.role === "ADMIN";

  const [selectedManagers, setSelectedManagers] = useState({});

  const [managerName, setManagerName] = useState("");
  const [managerEmail, setManagerEmail] = useState("");
  const [dropdownUsers, setDropdownUsers] = useState([]);
  const [selectedManager, setSelectedManager] = useState("");
  const [updatingManager, setUpdatingManager] = useState(false);

  
  const isChangeDisabled =
  updatingManager || selectedManager === managerEmail ;

  const offices = ["", "Boston", "Coimbra", "Lisboa", "Munich", "Porto", "Southampton", "Viseu"];

  // Fetch managers from managerDropdown endpoint
  useEffect(() => {
      const fetchDropdownUsers = async () => {
        if (!isAdmin) return;
        try {
          const result = await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.managerDropdown);
          setDropdownUsers(result);
        } catch (err) {
          toast.error("Erro ao carregar lista de gestores disponíveis.");
        }
      };
  
      fetchDropdownUsers();
    }, [isAdmin]);

  

  const filters = useMemo(() => ({
    name: nameFilter,
    office: officeFilter,
    page,
    pageSize,
  }), [nameFilter, officeFilter, page]);

  const {
    users,
    totalPages,
    loading,
    error,
    refetch
  } = useUsersWithoutManagerList(filters);

 const handleAssignManager = async (userEmail) => {
  const selectedManager = selectedManagers[userEmail];
  if (!selectedManager) return;

  setUpdatingManager(true);
  try {
    await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.assignManager, {
      method: "POST",
      body: JSON.stringify({ userEmail, managerEmail: selectedManager }),
    });

    toast.success("Gestor atribuído com sucesso.");
    refetch();
  } catch (err) {
    toast.error("Erro ao atribuir gestor.");
  } finally {
    setUpdatingManager(false);
  }
};

  const handleGoToPage = (p) => setPage(p);








 return (
  <PageLayout
    title={
      <FormattedMessage
        id="users.withoutManager.title"
        defaultMessage="Utilizadores Sem Gestor"
      />
    }
  >
    {/* Filters */}
    <div className="mb-4 flex gap-4">
  
  <FormattedMessage id="filter.byName" defaultMessage="Filtrar por nome">
    {(placeholderText) => (
      <input
        type="text"
        className="border px-2 py-1 rounded"
        placeholder={placeholderText}
        value={nameFilter}
        onChange={(e) => {
          setNameFilter(e.target.value);
          setPage(1);
        }}
      />
    )}
  </FormattedMessage>

  {/* Select from filtered office FormattedMessage with option "Any office" */}
  <select
    className="border px-2 py-1 rounded"
    value={officeFilter}
    onChange={(e) => {
      setOfficeFilter(e.target.value);
      setPage(1);
    }}
  >
    {offices.map((office) => (
      <option key={office || 'all'} value={office || ''}>
        {office ? office : (
          <FormattedMessage
            id="filter.allOffices"
            defaultMessage="Todos os escritórios"
          />
        )}
      </option>
    ))}
  </select>
</div>


    {/* Table */}
    <div className="border rounded">
      <table className="w-full text-sm">
        <thead>
  <tr>
    <th className="p-2">
      <FormattedMessage id="table.name" defaultMessage="Nome" />
    </th>
    <th className="p-2">
      <FormattedMessage id="table.email" defaultMessage="Email" />
    </th>
    <th className="p-2">
      <FormattedMessage id="table.office" defaultMessage="Escritório" />
    </th>
    <th className="p-2">
      <FormattedMessage id="table.assignManager" defaultMessage="Atribuir Gestor" />
    </th>
  </tr>
</thead>

        <tbody>
          {users.map((user) => (
            <tr key={user.id} className="border-t">
              <td className="p-2">{user.firstName} {user.lastName}</td>
              <td className="p-2">{user.email}</td>
              <td className="p-2">{formatWorkplace(user.usualWorkPlace)}</td>
              <td className="p-2">
                {isAdmin && (
                  <div className="flex gap-2 items-center">
                    <select
                      value={selectedManagers[user.email] || ""}
                      onChange={(e) =>
                        setSelectedManagers((prev) => ({
                          ...prev,
                          [user.email]: e.target.value,
                        }))
                      }
                      className="border px-2 py-1 rounded w-40"
                    >
                     <option value="">
  <FormattedMessage id="form.select.placeholder" defaultMessage="-- Selecionar --" />
</option>
                      {dropdownUsers.map((u) => (
                        <option key={u.email} value={u.email}>
                          {u.firstName} {u.lastName}
                        </option>
                      ))}
                    </select>
                    <button
                      type="button"
                      onClick={() => handleAssignManager(user.email)}
                      disabled={
                        updatingManager ||
                        !selectedManagers[user.email] ||
                        selectedManagers[user.email] === user.email
                      }
                      className="bg-red-600 text-white px-3 py-1 rounded disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      <FormattedMessage id="evaluations.form.changeManager" defaultMessage="Alterar" />
                    </button>
                  </div>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {loading && (
        <div className="p-4 text-center text-gray-500">A carregar...</div>
      )}

      {!loading && users.length === 0 && (
        <div className="p-4 text-center text-gray-500">Nenhum utilizador encontrado.</div>
      )}

      {error && (
        <div className="p-4 text-center text-red-600">{error}</div>
      )}
    </div>

    {/* Pagination */}
    {!loading && totalPages > 1 && (
      <div className="mt-4 flex justify-end gap-2 text-blue-700 text-sm">
        {Array.from({ length: totalPages }).map((_, idx) => (
          <button
            key={idx + 1}
            className={`hover:underline ${page === idx + 1 ? "font-bold underline" : ""}`}
            onClick={() => handleGoToPage(idx + 1)}
          >
            {idx + 1}
          </button>
        ))}
        {page < totalPages && (
          <button onClick={() => handleGoToPage(page + 1)}>
            {">"}
          </button>
        )}
      </div>
    )}
  </PageLayout>
);


}

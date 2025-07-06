import { useState, useMemo } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import MessageUserButton from "../components/MessageUserButton";
import { useUsersList } from "../hooks/useUsersList"; 
import { formatWorkplace } from "../utils/formatWorkplace";
import { apiConfig } from "../api/apiConfig";
import { useNavigate } from "react-router-dom";

export default function UsersPage() {
  // Filtros e página
  const [name, setName] = useState("");
  const [office, setOffice] = useState("");
  const [manager, setManager] = useState("");
  const [page, setPage] = useState(1);
  const navigate = useNavigate(); 

    const filters = useMemo(
    () => ({ name, office, manager, page }),
    [name, office, manager, page]
  );

  // Buscar utilizadores com filtros e paginação
  const { users, totalPages, loading, error } = useUsersList(filters);

  // Funções para lidar com filtros
  const handleFilterName = (e) => { setName(e.target.value); setPage(1); };
  const handleFilterOffice = (e) => { setOffice(e.target.value); setPage(1); };
  const handleFilterManager = (e) => { setManager(e.target.value); setPage(1); };
  const handleGoToPage = (p) => setPage(p);

  // Lista de escritórios (pode ser dinâmica!)
  const offices = [
    "", "Boston", "Coimbra", "Lisboa", "Munich", "Porto", "Southampton", "Viseu"
  ];

  const handleExportCSV = async () => {
  try {
    // Lê filtros do state (name, office, manager)
    const params = new URLSearchParams();
    if (name) params.append("profile-name", name);
    if (office) params.append("usual-work-place", office);
    if (manager) params.append("manager-email", manager);

    const url = `${apiConfig.API_ENDPOINTS.profiles.exportUsersCsv}?${params.toString()}`;

    const token = sessionStorage.getItem("authToken");

    // Usa fetch diretamente para blobs
    const response = await fetch(url, {
      headers: {
        sessionToken: token,
        token,
      },
    });
    if (!response.ok) throw new Error("Erro ao exportar CSV");
    const blob = await response.blob();
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = "users_export.csv";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (err) {
    alert("Erro ao exportar ficheiro.");
  }
};


  return (
    <PageLayout title={<FormattedMessage id="users.list.title" defaultMessage="Listagem de Utilizadores" />}>
      {/* Filtros */}
      <div className="flex gap-4 mb-4">
        <FormattedMessage id="users.filter.name" defaultMessage="Nome">
          {(msg) => (
            <input
              placeholder={msg}
              className="border px-2 py-1 rounded"
              value={name}
              onChange={handleFilterName}
            />
          )}
        </FormattedMessage>
        <select
          className="border px-2 py-1 rounded"
          value={office}
          onChange={handleFilterOffice}
        >
          {offices.map((officeOption) => (
            <option key={officeOption} value={officeOption}>
              {officeOption}
            </option>
          ))}
        </select>
        <FormattedMessage id="users.filter.manager" defaultMessage="Gestor">
          {(msg) => (
            <input
              placeholder={msg}
              className="border px-2 py-1 rounded"
              value={manager}
              onChange={handleFilterManager}
            />
          )}
        </FormattedMessage>
        <button className="bg-green-600 text-white px-3 rounded" 
        onClick={handleExportCSV}
        >
          <FormattedMessage id="users.button.excel" defaultMessage="Excel" />
        </button>
      </div>

      {/* Loading/Error */}
      {loading && <div className="py-8 text-center text-gray-500">A carregar...</div>}
      {error && <div className="py-8 text-center text-red-600">{error}</div>}

      {/* Tabela */}
      {!loading && users.length > 0 && (
        <div className="overflow-x-auto w-full">
        <table className="min-w-full text-left border-collapse table-auto">
          <thead>
            <tr className="bg-gray-200 text-sm">
              <th className="p-2 w-[180px]">
                <FormattedMessage id="users.table.name" defaultMessage="Nome" />
              </th>
              <th className="p-2 w-[140px]">
                <FormattedMessage id="users.table.office" defaultMessage="Escritório" />
              </th>
              <th className="p-2 w-[180px]">
                <FormattedMessage id="users.table.manager" defaultMessage="Gestor" />
              </th>
              <th className="p-2 w-[220px]">
                <FormattedMessage id="users.table.contact" defaultMessage="Contacto" />
              </th>
              <th className="p-2 w-[100px]"></th>
              <th className="p-2 w-[60px]"></th>
              <th className="p-2 w-[200px]">
                <FormattedMessage id="users.table.actions" defaultMessage="Ações" />
              </th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="border-b hover:bg-gray-50">
                <td className="p-2">{user.name}</td>
                <td className="p-2">{formatWorkplace(user.office)}</td>
                <td className="p-2">{user.manager}</td>
                <td className="p-2 truncate">{user.email}</td>
                <td className="p-2 pl-14">
                  <img
                    src={user.avatar || "/default_avatar.png"}
                    alt={user.name}
                    className="w-8 h-8 rounded-full"
                  />
                </td>
                <td className="p-2 text-center pl-14">
                  <MessageUserButton userId={user.id} />
                </td>
<td className="p-2 text-center pl-16">
<button
  onClick={() => navigate(`/profile/${user.id}`)}
  className="bg-[#D41C1C] text-white px-3 py-1 rounded flex items-center gap-2 ml-6"
>
  <FormattedMessage id="users.button.view" defaultMessage="Ver" /> <span>&gt;</span>
</button>
</td>
              </tr>
            ))}
          </tbody>
        </table>
        </div>
      )}

      {/* Paginação real */}
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
      {!loading && users.length === 0 && (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="users.table.empty" defaultMessage="Nenhum utilizador encontrado com estes filtros." />
        </div>
      )}
    </PageLayout>
  );
}


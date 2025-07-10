import { useState, useMemo, useEffect } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import MessageUserButton from "../components/MessageUserButton";
import { useUsersList } from "../hooks/useUsersList";
import { formatWorkplace } from "../utils/formatWorkplace";
import { apiConfig } from "../api/apiConfig";
import { useNavigate } from "react-router-dom";
import { AppTable } from "../components/AppTable";
import { profileAPI } from "../api/profileAPI";
import profileIcon from "../images/profile_icon.png";
import AppButton from "../components/AppButton";

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
  console.log("UsersPage renderizou!", { name, office, manager, page });

  function AvatarCell({ avatar, name }) {
    const [src, setSrc] = useState(
      avatar && avatar.trim() !== ""
        ? profileAPI.getPhoto(avatar)
        : profileIcon
    );
  
    return (
      <img
        src={src}
        alt={name}
        className="w-8 h-8 rounded-full object-cover ml-16"
        onError={() => setSrc("/default_avatar.png")}
      />
    );
  }

  // Definição das colunas para a tabela
  const columns = [
    {
      header: <FormattedMessage id="users.table.name" defaultMessage="Nome" />,
      accessor: "name",
      className: "w-[180px]",
    },
    {
      header: <FormattedMessage id="users.table.office" defaultMessage="Escritório" />,
      accessor: (u) => formatWorkplace(u.office),
      className: "w-[140px]",
    },
    {
      header: <FormattedMessage id="users.table.manager" defaultMessage="Gestor" />,
      accessor: "manager",
      className: "w-[180px]",
    },
    {
      header: <FormattedMessage id="users.table.contact" defaultMessage="Contacto" />,
      accessor: "email",
      className: "w-[220px] truncate",
    },
{
  header: "",
  accessor: (u) => <AvatarCell avatar={u.avatar} name={u.name} />,
  className: "w-[100px] flex items-center",
},
    {
      header: <FormattedMessage id="users.table.actions" defaultMessage="Ações" />,
      accessor: null,
      render: (user) => (
        <div className="flex flex-row items-center gap-2 justify-center">
          <MessageUserButton userId={user.id} />
          <button
            onClick={() =>
              navigate(`/profile/${user.id}`, { state: { profileOwnerEmail: user.email } })
            }
            className="bg-[#D41C1C] text-white px-3 py-1 rounded flex items-center gap-2"
          >
            <FormattedMessage id="users.button.view" defaultMessage="Ver" /> <span>&gt;</span>
          </button>
        </div>
      ),
      className: "w-[200px] text-center pr-8",
      colSpan: 2, // Podes remover se não estiveres a usar colSpan na tabela base
    },
  ];

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
        <AppButton
          variant="excel"
          onClick={handleExportCSV}
        >
          <FormattedMessage id="users.button.excel" defaultMessage="Excel" />
        </AppButton>
      </div>

      {/* Loading/Error */}
      {loading && <div className="py-8 text-center text-gray-500">A carregar...</div>}
      {error && <div className="py-8 text-center text-red-600">{error}</div>}

      {/* Tabela */}
      {!loading && users.length > 0 && (
        <div>
          <AppTable
            columns={columns}
            data={users}
            loading={loading}
            emptyMessage={<FormattedMessage id="users.table.noResults" defaultMessage="Nenhum utilizador encontrado." />}
          />
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



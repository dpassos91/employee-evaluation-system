import { useState, useMemo } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import MessageUserButton from "../components/MessageUserButton";
import { useUsersEvaluationList } from "../hooks/useUsersEvaluationList"; 

import { apiConfig } from "../api/apiConfig";
import { useNavigate } from "react-router-dom";




export default function EvaluationListPage() {
// Filtros e página
  const [name, setName] = useState("");
  const [evaluationState, setEvaluationState] = useState("");
  const [grade, setGrade] = useState("");
  const [cycleEnd, setCycleEnd] = useState("");
  const [page, setPage] = useState(1);
  const navigate = useNavigate(); 

    const filters = useMemo(
    () => ({ name, evaluationState, grade,  cycleEndDate: cycleEnd, page }),
    [name, evaluationState, grade, cycleEnd, page]
  );

  // Buscar utilizadores com filtros e paginação
    const { evaluations, totalPages, loading, error } = useUsersEvaluationList(filters);

 // Funções para lidar com filtros
  const handleFilterName = (e) => { setName(e.target.value); setPage(1); };
  const handleFilterEvaluationState = (e) => { setEvaluationState(e.target.value); setPage(1); };
  const handleFilterGrade = (e) => { setGrade(e.target.value); setPage(1); };
  const handleFilterCycleEnd = (e) => { setCycleEnd(e.target.value); setPage(1); };
  const handleGoToPage = (p) => setPage(p);

// Lista de estados de avaliação (pode ser dinâmica!)
   const evaluationStates = ["", "IN_EVALUATION", "EVALUATED", "CLOSED"];



const handleExportCSV = async () => {
  try {
    // Lê filtros do state (name, office, manager)
    const params = new URLSearchParams();
    if (name) params.append("name", name); 
    if (evaluationState) params.append("state", evaluationState);
    if (grade) params.append("grade", grade);
    if (cycleEnd) params.append("cycleEnd", cycleEnd);

    const url = `${apiConfig.API_ENDPOINTS.evaluations.exportCsv}?${params.toString()}`;

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

// botão para Fechar avaliações em bulk se for um admin



  return (
    <PageLayout title={<FormattedMessage id="evaluations.list.title" defaultMessage="Listagem de Avaliações" />}>
      {/* Filtros */}
<div className="flex gap-4 mb-4">
        <FormattedMessage id="evaluations.filter.name" defaultMessage="Nome">
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
  value={evaluationState}
  onChange={handleFilterEvaluationState}
>
  <option value="">
    <FormattedMessage id="evaluations.filter.state.state" defaultMessage="Estado" />
  </option>
  <option value="IN_EVALUATION">
    <FormattedMessage id="evaluation.state.IN_EVALUATION" defaultMessage="Em Avaliação" />
  </option>
  <option value="EVALUATED">
    <FormattedMessage id="evaluation.state.EVALUATED" defaultMessage="Concluído" />
  </option>
  <option value="CLOSED">
    <FormattedMessage id="evaluation.state.CLOSED" defaultMessage="Fechado" />
  </option>
</select>
        <FormattedMessage id="evaluations.filter.grade" defaultMessage="Nota">
          {(msg) => (
            <input
              placeholder={msg}
              className="border px-2 py-1 rounded"
              value={grade}
              onChange={handleFilterGrade}
            />
          )}
        </FormattedMessage>
        <button className="bg-green-600 text-white px-3 rounded" 
        onClick={handleExportCSV}
        >
          <FormattedMessage id="users.button.excel" defaultMessage="Excel CSV" />
        </button>
      </div>




      {/* Loading/Error */}
      {loading && <div className="py-8 text-center text-gray-500">A carregar...</div>}
      {error && <div className="py-8 text-center text-red-600">{error}</div>}


      {/* Tabela de avaliações */}
{!loading && evaluations.length > 0 && (
        <div className="overflow-x-auto w-full">
        <table className="min-w-full text-left border-collapse table-auto">
          <thead>
            <tr className="bg-gray-200 text-sm">
              <th className="p-2 w-[180px]">
                <FormattedMessage id="evaluations.table.photo" defaultMessage="Fotografia" />
              </th>
               <th className="p-2">
        <FormattedMessage id="evaluations.table.name" defaultMessage="Nome" />
      </th>
      <th className="p-2">
        <FormattedMessage id="evaluations.table.state" defaultMessage="Estado" />
      </th>
            </tr>
          </thead>
          <tbody>
{evaluations.map((evaluation) => (
      <tr key={evaluation.id} className="border-b hover:bg-gray-50">
        <td className="p-2">
          <img
            src={evaluation.avatar || "/default_avatar.png"}
            alt={evaluation.evaluated}
            className="w-8 h-8 rounded-full"
          />
        </td>
        <td className="p-2">{evaluation.evaluated}</td>
        <td className="p-2">
          <FormattedMessage
            id={`evaluation.state.${evaluation.state}`}
            defaultMessage={evaluation.state}
          />
        </td>
      </tr>
    ))}
  </tbody>
</table>      {/* Juntar os botões num só <td> 
      <td className="p-2 text-center pr-8" colSpan={2}>
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
      </td>*/}
    
        
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
      {!loading && evaluations.length === 0 && (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="users.table.empty" defaultMessage="Nenhuma avaliação encontrada com estes filtros." />
        </div>
      )}








          </PageLayout>
          );
        }


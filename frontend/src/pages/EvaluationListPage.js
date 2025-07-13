import { useState, useMemo } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import { useUsersEvaluationList } from "../hooks/useUsersEvaluationList"; 
import { userStore } from "../stores/userStore";
import { apiConfig } from "../api/apiConfig";
import { useNavigate } from "react-router-dom";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";




export default function EvaluationListPage() {
// Filters and page
  const [name, setName] = useState("");
  const [evaluationState, setEvaluationState] = useState("");
  const [grade, setGrade] = useState("");
  const [cycleEnd, setCycleEnd] = useState("");
  const [hover, setHover] = useState(null);
  const [page, setPage] = useState(1);
  const navigate = useNavigate(); 


/**
   * Authenticated user info from Zustand store.
   */
  const user = userStore((state) => state.user);

  // Checks if the current user is an admin
  const isAdmin = user?.role === "ADMIN";

    const filters = useMemo(
    () => ({ name, evaluationState, grade,  cycleEndDate: cycleEnd, page }),
    [name, evaluationState, grade, cycleEnd, page]
  );

  // Get users with filters and pagination
    const { evaluations, totalPages, loading, error, refetch  } = useUsersEvaluationList(filters);

 // Functions to handle filters
  const handleFilterName = (e) => { setName(e.target.value); setPage(1); };
  const handleFilterEvaluationState = (e) => { setEvaluationState(e.target.value); setPage(1); };
  const handleFilterGrade = (e) => { setGrade(e.target.value); setPage(1); };
  const handleFilterCycleEnd = (e) => { setCycleEnd(e.target.value); setPage(1); };
  const handleGoToPage = (p) => setPage(p);

// Evaluation States 
   const evaluationStates = ["", "IN_EVALUATION", "EVALUATED", "CLOSED"];


// Functions to change evaluation states and lead to the evaluation page

const handleCloseEvaluation = async (id) => {
  const toastId = toast.loading("A fechar avaliação...");
  try {
    await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.close(id), {
      method: "PUT",
    });

    toast.update(toastId, {
      render: "Avaliação fechada com sucesso.",
      type: "success",
      isLoading: false,
      autoClose: 3000,
    });

    refetch();
  } catch (err) {
    toast.update(toastId, {
      render: "Erro ao fechar avaliação.",
      type: "error",
      isLoading: false,
      autoClose: 3000,
    });
  }
};

const handleReopenEvaluation = async (id) => {
  const toastId = toast.loading("A reabrir avaliação...");
  try {
    await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.reopen(id), {
      method: "PUT",
    });

    toast.update(toastId, {
      render: "Avaliação reaberta com sucesso.",
      type: "success",
      isLoading: false,
      autoClose: 3000,
    });

    refetch();
  } catch (err) {
    toast.update(toastId, {
      render: "Erro ao reabrir avaliação.",
      type: "error",
      isLoading: false,
      autoClose: 3000,
    });
  }
};

const handleCloseAllEvaluations = async () => {
  if (!window.confirm("Tens a certeza que queres fechar todos os processos?")) return;

  const toastId = toast.loading("A fechar todos os processos...");

  try {
    await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.bulkClose, {
      method: "PUT",
    });

    toast.update(toastId, {
      render: "Todos os processos foram fechados com sucesso.",
      type: "success",
      isLoading: false,
      autoClose: 3000,
    });

    refetch();
  } catch (err) {
    toast.update(toastId, {
      render: "Erro ao fechar os processos.",
      type: "error",
      isLoading: false,
      autoClose: 3000,
    });
  }
};

const handleFillEvaluation = (email) => {
  navigate(`/evaluationform/${email}`);
};




const handleExportCSV = async () => {
  try {
    // Loads fields from state (name, office, manager)
    const params = new URLSearchParams();
    if (name) params.append("name", name); 
    if (evaluationState) params.append("state", evaluationState);
    if (grade) params.append("grade", grade);
    if (cycleEnd) params.append("cycleEnd", cycleEnd);

    const url = `${apiConfig.API_ENDPOINTS.evaluations.exportCsv}?${params.toString()}`;

    const token = sessionStorage.getItem("authToken");

    // Apply fetch directly for blobs
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
    <PageLayout title={<FormattedMessage id="evaluations.list.title" defaultMessage="Listagem de Avaliações" />}>
      {/* Filters */}
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
       <div className="flex flex-col items-start">
  <label className="text-sm mb-1 font-medium text-gray-700">
    <FormattedMessage id="evaluations.filter.grade" defaultMessage="Avaliação" />
  </label>
  <div className="flex items-center gap-1">
    {[1, 2, 3, 4].map((star) => (
  <button
    key={star}
    type="button"
    onClick={() => {
      setGrade(grade === String(star) ? "" : String(star));
      setPage(1);
    }}
    onMouseEnter={() => setHover(star)}
    onMouseLeave={() => setHover(null)}
    className="text-yellow-500 text-xl focus:outline-none"
    aria-label={`Nota ${star}`}
  >
    {star <= ((hover ?? Number(grade)) || 0) ? "★" : "☆"}
  </button>
))}
  </div>
</div>
<div className="flex flex-col">
  <label className="text-sm font-bold text-gray-700 mb-1">
    <FormattedMessage id="evaluations.filter.cycleEnd" defaultMessage="Data fim de ciclo" />
  </label>
  <input
    type="date"
    value={cycleEnd || ""}
    onChange={handleFilterCycleEnd}
    className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
  />
</div>



        <button className="bg-green-600 text-white px-3 rounded" 
        onClick={handleExportCSV}
        >
          <FormattedMessage id="users.button.excel" defaultMessage="Excel CSV" />
        </button>
      </div>




      {/* Loading/Error */}
      {loading && (
  <div className="py-8 text-center text-gray-500">
    <FormattedMessage id="table.loading" defaultMessage="A carregar..." />
  </div>
)}
      {error && <div className="py-8 text-center text-red-600">{error}</div>}


      {/* Evaluation Table */}
{!loading && evaluations.length > 0 && (
        <div className="overflow-x-auto w-full">
        <table className="min-w-full text-left border-collapse table-auto">
          <thead>
  <tr className="bg-gray-200 text-sm">
    <th className="p-2 w-[100px]">
      <FormattedMessage id="evaluations.table.photo" defaultMessage="Fotografia" />
    </th>
    <th className="p-2">
      <FormattedMessage id="evaluations.table.evaluated" defaultMessage="Avaliado" />
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
          className="w-10 h-10 rounded-full object-cover"
        />
      </td>
      <td className="p-2 font-medium">{evaluation.evaluated}</td>
      <td className="p-2">
        <FormattedMessage
          id={`evaluation.state.${evaluation.state}`}
          defaultMessage={evaluation.state}
        />
      </td>

      {/* Actions */}
      <td className="p-2 text-center">
        <div className="flex flex-row gap-2 justify-center">
  {/* Show "Fill" only if at IN_EVALUATION */}
  {evaluation.state === "IN_EVALUATION" ? (
    <button
      onClick={() => handleFillEvaluation(evaluation.email)}
      className="bg-[#D41C1C] text-white px-3 py-1 rounded"
    >
      <FormattedMessage id="evaluation.button.fill" defaultMessage="Preencher" />
    </button>
  ) : (
    <>
      {/* Show only if it's not at IN_EVALUATION */}
      <button
        onClick={() => navigate(`/evaluationform/${evaluation.email}`)}
        className="bg-[#D41C1C] text-white px-3 py-1 rounded"
      >
        <FormattedMessage id="evaluation.button.view" defaultMessage="Ver" /> <span>&gt;</span>
      </button>

      {/* Close if at EVALUATED */}
      {evaluation.state === "EVALUATED" && (
        <button
          onClick={() => handleCloseEvaluation(evaluation.id)}
          className="bg-[#D41C1C] text-white px-3 py-1 rounded"
        >
          <FormattedMessage id="evaluation.button.close" defaultMessage="Fechar" />
        </button>
      )}

      {/* Revert if in EVALUATED */}
      {evaluation.state === "EVALUATED" && (
        <button
          onClick={() => handleReopenEvaluation(evaluation.id)}
          className="bg-[#D41C1C] text-white px-3 py-1 rounded"
        >
          <FormattedMessage id="evaluation.button.revert" defaultMessage="Reverter" />
        </button>
      )}
    </>
  )}
</div>
      </td>
    </tr>
  ))}
</tbody>

</table>      
        
        </div>
      )}

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
      {!loading && evaluations.length === 0 && (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="evaluations.table.empty" defaultMessage="Nenhuma avaliação encontrada com estes filtros." />
        </div>
      )}


{/*  show the bulk close button only if the user is admin and all evaluations are at evaluated-state*/}
{isAdmin && evaluations.length > 0 && evaluations.every((e) => e.state === "EVALUATED") && (
  <div className="flex justify-center mt-10">
    <button
      onClick={handleCloseAllEvaluations}
      className="bg-[#D41C1C] text-white font-bold px-6 py-3 rounded shadow hover:shadow-md transition"
    >
      <FormattedMessage
        id="evaluations.button.closeAll"
        defaultMessage="Fechar Todos os Processos"
      />
    </button>
  </div>
)}


<ToastContainer position="top-center" autoClose={3000} />


          </PageLayout>
          );
        }


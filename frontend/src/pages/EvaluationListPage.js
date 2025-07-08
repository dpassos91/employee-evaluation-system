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
  const [cycleEnd, setcycleEnd] = useState("");
  const [page, setPage] = useState(1);
  const navigate = useNavigate(); 

    const filters = useMemo(
    () => ({ name, evaluationState, grade, page }),
    [name, evaluationState, grade, page]
  );

  // Buscar utilizadores com filtros e paginação
    const { evaluations, totalPages, loading, error } = useUsersEvaluationList(filters);

 // Funções para lidar com filtros
  const handleFilterName = (e) => { setName(e.target.value); setPage(1); };
  const handleFilterEvaluationState = (e) => { setEvaluationState(e.target.value); setPage(1); };
  const handleFilterGrade = (e) => { setGrade(e.target.value); setPage(1); };
  const handleFilterCycleEnd = (e) => { setcycleEnd(e.target.value); setPage(1); };
  const handleGoToPage = (p) => setPage(p);

// Lista de estados de avaliação (pode ser dinâmica!)
  const states = [
    "", "In Evaluation", "Evaluated", "Closed"
  ];

const handleExportCSV = async () => {
  try {
    // Lê filtros do state (name, office, manager)
    const params = new URLSearchParams();
    if (name) params.append("profile-name", name);
    if (evaluationState) params.append("evaluation-state", evaluationState);
    if (grade) params.append("grade", grade);

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
    <PageLayout
       title={<FormattedMessage id="users.list.title" defaultMessage="Listagem de Avaliações" />}
    >




          </PageLayout>
          );
        }


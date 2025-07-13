import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import AppForm from "../components/AppForm";
import { FormattedMessage } from "react-intl";
import { toast } from "react-toastify";
import { evaluationCycleAPI } from "../api/evaluationCycleAPI";
import { userStore } from "../stores/userStore";

export default function NewEvaluationCyclePage() {
  const navigate = useNavigate();
  const [endDate, setEndDate] = useState("");
  const [usersWithoutManager, setUsersWithoutManager] = useState(0);
  const [openEvaluations, setOpenEvaluations] = useState(0);
  const [loading, setLoading] = useState(false);

  const user = userStore((state) => state.user);
  const isAdmin = user?.role === "ADMIN";

  useEffect(() => {
    if (!isAdmin) {
      toast.error("Apenas administradores podem aceder a esta página.");
      navigate("/dashboard");
      return;
    }

   const fetchStats = async () => {
  try {
    const token = sessionStorage.getItem("authToken");

    const [noManagerRes, openCyclesRes] = await Promise.all([
      evaluationCycleAPI.getUsersWithoutManager(token),
      evaluationCycleAPI.getIncompleteEvaluations(token),
    ]);

    setUsersWithoutManager(noManagerRes.numberOfUsersWithoutManager || 0);
    setOpenEvaluations(openCyclesRes.totalUsersWithIncompleteEvaluations || 0);
  } catch (error) {
    toast.error("Erro ao carregar dados do ciclo.");
  }
};


    fetchStats();
  }, [isAdmin, navigate]);

  const handleSubmit = async () => {
    if (!endDate) {
      toast.error("Por favor, seleciona uma data de fim.");
      return;
    }

    setLoading(true);
    try {
      await evaluationCycleAPI.createCycle({ endDate }, sessionStorage.getItem("authToken"));
      toast.success("Novo ciclo iniciado com sucesso!");
      navigate("/evaluationlist");
    } catch (error) {
      toast.error("Erro ao iniciar novo ciclo.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <PageLayout
      title={
        <FormattedMessage
          id="cycle.new.title"
          defaultMessage="Novo Ciclo de Avaliação"
        />
      }
    >
      <AppForm onSubmit={handleSubmit} isLoading={loading} actions={[
        {
          label: <FormattedMessage id="button.start" defaultMessage="Iniciar Novo Ciclo" />,
          type: "submit",
          loading,
        },
      ]}>
        {/* Cycle end date */}
        <div>
          <label className="block text-sm font-bold mb-1">
            <FormattedMessage
              id="cycle.endDate"
              defaultMessage="Data de fim do Novo Ciclo"
            />
          </label>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
            className="border border-gray-300 rounded px-3 py-2 w-full"
          />
        </div>

     {/* Users without a manager */}
<div className="mt-4 text-md">
  <span className="inline-flex items-center">
    <FormattedMessage
      id="cycle.noManager"
      defaultMessage="Utilizadores sem Gestor"
    />: <strong className="ml-1">{usersWithoutManager}</strong>
    {usersWithoutManager > 0 && (
      <button
        onClick={() => navigate("/users-without-manager")}
        className="ml-3 mr-4 bg-gray-200 hover:bg-gray-300 text-sm text-gray-700 font-medium py-1 px-3 rounded shadow-sm transition"
      >
        <FormattedMessage id="button.viewDetails" defaultMessage="Ver Detalhes" />
      </button>
    )}
  </span>
</div>

{/* Open evaluation proceses */}
<div className="mt-2 text-md">
  <span className="inline-flex items-center">
    <FormattedMessage
      id="cycle.openEvaluations"
      defaultMessage="Processos em aberto do ciclo anterior"
    />: <strong className="ml-1">{openEvaluations}</strong>
    {openEvaluations > 0 && (
      <button
        onClick={() => navigate("/incomplete-evaluations")}
        className="ml-3 mr-4 bg-gray-200 hover:bg-gray-300 text-sm text-gray-700 font-medium py-1 px-3 rounded shadow-sm transition"
      >
        <FormattedMessage id="button.viewDetails" defaultMessage="Ver Detalhes" />
      </button>
    )}
  </span>
</div>


      </AppForm>
    </PageLayout>
  );
}

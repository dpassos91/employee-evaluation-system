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
       const [noManagerRes, openCyclesRes] = await Promise.all([
          // Podes criar helpers em evaluationCycleAPI depois
          // Por agora mantemos como estava
          fetch("/grupo7/rest/evaluation-cycles/users-without-manager", {
            headers: { sessionToken: sessionStorage.getItem("authToken") },
          }).then(r => r.json()),
          fetch("/grupo7/rest/evaluation-cycles/incomplete-evaluations", {
            headers: { sessionToken: sessionStorage.getItem("authToken") },
          }).then(r => r.json()),
        ]);

        setUsersWithoutManager(noManagerRes.count || 0);
        setOpenEvaluations(openCyclesRes.count || 0);
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
        {/* Data de fim do ciclo */}
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

        {/* Utilizadores sem gestor */}
        <div className="mt-4">
          <span className="block text-md">
            <FormattedMessage
              id="cycle.noManager"
              defaultMessage="Utilizadores sem Gestor"
            />: <strong>{usersWithoutManager}</strong>
          </span>
        </div>

        {/* Processos abertos */}
        <div className="mt-2">
          <span className="block text-md">
            <FormattedMessage
              id="cycle.openEvaluations"
              defaultMessage="Processos em aberto do ciclo anterior"
            />: <strong>{openEvaluations}</strong>
          </span>
        </div>
      </AppForm>
    </PageLayout>
  );
}

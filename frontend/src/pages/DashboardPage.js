import { useEffect, useState } from "react";
import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import { getDashboardSummary } from "../api/dashboardAPI";
import { useIntl, FormattedMessage } from "react-intl";
import {
  FaBook,
  FaFileAlt,
  FaCalendarCheck,
  FaClock,
  FaSearch,  
  FaCog,
  FaUsers,
  FaListAlt,
  FaUserShield,
  FaBell,
} from "react-icons/fa";
import Spinner from "../components/Spinner"; // Assume que tens um spinner
import { userStore } from "../stores/userStore";

export default function DashboardPage() {
  const { formatMessage } = useIntl();
  const user = userStore((state) => state.user); // Assume que o user tem role, nome, etc.

  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  // Para feedback elegante em caso de erro
  const [error, setError] = useState(null);

useEffect(() => {
  setLoading(true);
  getDashboardSummary()
    .then((res) => {
      console.log("DASHBOARD RESPONSE:", res);
      setDashboard(res);
    })
    .catch((err) => {
      console.error("DASHBOARD ERROR:", err);
      setError("Erro ao carregar dashboard: " + (err?.message || err));
    })
    .finally(() => setLoading(false));
}, []);

  if (loading) return <div className="flex justify-center items-center min-h-[300px]"><Spinner /></div>;
  if (error) return <div className="text-red-600 text-center py-10">{error}</div>;
  if (!dashboard) return null;

  // Role-awareness (podes afinar com base em userStore)
  const isManager = user?.role?.toUpperCase() === "MANAGER";
  const isAdmin = user?.role?.toUpperCase() === "ADMIN";
  const userName = user?.firstName && user?.lastName
  ? `${user.firstName} ${user.lastName}`
  : user?.email;

  console.log("DashboardPage render:", { user, userName });
  return (
    <PageLayout
      title={
        <span>
          <FormattedMessage id="dashboard.greeting" defaultMessage="Olá, {name} 👋" values={{ name: userName }} />
        </span>
      }
      subtitle={
        <FormattedMessage id="dashboard.subtitle" defaultMessage="Pronto para continuar a crescer?" />
      }
    >
      {/* Cards Section */}
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
  {/* Sempre visíveis para qualquer utilizador */}
  <DashboardCard
    icon={<FaBook className="text-blue-600 mr-2" size={26} />}
    label={formatMessage({ id: "dashboard.cards.activeTrainings", defaultMessage: "Formações ativas" })}
    value={dashboard.activeTrainings}
    subLabel={formatMessage({ id: "dashboard.cards.trainingsOngoing", defaultMessage: "em curso" })}
  />

  <DashboardCard
    icon={<FaFileAlt className="text-red-600 mr-2" size={26} />}
    label={formatMessage({ id: "dashboard.cards.pendingEvaluations", defaultMessage: "Avaliações em aberto" })}
    value={dashboard.pendingEvaluations}
    subLabel={formatMessage({ id: "dashboard.cards.evaluationsToFill", defaultMessage: "por preencher" })}
  />

  <DashboardCard
    icon={<FaCalendarCheck className="text-green-600 mr-2" size={26} />}
    label={formatMessage({ id: "dashboard.cards.lastEvaluation", defaultMessage: "Última avaliação" })}
    value={
      dashboard.lastEvaluationDate
        ? new Date(dashboard.lastEvaluationDate).toLocaleDateString()
        : formatMessage({ id: "dashboard.cards.noEvaluation", defaultMessage: "Sem registo" })
    }
    subLabel=""
  />

  <DashboardCard
    icon={<FaClock className="text-yellow-600 mr-2" size={26} />}
    label={formatMessage({ id: "dashboard.cards.totalTrainingHours", defaultMessage: "Total de horas de formação" })}
    value={dashboard.totalTrainingHours + "h"}
    subLabel={formatMessage({ id: "dashboard.cards.trainingHours", defaultMessage: "acumuladas este ano" })}
  />

  {/* Só para MANAGER */}
  {isManager && (
    <>
      <DashboardCard
        icon={<FaUsers className="text-purple-600 mr-2" size={26} />}
        label={formatMessage({ id: "dashboard.cards.teamSize", defaultMessage: "Colaboradores em equipa" })}
        value={dashboard.teamSize}
        subLabel=""
      />
      <DashboardCard
        icon={<FaListAlt className="text-fuchsia-600 mr-2" size={26} />}
        label={formatMessage({ id: "dashboard.cards.teamPendingEvaluations", defaultMessage: "Avaliações da equipa em aberto" })}
        value={dashboard.teamPendingEvaluations}
        subLabel=""
      />
    </>
  )}

  {/* Só para ADMIN */}
  {isAdmin && (
    <>
      <DashboardCard
        icon={<FaUserShield className="text-gray-700 mr-2" size={26} />}
        label={formatMessage({ id: "dashboard.cards.totalUsers", defaultMessage: "Utilizadores totais" })}
        value={dashboard.totalUsers}
        subLabel=""
      />
      <DashboardCard
        icon={<FaBell className="text-pink-600 mr-2" size={26} />}
        label={formatMessage({ id: "dashboard.cards.totalPendingEvaluations", defaultMessage: "Avaliações pendentes (global)" })}
        value={dashboard.totalPendingEvaluations}
        subLabel=""
      />
    </>
  )}
</div>

      {/* Notifications section */}
      {dashboard.notifications && dashboard.notifications.length > 0 && (
        <div className="mb-8">
          <h3 className="font-semibold text-lg mb-2 flex items-center">
            <FaBell className="mr-2" />
            <FormattedMessage id="dashboard.notifications.title" defaultMessage="Notificações importantes" />
          </h3>
          <ul className="bg-white rounded-xl shadow divide-y border">
            {dashboard.notifications.map((n, idx) => (
              <li key={idx} className="flex items-center gap-4 p-4">
                <span
                  className={`rounded-full h-3 w-3 ${
                    n.type === "WARNING" ? "bg-yellow-400" : n.type === "ACTION" ? "bg-red-400" : "bg-blue-400"
                  }`}
                  title={n.type}
                />
                <span className={n.read ? "text-gray-400" : "font-medium"}>
                  {n.message}
                </span>
                <span className="ml-auto text-xs text-gray-500">{n.date && new Date(n.date).toLocaleString()}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* Bottom Buttons */}
      <div className="flex flex-wrap gap-6 justify-center">
        <AppButton variant="primary">
          <FaSearch className="mr-2" />
          <FormattedMessage id="dashboard.buttons.viewTrainings" defaultMessage="Ver formações" />
        </AppButton>
        <AppButton variant="secondary">
          <FaFileAlt className="mr-2" />
          <FormattedMessage id="dashboard.buttons.viewEvaluations" defaultMessage="Consultar avaliações" />
        </AppButton>
        <AppButton variant="secondary">
          <FaCog className="mr-2" />
          <FormattedMessage id="dashboard.buttons.settings" defaultMessage="Definições da aplicação" />
        </AppButton>
      </div>
    </PageLayout>
  );
}

function DashboardCard({ icon, label, value, subLabel }) {
  return (
    <div className="bg-white rounded-2xl shadow-md hover:shadow-lg transition-all duration-200 p-6 flex flex-col items-start gap-2 min-h-[110px]">
      <div className="flex items-center text-lg">{icon}{label}</div>
      <div className="text-3xl font-extrabold mt-2">{value}</div>
      {subLabel && <div className="text-xs text-gray-400">{subLabel}</div>}
    </div>
  );
}






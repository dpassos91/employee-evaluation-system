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
import React from "react";
import { useNavigate, useParams } from "react-router-dom";

export default function DashboardPage() {
  const { formatMessage } = useIntl();
  const user = userStore((state) => state.user); // Assume que o user tem role, nome, etc.

  const navigate = useNavigate();

  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

    const { userId } = useParams();

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
  const isUser = user?.role.toUpperCase() === "USER";
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
<div className="flex flex-col gap-4 w-full">
  {/* Primeira linha: sempre 4 cards */}
  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
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
  </div>

  {/* Segunda linha: só para MANAGER, centrada */}
  {isManager && (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="col-start-2 col-span-1">
        <DashboardCard
          icon={<FaUsers className="text-purple-600 mr-2" size={26} />}
          label={formatMessage({ id: "dashboard.cards.teamSize", defaultMessage: "Colaboradores em equipa" })}
          value={dashboard.teamSize}
          subLabel=""
        />
      </div>
      <div className="col-span-1">
        <DashboardCard
          icon={<FaListAlt className="text-fuchsia-600 mr-2" size={26} />}
          label={formatMessage({ id: "dashboard.cards.teamPendingEvaluations", defaultMessage: "Avaliações da equipa em aberto" })}
          value={dashboard.teamPendingEvaluations}
          subLabel=""
        />
      </div>
    </div>
  )}

  {/* Segunda linha: só para ADMIN, centrada (caso se aplique) */}
  {isAdmin && (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div className="col-start-2 col-span-1">
        <DashboardCard
          icon={<FaUserShield className="text-gray-700 mr-2" size={26} />}
          label={formatMessage({ id: "dashboard.cards.totalUsers", defaultMessage: "Utilizadores totais" })}
          value={dashboard.totalUsers}
          subLabel=""
        />
      </div>
      <div className="col-span-1">
        <DashboardCard
          icon={<FaBell className="text-pink-600 mr-2" size={26} />}
          label={formatMessage({ id: "dashboard.cards.totalPendingEvaluations", defaultMessage: "Avaliações pendentes (global)" })}
          value={dashboard.totalPendingEvaluations}
          subLabel=""
        />
      </div>
    </div>
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
<div className="flex flex-wrap gap-6 justify-center mt-16">
  {/* User */}
  {isUser && (
    <>
      <AppButton
        variant="secondary"
        onClick={() => navigate(`/profile/${user.id}/evaluationhistory`)}
      >
        <FaSearch className="mr-2" />
        <FormattedMessage
          id="dashboard.buttons.evaluationsHistory"
          defaultMessage="Histórico de avaliações"
        />
      </AppButton>

      <AppButton
        variant="primary"
        onClick={() => navigate(`/profile/${user.id}/courseshistory`)}
      >
        <FaSearch className="mr-2" />
        <FormattedMessage
          id="dashboard.buttons.coursesHistory"
          defaultMessage="Histórico de formações"
        />
      </AppButton>
    </>
  )}


  {/* Só managers e admins */}
{(isManager || isAdmin) && (
  <>
    <AppButton variant="secondary" onClick={() => navigate("/teamCourses")}>
      <FaSearch className="mr-2" />
      <FormattedMessage id="dashboard.buttons.teamTrainings" defaultMessage="Formações da equipa" />
    </AppButton>

    <AppButton variant="primary" onClick={() => {
      if (isAdmin) {
        navigate("/evaluations");
      } else if (isManager) {
        navigate("/evaluationlist");
      }
    }}>
      <FaFileAlt className="mr-2" />
      <FormattedMessage id="dashboard.buttons.teamEvaluations" defaultMessage="Avaliações da equipa" />
    </AppButton>
  </>
)}

  {/* Só admins */}
  {isAdmin && (
    <AppButton variant="primary" onClick={() => navigate("/settings")}>
      <FaCog className="mr-2" />
      <FormattedMessage id="dashboard.buttons.settings" defaultMessage="Definições" />
    </AppButton>
  )}
</div>

    </PageLayout>
  );
}

function DashboardCard({ icon, label, value, subLabel }) {
  // Limitar value a máximo de 12 caracteres para não rebentar layout
  const displayedValue =
    typeof value === "string" && value.length > 12
      ? value.slice(0, 12) + "..."
      : value;

  return (
    <div className="bg-white rounded-2xl shadow-md hover:shadow-lg transition-all duration-200 p-6 flex flex-col justify-between items-center min-h-[160px] w-full text-center">
      <div className="flex flex-col items-center mb-1">
        <div className="mb-1">{React.cloneElement(icon, { size: 32 })}</div>
        <span className="text-base font-semibold truncate max-w-[250px]">{label}</span>
      </div>
      <div className="text-3xl font-extrabold leading-tight min-h-[38px] flex items-center justify-center">
        {displayedValue}
      </div>
      <div className="text-xs text-gray-500 mt-1" style={{ minHeight: "20px" }}>
        {subLabel || <span className="invisible">-</span>}
      </div>
    </div>
  );
}







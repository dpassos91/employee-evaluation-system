import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import { FormattedMessage } from "react-intl";
import {
  FaBook,
  FaFileAlt,
  FaCalendarCheck,
  FaClock,
  FaSearch,
  FaCog
} from "react-icons/fa";

export default function DashboardPage() {
  return (
    <PageLayout
      title={<FormattedMessage id="dashboard.greeting" defaultMessage="Olá, Nome Apelido 👋" />}
      subtitle={<FormattedMessage id="dashboard.subtitle" defaultMessage="Pronto para continuar a crescer?" />}
    >
      {/* Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center mb-2">
            <FaBook className="text-red-600 mr-2" />
            <span className="font-bold">
              <FormattedMessage id="dashboard.cards.activeTrainings" defaultMessage="Formações ativas" />
            </span>
          </div>
          <p className="text-gray-700">
            <FormattedMessage id="dashboard.cards.trainingsOngoing" defaultMessage="2 formações em curso" />
          </p>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center mb-2">
            <FaFileAlt className="text-red-600 mr-2" />
            <span className="font-bold">
              <FormattedMessage id="dashboard.cards.pendingEvaluations" defaultMessage="Avaliações em aberto" />
            </span>
          </div>
          <p className="text-gray-700">
            <FormattedMessage id="dashboard.cards.evaluationsToFill" defaultMessage="1 avaliação por preencher" />
          </p>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center mb-2">
            <FaCalendarCheck className="text-red-600 mr-2" />
            <span className="font-bold">
              <FormattedMessage id="dashboard.cards.lastEvaluation" defaultMessage="Última avaliação" />
            </span>
          </div>
          <p className="text-gray-700">
            <FormattedMessage id="dashboard.cards.lastEvaluationDate" defaultMessage="Feita a 27/05/2025" />
          </p>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center mb-2">
            <FaClock className="text-red-600 mr-2" />
            <span className="font-bold">
              <FormattedMessage id="dashboard.cards.totalTrainingHours" defaultMessage="Total de horas de formação" />
            </span>
          </div>
          <p className="text-gray-700">
            <FormattedMessage id="dashboard.cards.trainingHours" defaultMessage="24h acumuladas este ano" />
          </p>
        </div>
      </div>

      {/* Agenda */}
      <div className="mb-8">
        <h3 className="font-semibold text-lg mb-2">
          <FormattedMessage id="dashboard.agenda.title" defaultMessage="Agenda" />
        </h3>
        <ul className="bg-white rounded-lg shadow divide-y">
          <li className="p-4">
            <FormattedMessage id="dashboard.agenda.currentEvaluationEnds" defaultMessage="Avaliação atual termina a 29/05" />
          </li>
          <li className="p-4">
            <FormattedMessage id="dashboard.agenda.trainingSession" defaultMessage="Sessão de formação " />
            <span className="font-bold">UX/UI</span>
            <FormattedMessage id="dashboard.agenda.trainingSessionDate" defaultMessage=" a 1/06" />
          </li>
        </ul>
      </div>

      {/* Bottom Buttons */}
      <div className="flex flex-wrap gap-20 justify-center">
        <AppButton variant="primary">
          <FaSearch className="mr-2" />
          <FormattedMessage id="dashboard.buttons.viewTrainings" defaultMessage="Ver formações" />
        </AppButton>
        <AppButton variant="secondary">
          <FaCog className="mr-2" />
          <FormattedMessage id="dashboard.buttons.settings" defaultMessage="Definições da aplicação" />
        </AppButton>
        <AppButton variant="primary">
          <FaFileAlt className="mr-2" />
          <FormattedMessage id="dashboard.buttons.viewEvaluations" defaultMessage="Consultar avaliações" />
        </AppButton>
      </div>
    </PageLayout>
  );
}







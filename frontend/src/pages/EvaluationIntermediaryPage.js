/**
 * EvaluationIntermediaryPage component acts as a hub for evaluation-related actions.
 * It allows navigation to key areas like evaluation list and new cycle creation.
 * Icons and routes are customizable.
 */

import { useNavigate } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import { FormattedMessage } from "react-intl";

// Import your local icons
import cycleIcon from "../images/evaluation_cycle_icon.png";
import listIcon from "../images/evaluations_icon.png"; 

export default function EvaluationIntermediaryPage() {
  const navigate = useNavigate();

  return (
    <PageLayout
      title={
        <FormattedMessage
          id="evaluation.intermediary.title"
          defaultMessage="Gestão de Avaliações"
        />
      }
    >
      <div className="mt-20 flex flex-col md:flex-row justify-center items-center gap-10">
        {/* Button 1: create new cycle */}
        <AppButton
          variant="primary"
          onClick={() => navigate("/newevaluationcycle")}
          className="w-56 h-32 flex flex-col items-center justify-center gap-3 text-center"
        >
          <img
            src={cycleIcon}
            alt="Novo Ciclo"
            className="w-10 h-10"
          />
          <FormattedMessage
            id="button.newCycle"
            defaultMessage="Novo Ciclo de Avaliação"
          />
        </AppButton>

        {/* Button 2 : list of evaluations */}
        <AppButton
          variant="primary"
          onClick={() => navigate("/evaluationlist")}
           className="w-56 h-32 flex flex-col items-center justify-center gap-3 text-center"
        >
          <img
            src={listIcon}
            alt="Listagem de Avaliações"
            className="w-10 h-10"
          />
          <FormattedMessage
            id="button.evaluationList"
            defaultMessage="Lista de Avaliações"
          />
        </AppButton>
      </div>
    </PageLayout>
  );
}

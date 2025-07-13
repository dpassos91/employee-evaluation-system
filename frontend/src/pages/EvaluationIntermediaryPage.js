import { useNavigate } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import { FormattedMessage, useIntl } from "react-intl";
import { toast } from "react-toastify";
import { userStore } from "../stores/userStore";

import cycleIcon from "../images/evaluation_cycle_icon.png";
import listIcon from "../images/evaluations_icon.png";

export default function EvaluationIntermediaryPage() {
  const navigate = useNavigate();
  const intl = useIntl();
  const user = userStore((state) => state.user);

  /**
   * Shows a translated toast error when access is denied.
   */
  const showAccessDeniedToast = () => {
    toast.error(
      intl.formatMessage({
        id: "toast.accessDenied",
        defaultMessage: "Apenas administradores têm acesso a esta funcionalidade."
      })
    );
  };

  /**
   * Handles click on "Novo Ciclo" button.
   * Only admins can access this.
   */
  const handleNewCycleClick = () => {
    if (user?.role === "ADMIN") {
      navigate("/newevaluationcycle");
    } else {
      showAccessDeniedToast();
    }
  };

  /**
   * Handles click on "Listagem de Avaliações" (accessible to admin and manager).
   */
  const handleEvaluationListClick = () => {
    navigate("/evaluationlist");
  };


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
          onClick={handleNewCycleClick}
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
          onClick={handleEvaluationListClick}
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

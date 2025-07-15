/**
 * EvaluationFormPage component renders an evaluation form that allows admins
 * to assign managers and users to submit performance feedback.
 * 
 * It fetches evaluation data, handles form input, displays user information,
 * and provides feedback via toast notifications.
 */


import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import AppForm from "../components/AppForm";
import { FormattedMessage } from "react-intl";
import { evaluationAPI } from "../api/evaluationAPI";
import { toast } from "react-toastify";
import { userStore } from "../stores/userStore";
import profileIcon from "../images/profile_icon.png";
import { useIntl } from "react-intl";
import "react-toastify/dist/ReactToastify.css";
import { ToastContainer } from "react-toastify";
import AvatarCell from "../components/AvatarCell";

export default function EvaluationFormPage() {
  /** @type {{ userId: string }} */
  const { userId: rawUserId } = useParams();
const userId = parseInt(rawUserId, 10);
  const navigate = useNavigate();
  const user = userStore((state) => state.user);

const intl = useIntl();

 /**
   * Displays a toast notification with internationalized message.
   * @param {string} id - Message ID for i18n.
   * @param {string} defaultMessage - Fallback message text.
   * @param {"error"|"success"} [type="error"] - Toast type.
   * @param {object} [options={}] - Toast options (e.g., onClose, autoClose).
   */
const showToast = (id, defaultMessage, type = "error", options = {}) => {
  const msg = intl.formatMessage({ id, defaultMessage });
  toast[type](msg, options); 
};


  const isAdmin = user?.role === "ADMIN";

  const [grade, setGrade] = useState("");
  const [feedback, setFeedback] = useState("");
  const [name, setName] = useState("");
  const [photo, setPhoto] = useState(null);
  const [managerName, setManagerName] = useState("");
  const [managerEmail, setManagerEmail] = useState("");
  const [dropdownUsers, setDropdownUsers] = useState([]);
  const [selectedManager, setSelectedManager] = useState("");
  const [evaluatedEmail, setEvaluatedEmail] = useState("");

  const [loading, setLoading] = useState(true);
  const [updatingManager, setUpdatingManager] = useState(false);

  const isChangeDisabled =
  updatingManager ||
  selectedManager === managerEmail ||
  selectedManager === evaluatedEmail;



   /**
   * Fetches evaluation data from the API and populates form fields.
   */
  useEffect(() => {
    const fetchEvaluation = async () => {
      try {
        const result = await evaluationAPI.loadEvaluation(userId, sessionStorage.getItem("authToken"));

        const evaluation = result.evaluation;

        setEvaluatedEmail(evaluation.evaluatedEmail || "");
        setName(evaluation.evaluatedName || "");
        setGrade(evaluation.grade ? String(evaluation.grade) : "");
        setFeedback(evaluation.feedback || "");
        setPhoto(evaluation.photograph);
        setManagerName(evaluation.evaluatorName || "");
        setManagerEmail(evaluation.evaluatorEmail || "");
        setSelectedManager(evaluation.evaluatorEmail || "");
      } catch (err) {
        showToast("toast.loadEvaluationError", "Erro ao carregar dados da avaliação.");
      } finally {
        setLoading(false);
      }
    };

    fetchEvaluation();
  }, [userId]);


   /**
   * Loads available managers for the dropdown if user is admin.
   */
  useEffect(() => {
    const fetchDropdownUsers = async () => {
      if (!isAdmin) return;
      try {
        const result = await evaluationAPI.getManagerDropdown();
        setDropdownUsers(result);
      } catch (err) {
        showToast("toast.loadManagersError", "Erro ao carregar lista de gestores disponíveis.");
      }
    };

    fetchDropdownUsers();
  }, [isAdmin]);


   /**
   * Submits the evaluation data to the API.
   */
  const handleSubmit = async () => {

    try {

      await evaluationAPI.updateEvaluation(

        {

          evaluatedEmail,

          grade: parseInt(grade),

          feedback,

        },

        sessionStorage.getItem("authToken")

      );



      showToast("toast.updateSuccess", "Avaliação atualizada com sucesso.", "success", {

        onClose: () => navigate("/evaluationlist"),

        autoClose: 2000,

      });

    } catch (err) {

      showToast("toast.updateError", "Erro ao salvar avaliação.");

    }

  };


/**
   * Assigns a new manager to the evaluated user.
   */
  const handleAssignManager = async () => {
    if (!selectedManager || selectedManager === managerEmail) return;
    setUpdatingManager(true);
    try {
      await evaluationAPI.assignManager({ userEmail: evaluatedEmail, managerEmail: selectedManager });
      showToast("toast.managerUpdateSuccess", "Gestor atualizado com sucesso.", "success");
      setManagerEmail(selectedManager);
      const newManager = dropdownUsers.find((u) => u.email === selectedManager);
      setManagerName(`${newManager.firstName} ${newManager.lastName}`);
    } catch (err) {
      toast.error("Erro ao atualizar gestor.");
    } finally {
      setUpdatingManager(false);
    }
  };


  

  return (
  <PageLayout title={<FormattedMessage id="evaluations.form.title" defaultMessage="Formulário de Avaliação" />}>
    {loading ? (
      <div className="text-gray-500">
        <FormattedMessage id="loading" defaultMessage="A carregar..." />
      </div>
    ) : (
      <div className="flex items-start gap-10 px-10 pt-6">
        {/* Avatar at the left */}
        <div className="flex-shrink-0">
          <img
            src={photo || profileIcon}
            alt="avatar"
            className="w-28 h-28 rounded-full object-cover border"
          />
        </div>

        {/* Form at the right */}
        <div className="flex flex-col gap-4 max-w-md w-full">
          {/* Name */}
          <div>
            <label className="block text-sm font-bold mb-1">
              <FormattedMessage id="evaluations.form.name" defaultMessage="Nome" />
            </label>
            <input
              type="text"
              value={name}
              readOnly
              className="w-full border rounded px-2 py-1 bg-gray-100 cursor-not-allowed"
            />
          </div>

          {/* Manager */}
          <div>
            <label className="block text-sm font-bold mb-1">
              <FormattedMessage id="evaluations.form.manager" defaultMessage="Gestor" />
            </label>
            {isAdmin ? (
              <div className="flex gap-2 items-center">
                <select
                  value={selectedManager}
                  onChange={(e) => setSelectedManager(e.target.value)}
                  className="border px-2 py-1 rounded w-full"
                >
                  {dropdownUsers.map((u) => (
                    <option key={u.email} value={u.email}>
                      {u.firstName} {u.lastName}
                    </option>
                  ))}
                </select>
                <button
  type="button"
  onClick={handleAssignManager}
  disabled={isChangeDisabled}
  className="bg-red-600 text-white px-3 py-1 rounded disabled:opacity-50 disabled:cursor-not-allowed"
>
  <FormattedMessage id="evaluations.form.changeManager" defaultMessage="Alterar" />
</button>
              </div>
            ) : (
              <input
                type="text"
                value={managerName}
                readOnly
                className="w-full border rounded px-2 py-1 bg-gray-100 cursor-not-allowed"
              />
            )}
          </div>

          {/* Evaluation */}
          <div>
            <label className="block text-sm font-bold mb-1">
              <FormattedMessage id="evaluations.form.grade" defaultMessage="Avaliação" />
            </label>
            <select
              value={grade}
              onChange={(e) => setGrade(e.target.value)}
              className="w-full border rounded px-2 py-1"
              required
            >
              <option value="">
    <FormattedMessage id="evaluations.selectGrade" defaultMessage="-- Selecionar --" />
  </option>
  <option value="1">
    <FormattedMessage id="evaluations.grade.1" defaultMessage="1 - Contribuição baixa" />
  </option>
  <option value="2">
    <FormattedMessage id="evaluations.grade.2" defaultMessage="2 - Contribuição parcial" />
  </option>
  <option value="3">
    <FormattedMessage id="evaluations.grade.3" defaultMessage="3 - Conforme esperado" />
  </option>
  <option value="4">
    <FormattedMessage id="evaluations.grade.4" defaultMessage="4 - Contribuição excedida" />
  </option>
            </select>
          </div>

          {/* Feedback */}
          <div>
            <label className="block text-sm font-bold mb-1">
              <FormattedMessage id="evaluations.form.feedback" defaultMessage="Feedback" />
            </label>
            <textarea
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              rows={4}
              className="w-full border rounded px-2 py-1"
            />
          </div>

          {/* Buttons alligned */}
          <div className="flex justify-center gap-4 pt-4">
            <button
              type="button"
              onClick={() => navigate("/evaluationlist")}
              className="bg-gray-300 text-black px-4 py-2 rounded"
            >
              <FormattedMessage id="button.cancel" defaultMessage="Cancelar" />
            </button>
            <button
              type="button"
              onClick={handleSubmit}
              className="bg-red-600 text-white px-4 py-2 rounded"
            >
              <FormattedMessage id="button.save" defaultMessage="Salvar" />
            </button>
          </div>
        </div>
      </div>



    )}

<ToastContainer position="top-center" autoClose={3000} />

  </PageLayout>
);

}

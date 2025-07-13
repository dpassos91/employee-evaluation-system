import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import AppForm from "../components/AppForm";
import { FormattedMessage } from "react-intl";
import { apiConfig } from "../api/apiConfig";
import { toast } from "react-toastify";
import { userStore } from "../stores/userStore";
import profileIcon from "../images/profile_icon.png";

export default function EvaluationFormPage() {
  const { email } = useParams();
  const navigate = useNavigate();
  const user = userStore((state) => state.user);

  const isAdmin = user?.role === "ADMIN";

  const [grade, setGrade] = useState("");
  const [feedback, setFeedback] = useState("");
  const [name, setName] = useState("");
  const [photo, setPhoto] = useState(null);
  const [managerName, setManagerName] = useState("");
  const [managerEmail, setManagerEmail] = useState("");
  const [dropdownUsers, setDropdownUsers] = useState([]);
  const [selectedManager, setSelectedManager] = useState("");

  const [loading, setLoading] = useState(true);
  const [updatingManager, setUpdatingManager] = useState(false);

  const isChangeDisabled =
  updatingManager || selectedManager === managerEmail || selectedManager === email;

  useEffect(() => {
    const fetchEvaluation = async () => {
      try {
        const result = await apiConfig.apiCall(
          apiConfig.API_ENDPOINTS.evaluations.load(email)
        );
        const evaluation = result.evaluation;

        setName(evaluation.evaluatedName || "");
        setGrade(evaluation.grade ? String(evaluation.grade) : "");
        setFeedback(evaluation.feedback || "");
        setPhoto(evaluation.photograph);
        setManagerName(evaluation.evaluatorName || "");
        setManagerEmail(evaluation.evaluatorEmail || "");
        setSelectedManager(evaluation.evaluatorEmail || "");
      } catch (err) {
        toast.error("Erro ao carregar dados da avaliação.");
      } finally {
        setLoading(false);
      }
    };

    fetchEvaluation();
  }, [email]);

  useEffect(() => {
    const fetchDropdownUsers = async () => {
      if (!isAdmin) return;
      try {
        const result = await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.managerDropdown);
        setDropdownUsers(result);
      } catch (err) {
        toast.error("Erro ao carregar lista de gestores disponíveis.");
      }
    };

    fetchDropdownUsers();
  }, [isAdmin]);

  const handleSubmit = async () => {
    try {
      await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.update, {
        method: "PUT",
        body: JSON.stringify({
          evaluatedEmail: email,
          grade: parseInt(grade),
          feedback,
        }),
      });
      toast.success("Avaliação atualizada com sucesso.");
      navigate("/evaluationlist");
    } catch (err) {
      toast.error("Erro ao salvar avaliação.");
    }
  };

  const handleAssignManager = async () => {
    if (!selectedManager || selectedManager === managerEmail) return;
    setUpdatingManager(true);
    try {
      await apiConfig.apiCall(apiConfig.API_ENDPOINTS.evaluations.assignManager, {
        method: "POST",
        body: JSON.stringify({
          userEmail: email,
          managerEmail: selectedManager,
        }),
      });
      toast.success("Gestor atualizado com sucesso.");
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
  </PageLayout>
);

}

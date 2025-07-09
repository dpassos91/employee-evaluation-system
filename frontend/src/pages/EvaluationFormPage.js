import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import AppForm from "../components/AppForm";
import { FormattedMessage } from "react-intl";
import { apiConfig } from "../api/apiConfig";
import { toast } from "react-toastify";
import { userStore } from "../stores/userStore";

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

  useEffect(() => {
    const fetchEvaluation = async () => {
      try {
        const result = await apiConfig.apiCall(
          `${apiConfig.API_ENDPOINTS.evaluations.load}?email=${email}`
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
        const result = await apiConfig.apiCall(apiConfig.API_ENDPOINTS.users.managerDropdown);
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
      await apiConfig.apiCall(apiConfig.API_ENDPOINTS.users.assignManager, {
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
        <AppForm
          onSubmit={handleSubmit}
          actions={[
            {
              label: <FormattedMessage id="button.cancel" defaultMessage="Cancelar" />,
              onClick: () => navigate("/evaluationlist"),
              variant: "secondary",
            },
            {
              label: <FormattedMessage id="button.save" defaultMessage="Salvar" />,
              type: "submit",
              loading: loading,
            },
          ]}
        >
          {photo && (
            <div className="flex justify-center mb-4">
              <img
                src={photo}
                alt="avatar"
                className="w-24 h-24 rounded-full object-cover"
              />
            </div>
          )}

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

          {/* GESTOR */}
          <div>
            <label className="block text-sm font-bold mb-1">
              <FormattedMessage id="evaluations.form.manager" defaultMessage="Gestor" />
            </label>
            {isAdmin ? (
              <div className="flex gap-2 items-center">
                <select
                  value={selectedManager}
                  onChange={(e) => setSelectedManager(e.target.value)}
                  className="border px-2 py-1 rounded"
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
                  disabled={updatingManager}
                  className="bg-red-600 text-white px-3 py-1 rounded"
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

          {/* Grade */}
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
              <option value="">--</option>
              <option value="1">1 - Contribuição baixa</option>
              <option value="2">2 - Contribuição parcial</option>
              <option value="3">3 - Como esperado</option>
              <option value="4">4 - Excedeu</option>
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
              rows={5}
              className="w-full border rounded px-2 py-1"
            />
          </div>
        </AppForm>
      )}
    </PageLayout>
  );
}

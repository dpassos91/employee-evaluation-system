import { useState, useMemo } from "react";
import { useParams } from "react-router-dom";
import PageLayout from "../components/PageLayout";
import { FormattedMessage } from "react-intl";
import { evaluationAPI } from "../api/evaluationAPI";
import { useEvaluationHistoryWithFilters } from "../hooks/useEvaluationHistoryWithFilters";
import { toast } from "react-toastify";
import { useIntl } from "react-intl";

export default function EvaluationHistoryPage() {
  const { userId } = useParams();

const intl = useIntl();

  const [cycleId, setCycleId] = useState("");
  const [cycleEndDate, setCycleEndDate] = useState("");
  const [grade, setGrade] = useState("");
  const [hover, setHover] = useState(null);
  const [page, setPage] = useState(1);

 const filters = useMemo(
  () => ({ cycleId, cycleEndDate, grade, page }),
  [cycleId, cycleEndDate, grade, page]
);

  const {
    evaluations,
    totalPages,
    loading,
    error,
    refetch,
  } = useEvaluationHistoryWithFilters(userId, filters);

  const handleExportPDF = async (evaluationId) => {
  try {
    const token = sessionStorage.getItem("authToken");
    await evaluationAPI.downloadEvaluationPdf(evaluationId, token);
  } catch {
    toast.error(intl.formatMessage({
      id: "toast.exportPdfError",
      defaultMessage: "Erro ao exportar avaliação para PDF."
    }));
  }
};

  return (
    <PageLayout title={<FormattedMessage id="evaluations.history.title" defaultMessage="Histórico de Avaliações" />}>
      <div className="flex gap-4 mb-4">
  
  <FormattedMessage id="filter.cycle" defaultMessage="Ciclo">
    {(placeholderText) => (
      <input
        placeholder={placeholderText}
        className="border px-2 py-1 rounded"
        value={cycleId}
        onChange={(e) => {
          const value = parseInt(e.target.value, 10);
          setCycleId(Number.isNaN(value) ? "" : value);
          setPage(1);
        }}
      />
    )}
  </FormattedMessage>
        <input
          type="date"
          value={cycleEndDate}
          onChange={(e) => { setCycleEndDate(e.target.value); setPage(1); }}
          className="border px-2 py-1 rounded"
        />
        <div className="flex items-center gap-1">
          {[1, 2, 3, 4].map((star) => (
            <button
              key={star}
              type="button"
              onClick={() => {
                setGrade(grade === String(star) ? "" : String(star));
                setPage(1);
              }}
              onMouseEnter={() => setHover(star)}
              onMouseLeave={() => setHover(null)}
              className="text-yellow-500 text-xl focus:outline-none"
            >
              {star <= ((hover ?? Number(grade)) || 0) ? "★" : "☆"}
            </button>
          ))}
        </div>
      </div>

      {loading && <div><FormattedMessage id="loading" defaultMessage="A carregar..." /></div>}
      {error && <div className="text-red-600">{error}</div>}

      {!loading && evaluations.length > 0 && (
        <table className="min-w-full border-collapse table-auto text-sm">
          <thead className="bg-gray-200">
            <tr>
  <th className="p-2">
    <FormattedMessage id="table.cycle" defaultMessage="Ciclo" />
  </th>
  <th className="p-2">
    <FormattedMessage id="table.endDate" defaultMessage="Data de Fecho" />
  </th>
  <th className="p-2">
    <FormattedMessage id="table.grade" defaultMessage="Avaliação" />
  </th>
  <th className="p-2">
    <FormattedMessage id="table.export" defaultMessage="Exportar" />
  </th>
</tr>
          </thead>
          <tbody>
            {evaluations.map((e) => (
              <tr key={e.id} className="border-b">
                <td className="p-2 text-center">{e.cycleNumber}</td>
                <td className="p-2 text-center">{e.cycleEndDate}</td>
                <td className="p-2 text-center">
                  {"★".repeat(e.grade) + "☆".repeat(4 - e.grade)}
                </td>
                <td className="p-2 text-center">
                  <button
                    onClick={() => handleExportPDF(e.id)}
                    className="bg-red-600 text-white px-3 py-1 rounded"
                  >
                    PDF
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {!loading && evaluations.length === 0 && (
        <div className="text-center text-gray-500 mt-4">
          <FormattedMessage id="evaluations.table.empty" defaultMessage="Nenhuma avaliação encontrada." />
        </div>
      )}

      {totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-2 text-blue-700 text-sm">
          {Array.from({ length: totalPages }).map((_, idx) => (
            <button
              key={idx + 1}
              className={`hover:underline ${page === idx + 1 ? "font-bold underline" : ""}`}
              onClick={() => setPage(idx + 1)}
            >
              {idx + 1}
            </button>
          ))}
          {page < totalPages && (
            <button onClick={() => setPage(page + 1)}>{">"}</button>
          )}
        </div>
      )}
    </PageLayout>
  );
}
 
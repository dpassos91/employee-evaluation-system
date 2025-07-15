import { useEffect, useState } from "react";
import PageLayout from "../components/PageLayout";
import { userStore } from "../stores/userStore";
import { AppTable } from "../components/AppTable";
import { FormattedMessage, useIntl } from "react-intl";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { courseAPI } from "../api/courseAPI";
import Modal from "../components/Modal";
import { useParams } from "react-router-dom";

export default function CoursesHistoryPage() {
  const intl = useIntl();
  const user = userStore((state) => state.user);
  const [courses, setCourses] = useState([]);
  const [years, setYears] = useState([]);
  const [selectedYear, setSelectedYear] = useState(null);
  const [yearlySummary, setYearlySummary] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [formData, setFormData] = useState({ courseId: '', participationDate: '' });
  const [availableCourses, setAvailableCourses] = useState([]);

  const { userId: paramUserId } = useParams();
const userId = Number(paramUserId);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        setLoading(true);
        const [history, availableYears, summary] = await Promise.all([
          courseAPI.getUserCourseHistory(userId),
          courseAPI.getUserCourseYears(userId),
          courseAPI.getUserCourseSummaryByYear(userId),
        ]);
        setCourses(history);
        setYears(availableYears);
        setYearlySummary(summary);
        setSelectedYear(availableYears[availableYears.length - 1] || null);
        setError(null);
      } catch (err) {
        setError("Erro ao carregar o histórico de formações.");
      } finally {
        setLoading(false);
      }
    };

    if (userId) fetchHistory();
  }, [userId]);

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const activeCourses = await courseAPI.listCourses({ active: true });
        setAvailableCourses(activeCourses);
      } catch (err) {
        console.error("Erro ao carregar cursos ativos:", err);
      }
    };

    fetchCourses();
  }, []);

  const getHoursForSelectedYear = () => {
    const found = yearlySummary.find((item) => item.year === selectedYear);
    return found ? found.totalHours : 0;
  };

  const handleAssignCourse = async (e) => {
    e.preventDefault();
    try {
      await courseAPI.assignCourseToUser({
        userId,
        courseId: formData.courseId,
        participationDate: formData.participationDate,
      });
      toast.success("Formação atribuída com sucesso!");
      setShowAssignModal(false);
    } catch (err) {
      toast.error("Erro ao atribuir formação.");
    }
  };

  const columns = [
    {
      header: <FormattedMessage id="courses.table.name" defaultMessage="Formação" />, 
      accessor: "courseName",
    },
    {
      header: <FormattedMessage id="courses.table.hours" defaultMessage="Duração" />, 
      accessor: "timeSpan",
      render: (row) => `${row.timeSpan} h`,
    },
    {
      header: <FormattedMessage id="courses.table.language" defaultMessage="Idioma" />, 
      accessor: "language",
    },
    {
      header: <FormattedMessage id="courses.table.category" defaultMessage="Área" />, 
      accessor: "courseCategory",
    },
    {
      header: <FormattedMessage id="courses.table.date" defaultMessage="Data" />, 
      accessor: "participationDate",
      render: (row) => new Date(row.participationDate).toLocaleDateString(),
    },
  ];

  const formatDecimalHours = (decimalHours) => {
    const hours = Math.floor(decimalHours);
    const minutes = Math.round((decimalHours - hours) * 60);

    let result = '';
    if (hours > 0) result += `${hours} h`;
    if (minutes > 0) result += ` ${minutes} mins`;

    return result.trim() || '0 mins';
  };

  return (
    <PageLayout title={<FormattedMessage id="courses.history.title" defaultMessage="Histórico de Formações" />}>

      <div className="flex justify-between items-center mb-6">
        <div className="text-lg font-semibold text-gray-800 flex items-center">
          <span className="mr-2">
            <FormattedMessage
              id="courses.summary.totalHours.label"
              defaultMessage="Tempo de formação em:"
            />
          </span>

          <select
            id="yearSelect"
            value={selectedYear || ''}
            onChange={(e) => setSelectedYear(Number(e.target.value))}
            className="border border-gray-300 rounded px-2 py-1 text-sm mr-4"
          >
            <>
              <option value="">
                <FormattedMessage id="courses.dropdown.selectYear" defaultMessage="Selecione ano" />
              </option>
              {years.map((year) => (
                <option key={year} value={year}>{year}</option>
              ))}
            </>
          </select>

          <span>{formatDecimalHours(getHoursForSelectedYear())}</span>
        </div>

        <button
          onClick={() => setShowAssignModal(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded"
        >
          <FormattedMessage id="courses.assignCourse" defaultMessage="Atribuir Formação" />
        </button>
      </div>

      {showAssignModal && (
        <Modal
          isOpen={showAssignModal}
          onClose={() => setShowAssignModal(false)}
          title={<FormattedMessage id="courses.modal.title" defaultMessage="Atribuir Formação" />}
        >
          <form onSubmit={handleAssignCourse}>
            <div className="mb-4">
              <label className="block mb-1 text-sm font-medium">
                <FormattedMessage id="courses.field.courseId" defaultMessage="Formação" />
              </label>
              <select
                value={formData.courseId}
                onChange={(e) => setFormData({ ...formData, courseId: e.target.value })}
                required
                className="border px-2 py-1 rounded w-full"
              >
                <option value="">
                  <FormattedMessage id="courses.assign.select" defaultMessage="Selecione uma formação" />
                </option>
                {availableCourses.map((course) => (
                  <option key={course.id} value={course.id}>
                    {course.name} ({course.timeSpan}h)
                  </option>
                ))}
              </select>
            </div>
            <div className="mb-4">
              <label className="block mb-1 text-sm font-medium">
                <FormattedMessage id="courses.field.date" defaultMessage="Data de Participação" />
              </label>
              <input
                type="date"
                value={formData.participationDate}
                onChange={(e) => setFormData({ ...formData, participationDate: e.target.value })}
                required
                className="border px-2 py-1 rounded w-full"
              />
            </div>
            <div className="flex gap-2 justify-end">
              <button type="button" onClick={() => setShowAssignModal(false)} className="bg-gray-300 px-4 py-2 rounded">
                <FormattedMessage id="modal.cancel" defaultMessage="Cancelar" />
              </button>
              <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">
                <FormattedMessage id="modal.save" defaultMessage="Guardar" />
              </button>
            </div>
          </form>
        </Modal>
      )}

      {loading && (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="table.loading" defaultMessage="A carregar..." />
        </div>
      )}
      {error && (
        <div className="py-8 text-center text-red-600">{error}</div>
      )}

      {!loading && courses.length > 0 && (
        <AppTable
          columns={columns}
          data={courses}
          loading={loading}
          emptyMessage={<FormattedMessage id="courses.table.empty" defaultMessage="Sem formações." />}
        />
      )}

      {!loading && courses.length === 0 && (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="courses.table.empty" defaultMessage="Sem formações." />
        </div>
      )}

      <ToastContainer position="top-center" autoClose={3000} />
    </PageLayout>
  );
}

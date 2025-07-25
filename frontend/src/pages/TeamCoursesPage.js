import { useEffect, useState } from "react";
import PageLayout from "../components/PageLayout";
import { AppTable } from "../components/AppTable";
import { FormattedMessage, useIntl } from "react-intl";
import AvatarCell from "../components/AvatarCell";
import Modal from "../components/Modal";
import { courseAPI } from "../api/courseAPI";
import { userStore } from "../stores/userStore";
import { toast } from "react-toastify";

export default function TeamCoursesPage() {
  const intl = useIntl();
  const user = userStore((state) => state.user);

  const [teamCourses, setTeamCourses] = useState([]);
  const [loading, setLoading] = useState(true);

  const [years, setYears] = useState([]);
  const [selectedYear, setSelectedYear] = useState(null);

  // Modal state
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [assignTarget, setAssignTarget] = useState(null);
  const [formData, setFormData] = useState({ courseId: "", participationDate: "" });
  const [availableCourses, setAvailableCourses] = useState([]);

  // Fetch team courses (by year, or all if year is null)
  const fetchTeamCourses = async (year) => {
    setLoading(true);
    try {
      const params = year ? { year } : {};
      const result = await courseAPI.getTeamCourses(params);
      setTeamCourses(Array.isArray(result) ? result : []);
    } catch (err) {
      toast.error(intl.formatMessage({ id: "courses.team.loadError", defaultMessage: "Erro ao carregar equipa." }));
      setTeamCourses([]);
    } finally {
      setLoading(false);
    }
  };

  // Fetch available courses for assignment
  const fetchAvailableCourses = async () => {
    try {
      const courses = await courseAPI.listCourses({ active: true });
      setAvailableCourses(Array.isArray(courses) ? courses : []);
    } catch (err) {
      setAvailableCourses([]);
    }
  };

  // Fetch years for dropdown
const fetchTeamCourseYears = async () => {
  try {
    const response = await courseAPI.getTeamParticipationYears();
    console.log("DEBUG: years API response →", response);
    setYears(Array.isArray(response) ? response : []);
  } catch (e) {
    setYears([]);
    console.log("DEBUG: erro ao buscar anos", e);
  }
};

  // Carrega tudo no início
  useEffect(() => {
    fetchAvailableCourses();
    fetchTeamCourseYears();
    fetchTeamCourses(null); 
  }, []);

  // Atualiza cursos sempre que muda o ano (ou todos)
  useEffect(() => {
    fetchTeamCourses(selectedYear);
  }, [selectedYear]);

  // Modal open handler
  const openAssignModal = (user) => {
    setAssignTarget(user);
    setFormData({ courseId: "", participationDate: "" });
    setShowAssignModal(true);
  };

  // Handle assignment form submit
  const handleAssignCourse = async (e) => {
    e.preventDefault();
    if (!formData.courseId || !formData.participationDate) {
      toast.error(intl.formatMessage({ id: "toast.course.assign.missingFields" }), { closeButton: false });
      return;
    }
    try {
      await courseAPI.assignCourseToUser({
        userId: assignTarget.userId,
        courseId: formData.courseId,
        participationDate: formData.participationDate,
      });
      toast.success(intl.formatMessage({ id: "toast.course.assign.success" }), { closeButton: false });
      setShowAssignModal(false);
      fetchTeamCourses(selectedYear); // Atualiza para o ano filtrado
    } catch {
      toast.error(intl.formatMessage({ id: "toast.course.assign.error" }), { closeButton: false });
    }
  };

  // Define columns
  const columns = [
    {
      header: <FormattedMessage id="users.table.name" defaultMessage="Nome" />,
      accessor: (row) => (
        <span className="font-medium">{row.user.firstName} {row.user.lastName}</span>
      ),
      className: "text-left w-[140px] pr-2",
    },
    {
      header: "",
      accessor: (row) => (
        <div className="justify-left pr-24">
          <AvatarCell
            avatar={row.user.photograph}
            name={`${row.user.firstName} ${row.user.lastName}`}
          />
        </div>
      ),
      className: "text-center w-[54px]",
    },
    {
      header: <FormattedMessage id="courses.table.assigned" defaultMessage="Formações Atribuídas" />,
      accessor: (row) =>
        row.courses && row.courses.length
          ? row.courses.map((c) => c.courseName).join(", ")
          : <span className="text-gray-400"><FormattedMessage id="courses.table.none" defaultMessage="Nenhuma" /></span>,
      className: "text-left w-[260px]",
    },
    {
      header: <FormattedMessage id="courses.table.hours" defaultMessage="Duração" />,
      accessor: (row) =>
        row.courses && row.courses.length
          ? row.courses.reduce((sum, c) => sum + (c.timeSpan || 0), 0) + " h"
          : "0 h",
      className: "text-left w-[100px]",
    },
    {
      header: <FormattedMessage id="users.table.actions" defaultMessage="Ações" />,
      accessor: null,
      render: (row) => (
        <button
          className="bg-red-600 text-white px-3 py-1 rounded"
          onClick={() => openAssignModal(row.user)}
        >
          <FormattedMessage id="courses.assignCourse" defaultMessage="Atribuir Formação" />
        </button>
      ),
      className: "text-left w-[180px] pl-2",
    },
  ];

  return (
<PageLayout title={<FormattedMessage id="courses.team.title" defaultMessage="Formações de Equipa" />}>
  <div className="flex justify-between items-center mb-6">
    <div className="text-lg font-semibold text-gray-800 flex items-center">
      <span className="mr-2">
        <FormattedMessage id="courses.summary.totalHours.label" defaultMessage="Tempo de formação em:" />
      </span>
      <select
        id="yearSelect"
        value={selectedYear || ""}
        onChange={e => setSelectedYear(e.target.value ? Number(e.target.value) : null)}
        className="border border-gray-300 rounded px-2 py-1 text-sm mr-4"
      >
        <option value="">
          <FormattedMessage id="courses.dropdown.selectYear" defaultMessage="Selecione o ano" />
        </option>
        {years.map((year) => (
          <option key={year} value={year}>{year}</option>
        ))}
      </select>
    </div>
  </div>

      {/* Modal de atribuição */}
      {showAssignModal && assignTarget && (
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
                onChange={e => setFormData({ ...formData, courseId: e.target.value })}
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
                onChange={e => setFormData({ ...formData, participationDate: e.target.value })}
                required
                className="border px-2 py-1 rounded w-full"
              />
            </div>
            <div className="flex gap-2 justify-end">
              <button type="button" onClick={() => setShowAssignModal(false)} className="bg-gray-300 px-4 py-2 rounded">
                <FormattedMessage id="modal.cancel" defaultMessage="Cancelar" />
              </button>
              <button type="submit" className="bg-red-600 text-white px-4 py-2 rounded">
                <FormattedMessage id="modal.save" defaultMessage="Guardar" />
              </button>
            </div>
          </form>
        </Modal>
      )}

      {loading ? (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="table.loading" defaultMessage="A carregar..." />
        </div>
      ) : (
        <AppTable
          columns={columns}
          data={teamCourses}
          loading={loading}
          emptyMessage={<FormattedMessage id="courses.team.empty" defaultMessage="Nenhum colaborador na sua equipa." />}
        />
      )}
    </PageLayout>
  );
}


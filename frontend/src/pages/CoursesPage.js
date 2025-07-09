import { useState, useMemo, useEffect } from "react";
import PageLayout from "../components/PageLayout";
import Modal from "../components/Modal";
import { FormattedMessage } from "react-intl";
import { courseAPI } from "../api/courseAPI";
import { useNavigate } from "react-router-dom";
import AppForm from "../components/AppForm";
import AppButton from "../components/AppButton";

export default function CoursesPage() {
  // Filtros
  const [name, setName] = useState("");
  const [minTimeSpan, setMinTimeSpan] = useState("");
  const [maxTimeSpan, setMaxTimeSpan] = useState("");
  const [language, setLanguage] = useState("");
  const [category, setCategory] = useState("");
  const [active, setActive] = useState("");
  const [page, setPage] = useState(1);

  // Estado único para modal e modo (criar/editar)
  const [modalOpen, setModalOpen] = useState(false);
  const [isCreateMode, setIsCreateMode] = useState(true);
  const [editingCourseId, setEditingCourseId] = useState(null);

  // Estado do formulário (partilhado)
  const [form, setForm] = useState({
    name: "",
    timeSpan: "",
    description: "",
    link: "",
    language: "",
    courseCategory: "",
    active: true
  });

  // Estado de cursos e UI
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [totalPages, setTotalPages] = useState(1);

  const navigate = useNavigate();

  // Filtros para query string
  const filters = useMemo(() => ({
    ...(name && { name }),
    ...(minTimeSpan && { minTimeSpan }),
    ...(maxTimeSpan && { maxTimeSpan }),
    ...(language && { language }),
    ...(category && { category }),
    ...(active !== "" && { active }),
    page,
  }), [name, minTimeSpan, maxTimeSpan, language, category, active, page]);

  // Carregar cursos
  useEffect(() => {
    setLoading(true);
    setError("");
    courseAPI.listCourses(filters)
      .then(data => {
        setCourses(data);
        setTotalPages(1);
        setLoading(false);
      })
      .catch(err => {
        setError("Erro ao carregar cursos.");
        setLoading(false);
      });
  }, [filters]);

  // Exportação CSV
  const handleExportCSV = async () => {
    try {
      const csvData = await courseAPI.exportCoursesCsv(filters);
      const blob = new Blob([csvData], { type: "text/csv" });
      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = "courses_export.csv";
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (err) {
      alert("Erro ao exportar ficheiro.");
    }
  };

  // Atualiza campos do formulário
  const handleFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  // Abrir modal em modo criação
  const openCreateModal = () => {
    setIsCreateMode(true);
    setForm({
      name: "",
      timeSpan: "",
      description: "",
      link: "",
      language: "",
      courseCategory: "",
      active: true
    });
    setEditingCourseId(null);
    setModalOpen(true);
  };

  // Abrir modal em modo edição
  const openEditModal = (course) => {
    setIsCreateMode(false);
    setEditingCourseId(course.id);
    setForm({
      name: course.name,
      timeSpan: course.timeSpan,
      description: course.description,
      link: course.link,
      language: course.language,
      courseCategory: course.courseCategory,
      active: course.active
    });
    setModalOpen(true);
  };

  // Handler universal submit
  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    try {
      if (isCreateMode) {
        await courseAPI.createCourse(form);
      } else {
        await courseAPI.updateCourse(editingCourseId, form);
      }
      setModalOpen(false);
      setEditingCourseId(null);
      setForm({
        name: "",
        timeSpan: "",
        description: "",
        link: "",
        language: "",
        courseCategory: "",
        active: true
      });
      courseAPI.listCourses(filters).then(setCourses);
    } catch (err) {
      alert(isCreateMode ? "Erro ao criar formação." : "Erro ao atualizar formação.");
    }
  };

  // Handler para desativar curso
  const handleDeactivate = async (courseId) => {
    if (!window.confirm("Tens a certeza que queres desativar esta formação?")) return;
    try {
      await courseAPI.deactivateCourse(courseId);
      courseAPI.listCourses(filters).then(setCourses);
    } catch (err) {
      alert("Erro ao desativar formação.");
    }
  };

  return (
    <PageLayout title={<FormattedMessage id="courses.list.title" defaultMessage="Listagem de Formações" />}>
      {/* Topo: botão criar formação e filtros */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-4">
        {/* Botão Criar Formação */}
        <button
          className="bg-green-600 text-white px-4 py-2 rounded"
          onClick={openCreateModal}
        >
          <FormattedMessage id="courses.create.button" defaultMessage="Criar Formação" />
        </button>
        {/* Filtros */}
        <div className="flex gap-2">
          <input
            className="border px-2 py-1 rounded"
            placeholder="Nome"
            value={name}
            onChange={e => { setName(e.target.value); setPage(1); }}
          />
          <input
            className="border px-2 py-1 rounded"
            placeholder="Duração min"
            type="number"
            min={0}
            value={minTimeSpan}
            onChange={e => { setMinTimeSpan(e.target.value); setPage(1); }}
          />
          <input
            className="border px-2 py-1 rounded"
            placeholder="Duração max"
            type="number"
            min={0}
            value={maxTimeSpan}
            onChange={e => { setMaxTimeSpan(e.target.value); setPage(1); }}
          />
          {/* Aqui depois selects para idioma, categoria, ativo */}
          <button className="bg-green-600 text-white px-3 rounded" onClick={handleExportCSV}>
            <FormattedMessage id="courses.button.excel" defaultMessage="Excel" />
          </button>
        </div>
      </div>

      {/* Loading/Error */}
      {loading && <div className="py-8 text-center text-gray-500">A carregar...</div>}
      {error && <div className="py-8 text-center text-red-600">{error}</div>}

      {/* Tabela cursos */}
      {!loading && courses.length > 0 && (
        <div className="overflow-x-auto w-full">
          <table className="min-w-full text-left border-collapse table-auto">
            <thead>
              <tr className="bg-gray-200 text-sm">
                <th className="p-2"><FormattedMessage id="courses.table.name" defaultMessage="Nome" /></th>
                <th className="p-2"><FormattedMessage id="courses.table.timeSpan" defaultMessage="Duração" /></th>
                <th className="p-2"><FormattedMessage id="courses.table.language" defaultMessage="Idioma" /></th>
                <th className="p-2"><FormattedMessage id="courses.table.category" defaultMessage="Área" /></th>
                <th className="p-2"><FormattedMessage id="courses.table.active" defaultMessage="Ativo" /></th>
                <th className="p-2"><FormattedMessage id="courses.table.actions" defaultMessage="Ações" /></th>
              </tr>
            </thead>
            <tbody>
              {courses.map((course) => (
                <tr key={course.id} className="border-b hover:bg-gray-50">
                  <td className="p-2">{course.name}</td>
                  <td className="p-2">{course.timeSpan}</td>
                  <td className="p-2">{course.language}</td>
                  <td className="p-2">{course.courseCategory}</td>
                  <td className="p-2">{course.active ? "Sim" : "Não"}</td>
                  <td className="p-2 flex gap-2">
                    <AppButton
                      variant="primary"
                      className="px-2 py-1"
                      onClick={() => openEditModal(course)}
                    >
                      <FormattedMessage id="courses.button.edit" defaultMessage="Editar" />
                    </AppButton>
                    <AppButton
                      variant="secondary"
                      className="px-2 py-1 bg-red-600 text-white hover:bg-red-700"
                      onClick={() => handleDeactivate(course.id)}
                    >
                      <FormattedMessage id="courses.button.deactivate" defaultMessage="Desativar" />
                    </AppButton>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal único para criar/editar */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={
          isCreateMode
            ? <FormattedMessage id="courses.create.title" defaultMessage="Nova Formação" />
            : <FormattedMessage id="courses.edit.title" defaultMessage="Editar Formação" />
        }
      >
        <AppForm onSubmit={handleSubmit}>
          {/* Nome */}
          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              <FormattedMessage id="courses.field.name" defaultMessage="Nome" />
            </label>
            <input
              name="name"
              className="border px-2 py-1 rounded w-full"
              value={form.name}
              onChange={handleFormChange}
              required
            />
          </div>
          {/* Duração */}
          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              <FormattedMessage id="courses.field.timeSpan" defaultMessage="Duração (horas)" />
            </label>
            <input
              name="timeSpan"
              type="number"
              min={0}
              step={0.1}
              className="border px-2 py-1 rounded w-full"
              value={form.timeSpan}
              onChange={handleFormChange}
              required
            />
          </div>
          {/* Descrição */}
          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              <FormattedMessage id="courses.field.description" defaultMessage="Descrição" />
            </label>
            <textarea
              name="description"
              className="border px-2 py-1 rounded w-full"
              value={form.description}
              onChange={handleFormChange}
              required
            />
          </div>
          {/* Link */}
          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              <FormattedMessage id="courses.field.link" defaultMessage="Link" />
            </label>
            <input
              name="link"
              className="border px-2 py-1 rounded w-full"
              value={form.link}
              onChange={handleFormChange}
              required
            />
          </div>
          {/* Idioma */}
          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              <FormattedMessage id="courses.field.language" defaultMessage="Idioma" />
            </label>
            <select
              name="language"
              className="border px-2 py-1 rounded w-full"
              value={form.language}
              onChange={handleFormChange}
              required
            >
              <option value="">--</option>
              <option value="PT">Português</option>
              <option value="EN">Inglês</option>
              <option value="IT">Italiano</option>
              <option value="FR">Francês</option>
              <option value="ES">Espanhol</option>
            </select>
          </div>
          {/* Área */}
          <div className="mb-3">
            <label className="block text-sm font-medium mb-1">
              <FormattedMessage id="courses.field.category" defaultMessage="Área" />
            </label>
            <select
              name="courseCategory"
              className="border px-2 py-1 rounded w-full"
              value={form.courseCategory}
              onChange={handleFormChange}
              required
            >
              <option value="">--</option>
              <option value="FRONTEND">Frontend</option>
              <option value="BACKEND">Backend</option>
              <option value="INFRAESTRUTURA">Infraestrutura</option>
              <option value="UX_UI">UX/UI</option>
            </select>
          </div>
          {/* Ativo */}
          <div className="mb-3 flex items-center">
            <input
              id="active"
              name="active"
              type="checkbox"
              className={`mr-2 ${isCreateMode ? "cursor-not-allowed opacity-60" : ""}`}
              checked={form.active}
              disabled={isCreateMode}
              onChange={handleFormChange}
            />
            <label htmlFor="active" className="text-sm font-medium">
              <FormattedMessage id="courses.field.active" defaultMessage="Ativo" />
            </label>
          </div>
          {/* Botões */}
          <div className="flex gap-2 justify-end mt-4">
            <AppButton
              variant="secondary"
              type="button"
              onClick={() => setModalOpen(false)}
            >
              <FormattedMessage id="modal.cancel" defaultMessage="Cancelar" />
            </AppButton>
            <AppButton
              variant="primary"
              type="submit"
            >
              <FormattedMessage
                id={isCreateMode ? "modal.save" : "modal.save"}
                defaultMessage="Guardar"
              />
            </AppButton>
          </div>
        </AppForm>
      </Modal>

      {/* Empty/Paginação pode ser igual ao UsersPage */}
      {!loading && courses.length === 0 && (
        <div className="py-8 text-center text-gray-500">
          <FormattedMessage id="courses.table.empty" defaultMessage="Nenhuma formação encontrada com estes filtros." />
        </div>
      )}
    </PageLayout>
  );
}

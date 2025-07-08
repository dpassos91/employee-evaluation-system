import { useState, useMemo, useEffect } from "react";
import PageLayout from "../components/PageLayout";
import Modal from "../components/Modal";
import { FormattedMessage } from "react-intl";
import { courseAPI } from "../api/courseAPI";
import { useNavigate } from "react-router-dom";

export default function CoursesPage() {
  // Filtros
  const [name, setName] = useState("");
  const [minTimeSpan, setMinTimeSpan] = useState("");
  const [maxTimeSpan, setMaxTimeSpan] = useState("");
  const [language, setLanguage] = useState("");
  const [category, setCategory] = useState("");
  const [active, setActive] = useState("");
  const [page, setPage] = useState(1);

  // Estado do modal de criação
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Estado do formulário de criação de formação
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
        setCourses(data); // Adapta se tiveres paginação real
        setTotalPages(1); // Adapta se tiveres paginação real
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

  // Atualiza campos do formulário de criação
  const handleFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  // Salvar nova formação
  const handleCreate = async () => {
    try {
      await courseAPI.createCourse(form);
      setShowCreateModal(false);
      setForm({
        name: "",
        timeSpan: "",
        description: "",
        link: "",
        language: "",
        courseCategory: "",
        active: true
      });
      // Reload da lista de cursos
      courseAPI.listCourses(filters).then(setCourses);
    } catch (err) {
      alert("Erro ao criar formação.");
    }
  };

  // Actions do modal
  const createActions = [
    <button key="cancel" onClick={() => setShowCreateModal(false)} className="bg-gray-200 px-4 py-2 rounded">
      <FormattedMessage id="common.cancel" defaultMessage="Cancelar" />
    </button>,
    <button key="save" type="submit" onClick={handleCreate} className="bg-blue-600 text-white px-4 py-2 rounded">
      <FormattedMessage id="courses.create.save" defaultMessage="Salvar" />
    </button>
  ];

  return (
    <PageLayout title={<FormattedMessage id="courses.list.title" defaultMessage="Listagem de Formações" />}>
      {/* Topo: botão criar formação e filtros */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-4">
        {/* Botão Criar Formação */}
        <button
          className="bg-green-600 text-white px-4 py-2 rounded"
          onClick={() => setShowCreateModal(true)}
        >
          <FormattedMessage id="courses.create.button" defaultMessage="Criar Formação" />
        </button>
        {/* Filtros (exemplo, completa conforme preferires) */}
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
                  <td className="p-2">
                    {/* Aqui podes pôr botões Editar, Desativar, etc */}
                    <button className="bg-blue-600 text-white px-2 py-1 rounded mr-2">Editar</button>
                    <button className="bg-red-600 text-white px-2 py-1 rounded">Desativar</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal de criação */}
      <Modal
  isOpen={showCreateModal}
  onClose={() => setShowCreateModal(false)}
  title={<FormattedMessage id="courses.create.title" defaultMessage="Nova Formação" />}
  actions={[
    {
      label: <FormattedMessage id="modal.cancel" defaultMessage="Cancelar" />,
      variant: "secondary",
      onClick: () => setShowCreateModal(false)
    },
    {
      label: <FormattedMessage id="modal.save" defaultMessage="Guardar" />,
      variant: "primary",
      type: "submit",
      onClick: handleCreate
    }
  ]}
>
  <form onSubmit={e => { e.preventDefault(); handleCreate(); }}>
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
        className="mr-2"
        checked={form.active}
        onChange={handleFormChange}
      />
      <label htmlFor="active" className="text-sm font-medium">
        <FormattedMessage id="courses.field.active" defaultMessage="Ativo" />
      </label>
    </div>
  </form>
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


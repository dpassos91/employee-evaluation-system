import Sidebar from "../components/Sidebar";

export default function DashboardPage() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <main className="ml-64 flex-1 p-8 pt-24">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold">Olá, Nome Apelido 👋</h2>
            <p className="text-gray-600">Pronto para continuar a crescer?</p>
          </div>
          <div className="flex space-x-4">
            <button className="text-gray-500 hover:text-red-500">
              <i className="fas fa-bell"></i>
            </button>
            <button className="text-gray-500 hover:text-red-500">
              <i className="fas fa-envelope"></i>
            </button>
            <button className="text-gray-500 hover:text-red-500">
              <i className="fas fa-globe"></i>
            </button>
          </div>
        </div>

        {/* Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <i className="fas fa-book text-red-600 mr-2"></i>
              <span className="font-bold">Formações ativas</span>
            </div>
            <p className="text-gray-700">2 formações em curso</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <i className="fas fa-file-alt text-red-600 mr-2"></i>
              <span className="font-bold">Avaliações em aberto</span>
            </div>
            <p className="text-gray-700">1 avaliação por preencher</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <i className="fas fa-calendar-check text-red-600 mr-2"></i>
              <span className="font-bold">Última avaliação</span>
            </div>
            <p className="text-gray-700">Feita a 27/05/2025</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <i className="fas fa-clock text-red-600 mr-2"></i>
              <span className="font-bold">Total de horas de formação</span>
            </div>
            <p className="text-gray-700">24h acumuladas este ano</p>
          </div>
        </div>

        {/* Agenda */}
        <div className="mb-8">
          <h3 className="font-semibold text-lg mb-2">Agenda</h3>
          <ul className="bg-white rounded-lg shadow divide-y">
            <li className="p-4">Avaliação atual termina a 29/05</li>
            <li className="p-4">Sessão de formação <span className="font-bold">UX/UI</span> a 1/06</li>
          </ul>
        </div>

        {/* Bottom Buttons */}
        <div className="flex flex-wrap gap-4">
          <button className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold flex items-center">
            <i className="fas fa-search mr-2"></i> Ver formações
          </button>
          <button className="bg-gray-200 text-gray-800 px-6 py-3 rounded-lg font-semibold flex items-center">
            <i className="fas fa-cog mr-2"></i> Definições da aplicação
          </button>
          <button className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold flex items-center">
            <i className="fas fa-file-alt mr-2"></i> Consultar avaliações
          </button>
        </div>
      </main>
    </div>
  );
}



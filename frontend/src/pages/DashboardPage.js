import Sidebar from "../components/Sidebar";
import {
  FaBell,
  FaEnvelope,
  FaGlobe,
  FaBook,
  FaFileAlt,
  FaCalendarCheck,
  FaClock,
  FaSearch,
  FaCog
} from "react-icons/fa";

export default function DashboardPage() {
  return (
    <div className="flex h-screen bg-gray-100">
      <Sidebar />
      <main className="ml-64 flex-1 p-8 pl-[105px] pr-[105px] pt-24">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold">Olá, Nome Apelido 👋</h2>
            <p className="text-gray-600">Pronto para continuar a crescer?</p>
          </div>
          
        </div>

        {/* Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <FaBook className="text-red-600 mr-2" />
              <span className="font-bold">Formações ativas</span>
            </div>
            <p className="text-gray-700">2 formações em curso</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <FaFileAlt className="text-red-600 mr-2" />
              <span className="font-bold">Avaliações em aberto</span>
            </div>
            <p className="text-gray-700">1 avaliação por preencher</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <FaCalendarCheck className="text-red-600 mr-2" />
              <span className="font-bold">Última avaliação</span>
            </div>
            <p className="text-gray-700">Feita a 27/05/2025</p>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center mb-2">
              <FaClock className="text-red-600 mr-2" />
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
            <li className="p-4">
              Sessão de formação <span className="font-bold">UX/UI</span> a 1/06
            </li>
          </ul>
        </div>

        {/* Bottom Buttons */}
        <div className="flex flex-wrap gap-20 justify-center">
          <button className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold flex items-center">
            <FaSearch className="mr-2" /> Ver formações
          </button>
          <button className="bg-gray-200 text-gray-800 px-6 py-3 rounded-lg font-semibold flex items-center">
            <FaCog className="mr-2" /> Definições da aplicação
          </button>
          <button className="bg-red-600 text-white px-6 py-3 rounded-lg font-semibold flex items-center">
            <FaFileAlt className="mr-2" /> Consultar avaliações
          </button>
        </div>
      </main>
    </div>
  );
}




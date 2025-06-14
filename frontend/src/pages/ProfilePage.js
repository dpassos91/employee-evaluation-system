import Sidebar from "../components/Sidebar"; // Ajusta o caminho se necessário
import profilePlaceholder from "../images/profile_icon.png"; // Foto de perfil default
import { BellIcon, EnvelopeIcon, GlobeAltIcon, LockClosedIcon } from "@heroicons/react/24/outline";

export default function ProfilePage() {
  // Aqui podes ir buscar o utilizador autenticado via contexto, hook, etc.
  // Exemplo: const user = userStore((state) => state.user);

  return (
    <div className="flex min-h-screen bg-[#f7f7f7]">
      {/* Sidebar */}
      <Sidebar />

      {/* Conteúdo principal */}
      <main className="flex-1 p-10 flex flex-col items-center">
        {/* Header */}
        <section className="w-full flex justify-end gap-6 mb-8">
        </section>

        {/* Perfil */}
        <section className="w-full max-w-5xl flex gap-16">
          {/* Coluna esquerda */}
          <div className="flex flex-col items-center min-w-[260px]">
            <div className="w-40 h-40 rounded-full overflow-hidden mb-2 border-4 border-[#D41C1C] bg-white">
              <img
                src={profilePlaceholder}
                alt="Foto de perfil"
                className="object-cover w-full h-full"
              />
            </div>
            <button className="mb-8 text-sm font-medium text-[#333]">Alterar fotografia</button>
            <button className="w-56 bg-[#D41C1C] text-white rounded shadow-md px-4 py-3 font-medium mb-4">
              Histórico de formações
            </button>
            <button className="w-56 bg-[#D41C1C] text-white rounded shadow-md px-4 py-3 font-medium">
              Histórico de avaliações
            </button>
          </div>

          {/* Coluna central */}
          <form className="flex-1 flex flex-col gap-4">
            <h1 className="text-4xl font-bold mb-6">Perfil</h1>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col">
                <label className="font-semibold">Email</label>
                <input type="email" className="border border-[#D41C1C] rounded px-2 py-1" />
              </div>
              <div className="flex flex-col">
                <label className="font-semibold">Nome</label>
                <input type="text" className="border border-[#D41C1C] rounded px-2 py-1" />
              </div>
              <div className="flex flex-col">
                <label className="font-semibold">Apelido</label>
                <input type="text" className="border border-[#D41C1C] rounded px-2 py-1" />
              </div>
              <div className="flex flex-col">
                <label className="font-semibold">Local de Trabalho habitual</label>
                <select className="border border-[#D41C1C] rounded px-2 py-1">
                  <option>Boston</option>
                  <option>Coimbra</option>
                  <option>Lisboa</option>
                  <option>Munich</option>
                  <option>Porto</option>
                  <option>Southampton</option>
                  <option>Viseu</option>
                  {/* outras opções */}
                </select>
              </div>
              <div className="flex flex-col">
                <label className="font-semibold">Contacto telefónico</label>
                <input type="tel" className="border border-[#D41C1C] rounded px-2 py-1" />
              </div>
              <div className="flex flex-col">
                <label className="font-semibold">Morada</label>
                <input type="text" className="border border-[#D41C1C] rounded px-2 py-1" />
              </div>
            </div>
            <button
              type="button"
              className="w-full flex items-center justify-center gap-2 bg-[#D41C1C] text-white rounded px-4 py-2 mt-4 mb-2"
            >
              <LockClosedIcon className="w-5 h-5" /> Alterar password
            </button>
            <div className="flex flex-col">
              <label className="font-semibold">Gestor</label>
              <input type="text" className="border border-[#D41C1C] rounded px-2 py-1" />
            </div>
            <button
              type="submit"
              className="w-full bg-[#D41C1C] text-white rounded shadow-lg px-4 py-4 text-2xl font-bold mt-6"
            >
              Guardar
            </button>
          </form>

          {/* Coluna direita */}
          <div className="flex-1 flex flex-col">
            <label className="font-semibold mb-1 text-center">Biografia</label>
            <textarea className="border border-[#D41C1C] rounded w-full h-full min-h-[350px] px-3 py-2" />
          </div>
        </section>
      </main>
    </div>
  );
}



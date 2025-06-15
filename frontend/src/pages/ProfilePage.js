import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import profilePlaceholder from "../images/profile_icon.png";
import { LockClosedIcon } from "@heroicons/react/24/outline";

export default function ProfilePage() {
  return (
    <PageLayout title="Perfil">
      <section className="w-full max-w-5.5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
        {/* Coluna 1: Foto + ações secundárias */}
        <div className="flex flex-col items-center md:items-start">
          <div className="w-28 h-28 rounded-full overflow-hidden mb-2 border-2 border-[#D41C1C] bg-white">
            <img
              src={profilePlaceholder}
              alt="Foto de perfil"
              className="object-cover w-full h-full"
            />
          </div>
          <AppButton variant="secondary" className="mb-4 w-full px-3 py-1.5 text-sm">
            Alterar fotografia
          </AppButton>
          <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm">
            Histórico de formações
          </AppButton>
          <AppButton variant="secondary" className="w-full px-3 py-1.5 mt-2 text-sm">
            Histórico de avaliações
          </AppButton>
        </div>

        {/* Coluna 2: Formulário e Biografia */}
        <div className="flex flex-col w-full pl-12">
          <form className="flex flex-col gap-2 w-full">
            {/* Email e Gestor */}
            <div className="grid grid-cols-2 gap-2">
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Email</label>
                <input
                  type="email"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Gestor</label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
            </div>

            {/* Nome, Apelido, Local */}
            <div className="grid grid-cols-3 gap-2">
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Nome</label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Apelido</label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Local de Trabalho habitual</label>
                <select className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm">
                  <option> </option>
                  <option>Boston</option>
                  <option>Coimbra</option>
                  <option>Lisboa</option>
                  <option>Munich</option>
                  <option>Porto</option>
                  <option>Southampton</option>
                  <option>Viseu</option>
                </select>
              </div>
            </div>

            {/* Morada, Código Postal, Contacto */}
            <div className="grid grid-cols-3 gap-2 items-end">
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Morada</label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Código Postal</label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm text-gray-700 mb-1">Contacto telefónico</label>
                <input
                  type="tel"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
            </div>

            {/* Botões lado a lado */}
<div className="grid grid-cols-2 gap-2 mt-4 w-full">
  <AppButton
    type="button"
    variant="secondary"
    className="w-full flex items-center justify-center gap-2 px-4 py-2 text-base"
  >
    <LockClosedIcon className="w-5 h-5" /> Alterar password
  </AppButton>
  <AppButton
    type="submit"
    variant="primary"
    className="w-full flex items-center justify-center px-4 py-2 text-base font-semibold"
  >
    Guardar
  </AppButton>
</div>
          </form>

          {/* Biografia */}
          <div className="flex flex-col mt-4 w-full">
            <label className="font-medium mb-2 text-gray-700 text-base text-left">Biografia</label>
            <textarea
              className="border border-gray-300 focus:border-[#D41C1C] rounded w-full min-h-[120px] max-h-[220px] px-3 py-2 bg-white text-sm"
            />
          </div>
        </div>
      </section>
    </PageLayout>
  );
}






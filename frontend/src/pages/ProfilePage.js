import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import profilePlaceholder from "../images/profile_icon.png";
import { LockClosedIcon } from "@heroicons/react/24/outline";
import { FormattedMessage } from "react-intl";

export default function ProfilePage() {
  return (
    <PageLayout title={<FormattedMessage id="profile.title" defaultMessage="Perfil" />}>
      <section className="w-full max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
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
            <FormattedMessage id="profile.changePhoto" defaultMessage="Alterar fotografia" />
          </AppButton>
          <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm">
            <FormattedMessage id="profile.trainingHistory" defaultMessage="Histórico de formações" />
          </AppButton>
          <AppButton variant="secondary" className="w-full px-3 py-1.5 mt-2 text-sm">
            <FormattedMessage id="profile.evaluationHistory" defaultMessage="Histórico de avaliações" />
          </AppButton>
        </div>

        {/* Coluna 2: Formulário e Biografia */}
        <div className="flex flex-col w-full pl-12">
          <form className="flex flex-col gap-2 w-full">
            {/* Email e Gestor */}
            <div className="grid grid-cols-2 gap-2">
              <div className="flex flex-col">
                <label className="font-medium mb-2 text-gray-700 mb-1">
                  <FormattedMessage id="profile.email" defaultMessage="Email" />
                </label>
                <input
                  type="email"
                  value="exemplo@email.com"
                  disabled
                  className="border border-gray-300 rounded px-2 py-1.5 text-sm bg-gray-100 text-gray-700 cursor-not-allowed"
                />
              </div>
              <div className="flex flex-col">
                <label className="font-medium mb-2 text-gray-700 mb-1">
                  <FormattedMessage id="profile.manager" defaultMessage="Gestor" />
                </label>
                <input
                  type="text"
                  value="Joana Ferreira"
                  disabled
                  className="border border-gray-300 rounded px-2 py-1.5 text-sm bg-gray-100 text-gray-700 cursor-not-allowed"
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-2 items-start">
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.name" defaultMessage="Nome" />
                </label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.surname" defaultMessage="Apelido" />
                </label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.workplace" defaultMessage="Local de Trabalho" />
                </label>
                <select
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                >
                  <option value=""> </option>
                  <option value="Boston">Boston</option>
                  <option value="Coimbra">Coimbra</option>
                  <option value="Lisboa">Lisboa</option>
                  <option value="Munich">Munich</option>
                  <option value="Porto">Porto</option>
                  <option value="Southampton">Southampton</option>
                  <option value="Viseu">Viseu</option>
                </select>
              </div>
            </div>

            {/* Morada, Código Postal, Contacto */}
            <div className="grid grid-cols-3 gap-2 items-end">
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.address" defaultMessage="Morada" />
                </label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.zipcode" defaultMessage="Código Postal" />
                </label>
                <input
                  type="text"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.phone" defaultMessage="Contacto telefónico" />
                </label>
                <input
                  type="tel"
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
            </div>

            {/* Biografia */}
            <div className="flex flex-col mt-4 w-full">
              <label className="font-medium mb-2 text-gray-700 text-base text-left">
                <FormattedMessage id="profile.biography" defaultMessage="Biografia" />
              </label>
              <textarea
                className="border border-gray-300 focus:border-[#D41C1C] rounded w-full min-h-[120px] max-h-[220px] px-3 py-2 bg-white text-sm"
              />
            </div>

            {/* Botões lado a lado */}
            <div className="grid grid-cols-2 gap-2 mt-4 w-full">
              <AppButton
                type="button"
                variant="secondary"
                className="w-full flex items-center justify-center gap-2 px-4 py-2 text-base"
              >
                <LockClosedIcon className="w-5 h-5" />
                <FormattedMessage id="profile.changePassword" defaultMessage="Alterar password" />
              </AppButton>
              <AppButton
                type="submit"
                variant="primary"
                className="w-full flex items-center justify-center px-4 py-2 text-base font-semibold"
              >
                <FormattedMessage id="profile.save" defaultMessage="Guardar" />
              </AppButton>
            </div>
          </form>
        </div>
      </section>
    </PageLayout>
  );
}







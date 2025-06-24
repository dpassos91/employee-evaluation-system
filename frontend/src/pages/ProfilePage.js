import { useState, useEffect, useCallback } from "react";
import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import profilePlaceholder from "../images/profile_icon.png";
import { LockClosedIcon } from "@heroicons/react/24/outline";
import { toast } from "react-toastify";
import { userStore } from "../stores/userStore";
import { useIntl, FormattedMessage } from "react-intl";
import { profileAPI } from "../api/profileAPI";
import { fieldLabelKeys } from "../utils/fieldLabels"; // Importa os rótulos dos campos

export default function ProfilePage() {
  const { user, profileComplete, missingFields } = userStore();
  const { formatMessage } = useIntl();

  // 1. Controlar os campos do formulário
const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [address, setAddress] = useState("");
  const [phone, setPhone] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [usualWorkplace, setUsualWorkplace] = useState("");
  const [bio, setBio] = useState("");

  // Função reutilizável para buscar perfil (useCallback evita warnings no React)
  const fetchProfile = useCallback(async () => {
    
    if (!user?.email) return;
    try {
      const sessionToken = sessionStorage.getItem("authToken");
      const profile = await profileAPI.getProfile(user.email, sessionToken);
      console.log("PROFILE JSON:", profile);

      setFirstName(profile.firstName || "");
      setLastName(profile.lastName || "");
      setAddress(profile.address || "");
      setPhone(profile.phone || "");
      setBirthDate(profile.birthDate || "");
      setUsualWorkplace(profile.usualWorkplace || "");
      setBio(profile.bio || "");
    } catch (err) {
      toast.error(
        formatMessage({
          id: "profile.fetch.error",
          defaultMessage: "Erro ao carregar dados do perfil!",
        })
      );
    }
  }, [user?.email, formatMessage]);

  // Chama fetchProfile ao montar e quando o email muda
  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  // Toast do perfil incompleto (igual ao teu)
  useEffect(() => {
    if (profileComplete === false) {
      const missingLabels = (missingFields || [])
        .map((field) =>
          fieldLabelKeys[field]
            ? formatMessage({ id: fieldLabelKeys[field] })
            : field // fallback se faltar tradução
        )
        .join(", ");

      toast.info(
        formatMessage(
          {
            id: "profile.incomplete.fields",
            defaultMessage: "Por favor, preencha todos os dados obrigatórios do perfil: {fields}",
          },
          { fields: missingLabels }
        )
      );
    }
  }, [profileComplete, missingFields, formatMessage]);

  // Handler de submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    const sessionToken = sessionStorage.getItem("authToken");
    const profileData = {
      firstName,
      lastName,
      address,
      phone,
      birthDate: birthDate ? `${birthDate}T00:00:00` : null,
      usualWorkplace,
      bio,
    };

    try {
      console.log(profileData)
      await profileAPI.updateProfile(user.email, profileData, sessionToken);
      
      toast.success(
        formatMessage({
          id: "profile.update.success",
          defaultMessage: "Perfil atualizado com sucesso!",
        })
      );
      // Refresca os dados do perfil após update
      fetchProfile();
      console.log(profileData)
    } catch (err) {
      toast.error(
        formatMessage({
          id: "profile.update.error",
          defaultMessage: "Erro ao atualizar perfil!",
        })
      );
    }
  };

  return (
    <PageLayout>
      <section className="w-full max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
        {/* Coluna 1: Foto + ações secundárias */}
        <div className="flex flex-col items-center w-full">
          <h2 className="text-3xl font-bold text-center mb-2 w-full">
            <FormattedMessage id="profile.title" defaultMessage="Perfil" />
          </h2>
          <div className="w-28 h-28 rounded-full overflow-hidden mb-8 border-2 border-[#D41C1C] bg-white mx-auto mt-10">
            <img
              src={profilePlaceholder}
              alt="Foto de perfil"
              className="object-cover w-full h-full"
            />
          </div>
          {/* Botões com gap regular */}
          <div className="flex flex-col gap-3 w-full mt-6">
            <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm justify-center text-center">
              <FormattedMessage id="profile.changePhoto" defaultMessage="Alterar fotografia" />
            </AppButton>
            <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm justify-center text-center">
              <FormattedMessage id="profile.trainingHistory" defaultMessage="Histórico de formações" />
            </AppButton>
            <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm justify-center text-center">
              <FormattedMessage id="profile.evaluationHistory" defaultMessage="Histórico de avaliações" />
            </AppButton>
          </div>
        </div>

        {/* Coluna 2: Formulário e Biografia */}
        <div className="flex flex-col w-full pl-12">
          <form className="flex flex-col gap-2 w-full" onSubmit={handleSubmit}>
            {/* Email e Gestor */}
            <div className="grid grid-cols-2 gap-2">
              <div className="flex flex-col">
                <label className="font-medium mb-2 text-gray-700 mb-1">
                  <FormattedMessage id="profile.email" defaultMessage="Email" />
                </label>
                <input
                  type="email"
                  value={user.email}
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
                  value={user.managerName || ""}
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
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.surname" defaultMessage="Apelido" />
                </label>
                <input
                  type="text"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.workplace" defaultMessage="Local de Trabalho" />
                </label>
                <select
                  value={usualWorkplace}
                  onChange={(e) => setUsualWorkplace(e.target.value)}
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                >
                  <option value=""> </option>
                  <option value="BOSTON">Boston</option>
                  <option value="COIMBRA">Coimbra</option>
                  <option value="LISBOA">Lisboa</option>
                  <option value="MUNICH">Munich</option>
                  <option value="PORTO">Porto</option>
                  <option value="SOUTHAMPTON">Southampton</option>
                  <option value="VISEU">Viseu</option>
                </select>
              </div>
            </div>

            {/* Morada, Aniversário, Contacto */}
            <div className="grid grid-cols-3 gap-2 items-end">
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.address" defaultMessage="Morada" />
                </label>
                <input
                  type="text"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.birthdate" defaultMessage="Data de nascimento" />
                </label>
                <input
                  type="date"
                  value={birthDate}
                  onChange={(e) => setBirthDate(e.target.value)}
                  className="border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.phone" defaultMessage="Contacto telefónico" />
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
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
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                className="border border-gray-300 focus:border-[#D41C1C] rounded w-full min-h-[155px] max-h-[220px] px-3 py-2 bg-white text-sm"
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








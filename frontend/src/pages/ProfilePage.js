/**
 * ProfilePage
 * User profile page supporting both self and other users' profiles.
 * If the user is viewing their own profile or is an admin, they can edit the profile.
 * Otherwise, the profile is in read-only mode, with an optional "Send Message" button for interaction.
 *
 * Route: /profile/:userId
 */

import { useState, useEffect, useCallback } from "react";
import PageLayout from "../components/PageLayout";
import AppButton from "../components/AppButton";
import profilePlaceholder from "../images/profile_icon.png";
import { LockClosedIcon, ChatBubbleBottomCenterTextIcon } from "@heroicons/react/24/outline";
import { toast } from "react-toastify";
import { userStore } from "../stores/userStore";
import { useIntl, FormattedMessage } from "react-intl";
import { profileAPI } from "../api/profileAPI";
import { authAPI } from "../api/authAPI";
import { fieldLabelKeys } from "../utils/fieldLabels";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import Modal from "../components/Modal";

export default function ProfilePage() {
  /**
   * Authenticated user info from Zustand store.
   */
  const user = userStore((state) => state.user);

  /**
   * Zustand setter for user info.
   */
  const setUser = userStore((state) => state.setUser);

  /**
   * Zustand setters for profile completeness and missing fields.
   */
  const setProfileComplete = userStore.getState().setProfileComplete;
  const setMissingFields = userStore.getState().setMissingFields;

  const { formatMessage } = useIntl();
  const { userId } = useParams();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const location = useLocation();
  const profileOwnerEmail = location.state?.profileOwnerEmail;

  // Password modal states
  const [isPasswordModalOpen, setPasswordModalOpen] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  // Photo upload states
  const [selectedPhoto, setSelectedPhoto] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isPreviewModalOpen, setPreviewModalOpen] = useState(false);
  const photoUrl = profileAPI.getPhoto(profile?.photograph);

  // True if current user is viewing own profile or is admin (can edit)
  const canEdit = user?.role === "ADMIN" || String(user?.id) === String(userId);

  /**
   * Fetch profile data from API.
   * Only update global state if viewing own profile AND values actually change, to avoid render cycles.
   */
  const fetchProfile = useCallback(async () => {
    setLoading(true);
    try {
      const targetUserId = userId || user?.id;
      if (!targetUserId) throw new Error("No userId provided");

      const sessionToken = sessionStorage.getItem("authToken");
      const profileData = await profileAPI.getProfileById(targetUserId, sessionToken);
      setProfile(profileData);

      // Only update Zustand if the logged user is viewing their own profile
      if (String(user?.id) === String(targetUserId)) {
        // Only update Zustand if values have actually changed
        const currentProfileComplete = userStore.getState().profileComplete;
        const currentMissingFields = userStore.getState().missingFields;

        if (currentProfileComplete !== profileData.profileComplete) {
          setProfileComplete(profileData.profileComplete);
        }
        // Deep compare for arrays (can use lodash.isEqual for large arrays)
        if (
          JSON.stringify(currentMissingFields) !== JSON.stringify(profileData.missingFields)
        ) {
          setMissingFields(profileData.missingFields);
        }
      }
    } catch (err) {
      toast.error(
        formatMessage({
          id: "profile.fetch.error",
          defaultMessage: "Error loading profile data!",
        })
      );
    } finally {
      setLoading(false);
    }
  }, [userId, user?.id, formatMessage]);

  /**
   * Triggers fetching the profile whenever the userId in the route changes.
   */
  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  /**
   * Display a toast with missing profile fields if own profile is incomplete.
   * This effect only depends on values - never calls setters or fetch!
   */
  useEffect(() => {
    if (canEdit && userStore.getState().profileComplete === false) {
      const missingFields = userStore.getState().missingFields;
      const missingLabels = (missingFields || [])
        .map((field) =>
          fieldLabelKeys[field]
            ? formatMessage({ id: fieldLabelKeys[field] })
            : field
        )
        .join(", ");
      toast.info(
        formatMessage(
          {
            id: "profile.incomplete.fields",
            defaultMessage: "Please complete all required profile fields: {fields}",
          },
          { fields: missingLabels }
        )
      );
    }
  }, [
    canEdit,
    userStore.getState().profileComplete,
    userStore.getState().missingFields,
    formatMessage,
  ]);

  /**
   * Local profile edit handlers (used for editing inputs)
   */
  const handleChange = (field, value) => {
    setProfile((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  /**
   * Handler for form submission to save profile changes.
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const sessionToken = sessionStorage.getItem("authToken");
      // Use the profile owner's email if editing another user's profile, otherwise use the logged-in user's email
      const emailToUpdate = profileOwnerEmail || user.email;
      await profileAPI.updateProfile(emailToUpdate, profile, sessionToken);
      console.log("Profile updated:", profile);
      toast.success(
        formatMessage({
          id: "profile.update.success",
          defaultMessage: "Profile updated successfully!",
        })
      );
      await fetchProfile(); // Refresh profile data after save

      // If the currently logged-in user just updated their own profile,
      // update the global user state so components like Sidebar reflect the new name immediately
      if (String(user?.id) === String(userId) && profile) {
        setUser({
          ...user,
          firstName: profile.firstName, 
          lastName: profile.lastName,   
          photograph: profile.photograph
        });
      }
    } catch (err) {
      toast.error(
        formatMessage({
          id: "profile.update.error",
          defaultMessage: "Error updating profile!",
        })
      );
    }
  };

  /**
   * Handler for changing the user's password via modal.
   */
  const handleChangePassword = async (e) => {
    e.preventDefault();

    if (newPassword.length < 6) {
      toast.error(formatMessage({ id: "profile.password.short", defaultMessage: "Password must have at least 6 characters." }));
      return;
    }
    if (newPassword !== confirmPassword) {
      toast.error(formatMessage({ id: "profile.password.mismatch", defaultMessage: "Passwords do not match." }));
      return;
    }
    try {
      const sessionToken = sessionStorage.getItem("authToken");
      // Always use the profile owner's email
      const emailToUpdate = profileOwnerEmail || user.email;
      await authAPI.changePassword(emailToUpdate, currentPassword, newPassword, sessionToken);
      toast.success(formatMessage({ id: "profile.password.success", defaultMessage: "Password updated successfully!" }));
      setPasswordModalOpen(false);
      setCurrentPassword(""); setNewPassword(""); setConfirmPassword("");
    } catch (err) {
      console.log(err)
      toast.error(formatMessage({ id: "profile.password.error", defaultMessage: "Error updating password!" }));
    }
  };

  /**
   * Handler to start a chat with this user.
   */
  const handleSendMessage = () => {
    navigate(`/chat?userId=${userId}`);
  };

  if (loading || !profile)
    return (
      <PageLayout>
        <div className="text-center text-gray-500 py-20">
          <FormattedMessage id="profile.loading" defaultMessage="Loading profile..." />
        </div>
      </PageLayout>
    );

 /**
 * Handles photo file selection and opens preview modal.
 * @param {Event} e - Input file change event
 */
const handlePhotoChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    setSelectedPhoto(file);
    setPhotoPreview(URL.createObjectURL(file)); // Show preview
    setPreviewModalOpen(true); // <-- ABRE MODAL AUTOMATICAMENTE
  }
};

/**
 * Handles uploading the selected photo to the backend server.
 * Uses FormData to send the file via multipart/form-data POST request.
 * On success, refreshes the profile to show the new photo.
 */
const handlePhotoUpload = async () => {
  if (!selectedPhoto) {
    toast.error("Por favor selecione uma foto primeiro.");
    return;
  }
  setIsUploading(true);
  try {
    const sessionToken = sessionStorage.getItem("authToken");
    const emailToUpdate = profileOwnerEmail || user.email;
    const response = await profileAPI.uploadPhoto(emailToUpdate, selectedPhoto, sessionToken);
    toast.success("Foto atualizada com sucesso!");

    setUser({
      ...user,
      photograph: response.fileName // isto vem do backend no JSON!
    });
    
    setPhotoPreview(null);
    setSelectedPhoto(null);
    await fetchProfile(); // Atualiza o perfil com a nova foto
  } catch (err) {
    toast.error("Erro ao fazer upload da foto!");
  } finally {
    setIsUploading(false);
  }
};


  return (
    <PageLayout>
      <section className="w-full max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
        {/* Column 1: Photo + secondary actions */}
        <div className="flex flex-col items-center w-full">
          <h2 className="text-3xl font-bold text-center mb-2 w-full">
            <FormattedMessage id="profile.title" defaultMessage="Profile" />
          </h2>
<div className="w-28 h-28 rounded-full overflow-hidden border-2 border-[#D41C1C] bg-white flex items-center justify-center">
            {console.log('profile.photograph:', profile.photograph)}

<img
  src={
    profile.photograph && profile.photograph.trim() !== ""
      ? photoUrl
      : profilePlaceholder
  }
  alt="Profile"
      className="w-full h-full object-cover"
    style={{ display: "block" }}
  onError={(e) => {
    e.target.onerror = null; // remove loop
    e.target.src = profilePlaceholder;
  }}
/>
          </div>

          {/* Actions */}
<div className="flex flex-col gap-3 w-full mt-6">
  {canEdit ? (
    <>
      {/* Hidden file input */}
      <input
        id="profile-photo-input"
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handlePhotoChange}
      />
      {/* Change Photo Button */}
      <AppButton
        variant="secondary"
        className="w-full px-3 py-1.5 text-sm justify-center text-center"
        onClick={() => document.getElementById("profile-photo-input").click()}
        disabled={isUploading}
      >
        <FormattedMessage id="profile.changePhoto" defaultMessage="Change Photo" />
      </AppButton>

      <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm justify-center text-center">
        <FormattedMessage id="profile.trainingHistory" defaultMessage="Training History" />
      </AppButton>
      <AppButton variant="secondary" className="w-full px-3 py-1.5 text-sm justify-center text-center">
        <FormattedMessage id="profile.evaluationHistory" defaultMessage="Evaluation History" />
      </AppButton>
    </>
  ) : (
    // If not edit, show "Send Message" only
    <AppButton
      variant="primary"
      className="w-full px-3 py-1.5 text-sm justify-center text-center"
      onClick={handleSendMessage}
    >
      <ChatBubbleBottomCenterTextIcon className="w-5 h-5 mr-2" />
      <FormattedMessage id="profile.message" defaultMessage="Send Message" />
    </AppButton>
  )}
</div>
</div>

        {/* Column 2: Form / Info */}
        <div className="flex flex-col w-full pl-12">
          <form
            className="flex flex-col gap-2 w-full"
            onSubmit={canEdit ? handleSubmit : (e) => e.preventDefault()}
          >
            {/* Email and Manager (always read-only) */}
            <div className="grid grid-cols-2 gap-2">
              <div className="flex flex-col">
                <label className="font-medium mb-2 text-gray-700 mb-1">
                  <FormattedMessage id="profile.email" defaultMessage="Email" />
                </label>
                <input
                  type="email"
                  value={profileOwnerEmail || user.email || ""}
                  disabled
                  className="border border-gray-300 rounded px-2 py-1.5 text-sm bg-gray-100 text-gray-700 cursor-not-allowed"
                />
              </div>
              <div className="flex flex-col">
                <label className="font-medium mb-2 text-gray-700 mb-1">
                  <FormattedMessage id="profile.manager" defaultMessage="Manager" />
                </label>
                <input
                  type="text"
                  value={profile.managerName || ""}
                  disabled
                  className="border border-gray-300 rounded px-2 py-1.5 text-sm bg-gray-100 text-gray-700 cursor-not-allowed"
                />
              </div>
            </div>

            {/* Main info */}
            <div className="grid grid-cols-3 gap-2 items-start">
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.name" defaultMessage="First Name" />
                </label>
                <input
                  type="text"
                  value={profile.firstName}
                  onChange={(e) => handleChange("firstName", e.target.value)}
                  className={`border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm ${
                    !canEdit ? "bg-gray-100 cursor-not-allowed" : ""
                  }`}
                  disabled={!canEdit}
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.surname" defaultMessage="Last Name" />
                </label>
                <input
                  type="text"
                  value={profile.lastName}
                  onChange={(e) => handleChange("lastName", e.target.value)}
                  className={`border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm ${
                    !canEdit ? "bg-gray-100 cursor-not-allowed" : ""
                  }`}
                  disabled={!canEdit}
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.workplace" defaultMessage="Workplace" />
                </label>
                <select
                  value={profile.usualWorkplace}
                  onChange={(e) => handleChange("usualWorkplace", e.target.value)}
                  className={`border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm ${
                    !canEdit ? "bg-gray-100 cursor-not-allowed" : ""
                  }`}
                  disabled={!canEdit}
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

            {/* Address, Birth Date, Phone */}
            <div className="grid grid-cols-3 gap-2 items-end">
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.address" defaultMessage="Address" />
                </label>
                <input
                  type="text"
                  value={profile.address || ""}
                  onChange={(e) => handleChange("address", e.target.value)}
                  className={`border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm ${
                    !canEdit ? "bg-gray-100 cursor-not-allowed" : ""
                  }`}
                  disabled={!canEdit}
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.birthdate" defaultMessage="Birth Date" />
                </label>
                <input
                  type="date"
                  value={profile.birthDate || ""}
                  onChange={(e) => handleChange("birthDate", e.target.value)}
                  className={`border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm ${
                    !canEdit ? "bg-gray-100 cursor-not-allowed" : ""
                  }`}
                  disabled={!canEdit}
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-bold text-gray-700 mb-1">
                  <FormattedMessage id="profile.phone" defaultMessage="Phone" />
                </label>
                <input
                  type="tel"
                  value={profile.phone || ""}
                  onChange={(e) => handleChange("phone", e.target.value)}
                  className={`border border-gray-300 focus:border-[#D41C1C] rounded px-2 py-1.5 text-sm ${
                    !canEdit ? "bg-gray-100 cursor-not-allowed" : ""
                  }`}
                  disabled={!canEdit}
                />
              </div>
            </div>

            {/* Bio */}
            <div className="flex flex-col mt-4 w-full">
              <label className="font-medium mb-2 text-gray-700 text-base text-left">
                <FormattedMessage id="profile.biography" defaultMessage="Biography" />
              </label>
              <textarea
                value={profile.bio || ""}
                onChange={(e) => handleChange("bio", e.target.value)}
                className={`border border-gray-300 focus:border-[#D41C1C] rounded w-full min-h-[155px] max-h-[220px] px-3 py-2 text-sm ${
                  !canEdit ? "bg-gray-100 cursor-not-allowed" : "bg-white"
                }`}
                disabled={!canEdit}
              />
            </div>

            {/* Buttons (only for editable profile) */}
            {canEdit && (
              <div className="grid grid-cols-2 gap-2 mt-4 w-full">
                <AppButton
                  type="button"
                  variant="secondary"
                  className="w-full flex items-center justify-center gap-2 px-4 py-2 text-base"
                  onClick={() => setPasswordModalOpen(true)}
                >
                  <LockClosedIcon className="w-5 h-5" />
                  <FormattedMessage id="profile.changePassword" defaultMessage="Change Password" />
                </AppButton>
                <AppButton
                  type="submit"
                  variant="primary"
                  className="w-full flex items-center justify-center px-4 py-2 text-base font-semibold"
                >
                  <FormattedMessage id="profile.save" defaultMessage="Save" />
                </AppButton>
              </div>
            )}
          </form>
          {/* Password Modal */}
          <Modal
            isOpen={isPasswordModalOpen}
            onClose={() => setPasswordModalOpen(false)}
            title={formatMessage({ id: "profile.changePassword", defaultMessage: "Alterar password" })}
            actions={[
              <AppButton key="cancel" variant="secondary" onClick={() => setPasswordModalOpen(false)}>
                <FormattedMessage id="modal.cancel" defaultMessage="Cancelar" />
              </AppButton>,
              <AppButton key="save" variant="primary" type="submit" onClick={handleChangePassword}>
                <FormattedMessage id="modal.save" defaultMessage="Guardar" />
              </AppButton>,
            ]}
          >
            <form onSubmit={handleChangePassword}>
              <div className="mb-3">
                <label className="block text-sm font-medium mb-1">
                  <FormattedMessage id="profile.currentPassword" defaultMessage="Password atual" />
                </label>
                <input
                  type="password"
                  className="border px-2 py-1 rounded w-full"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="block text-sm font-medium mb-1">
                  <FormattedMessage id="profile.newPassword" defaultMessage="Nova password" />
                </label>
                <input
                  type="password"
                  className="border px-2 py-1 rounded w-full"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="block text-sm font-medium mb-1">
                  <FormattedMessage id="profile.confirmPassword" defaultMessage="Confirmar password" />
                </label>
                <input
                  type="password"
                  className="border px-2 py-1 rounded w-full"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
              </div>
            </form>
          </Modal>
        </div>
      </section>
<Modal
  isOpen={!!photoPreview}
  onClose={() => {
    setPhotoPreview(null);
    setSelectedPhoto(null);
  }}
  // Centered title with bold text
  title={
    <div className="w-full text-center font-bold">
      <FormattedMessage id="profile.previewPhoto" defaultMessage="Preview Photo" />
    </div>
  }
>
  {photoPreview && (
    <img
      src={photoPreview}
      alt="Preview"
      className="w-48 h-48 rounded-full border-2 object-cover mx-auto my-4"
    />
  )}

  {/* Centered buttons */}
  <div className="flex justify-center gap-3 mt-4">
    <AppButton
      variant="secondary"
      onClick={() => {
        setPhotoPreview(null);
        setSelectedPhoto(null);
      }}
    >
      <FormattedMessage id="modal.cancel" defaultMessage="Cancel" />
    </AppButton>
    <AppButton
      variant="secondary"
      onClick={() => document.getElementById("profile-photo-input").click()}
    >
      <FormattedMessage id="profile.chooseOtherPhoto" defaultMessage="Choose Another Photo" />
    </AppButton>
    <AppButton
      variant="primary"
      onClick={handlePhotoUpload}
      disabled={isUploading}
    >
      {isUploading
        ? <FormattedMessage id="profile.uploading" defaultMessage="Uploading..." />
        : <FormattedMessage id="profile.savePhoto" defaultMessage="Save Photo" />}
    </AppButton>
  </div>
</Modal>
    </PageLayout>
  );
}










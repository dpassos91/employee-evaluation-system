import { useState, useEffect } from "react";
import PageLayout from "../components/PageLayout";
import { FormattedMessage, useIntl } from "react-intl";
import { settingsAPI } from "../api/settingsAPI";
import AppButton from "../components/AppButton";
import { toast } from "react-toastify";
import { evaluationAPI } from "../api/evaluationAPI";

/**
 * SettingsPage
 * Page used for administrative platform settings like token timeouts or role management.
 * Only accessible by ADMIN users.
 */
export default function SettingsPage() {
  const [confirmationTimeout, setConfirmationTimeout] = useState("");
  const [recoveryTimeout, setRecoveryTimeout] = useState("");
  const [sessionTimeout, setSessionTimeout] = useState("");
  const { formatMessage } = useIntl();
  const [potentialAdmins, setPotentialAdmins] = useState([]);
const [selectedAdminEmail, setSelectedAdminEmail] = useState("");

  // Fetch timeouts from the backend
  useEffect(() => {
    const fetchTimeouts = async () => {
      try {
        const data = await settingsAPI.getTimeouts();
        setConfirmationTimeout(data.confirmationTokenTimeout);
        setRecoveryTimeout(data.recoveryTokenTimeout);
        setSessionTimeout(data.sessionTokenTimeout);
      } catch (err) {
        toast.error(formatMessage({ id: "settings.load.error", defaultMessage: "Failed to load settings." }));
      }
    };

    fetchTimeouts();
  }, [formatMessage]);

useEffect(() => {
  const fetchPotentialAdmins = async () => {
    try {
      const data = await evaluationAPI.getManagerDropdown(); 
      setPotentialAdmins(data);
    } catch (err) {
      toast.error(formatMessage({ id: "settings.load.managers.error", defaultMessage: "Failed to load potential admins." }));
    }
  };

  fetchPotentialAdmins();
}, [formatMessage]);



  // Individual update functions
  const handleUpdateConfirmation = async () => {
    try {
      await settingsAPI.updateConfirmationTimeout(Number(confirmationTimeout));
      toast.success(formatMessage({ id: "settings.confirmation.success", defaultMessage: "Confirmation timeout updated." }));
    } catch {
      toast.error(formatMessage({ id: "settings.confirmation.error", defaultMessage: "Failed to update confirmation timeout." }));
    }
  };

  const handleUpdateRecovery = async () => {
    try {
      await settingsAPI.updateRecoveryTimeout(Number(recoveryTimeout));
      toast.success(formatMessage({ id: "settings.recovery.success", defaultMessage: "Recovery timeout updated." }));
    } catch {
      toast.error(formatMessage({ id: "settings.recovery.error", defaultMessage: "Failed to update recovery timeout." }));
    }
  };

  const handleUpdateSession = async () => {
    try {
      await settingsAPI.updateSessionTimeout(Number(sessionTimeout));
      toast.success(formatMessage({ id: "settings.session.success", defaultMessage: "Session timeout updated." }));
    } catch {
      toast.error(formatMessage({ id: "settings.session.error", defaultMessage: "Failed to update session timeout." }));
    }
  };

  const handlePromoteToAdmin = async (email) => {
    try {
      await settingsAPI.promoteToAdmin(email);
      toast.success(formatMessage({ id: "settings.promote.success", defaultMessage: "User promoted to admin." }));
    } catch {
      toast.error(formatMessage({ id: "settings.promote.error", defaultMessage: "Failed to promote user." }));
    }
  };

  return (
    <PageLayout
      title={
        <FormattedMessage
          id="settings.title"
          defaultMessage="Definições"
        />
      }
    >
      <div className="max-w-2xl mx-auto mt-8 space-y-6">

        {/* Confirmation Timeout */}
        <div className="flex items-center gap-4">
          <label className="w-1/2 font-medium text-gray-700">
            <FormattedMessage id="settings.confirmation.label" defaultMessage="Confirmation Token Timeout (min)" />
          </label>
          <input
            type="number"
            className="border px-2 py-1 rounded w-24"
            value={confirmationTimeout}
            onChange={(e) => setConfirmationTimeout(e.target.value)}
          />
          <AppButton onClick={handleUpdateConfirmation}>
            <FormattedMessage id="settings.update" defaultMessage="Update" />
          </AppButton>
        </div>

        {/* Recovery Timeout */}
        <div className="flex items-center gap-4">
          <label className="w-1/2 font-medium text-gray-700">
            <FormattedMessage id="settings.recovery.label" defaultMessage="Recovery Token Timeout (min)" />
          </label>
          <input
            type="number"
            className="border px-2 py-1 rounded w-24"
            value={recoveryTimeout}
            onChange={(e) => setRecoveryTimeout(e.target.value)}
          />
          <AppButton onClick={handleUpdateRecovery}>
            <FormattedMessage id="settings.update" defaultMessage="Update" />
          </AppButton>
        </div>

        {/* Session Timeout */}
        <div className="flex items-center gap-4">
          <label className="w-1/2 font-medium text-gray-700">
            <FormattedMessage id="settings.session.label" defaultMessage="Session Timeout (min)" />
          </label>
          <input
            type="number"
            className="border px-2 py-1 rounded w-24"
            value={sessionTimeout}
            onChange={(e) => setSessionTimeout(e.target.value)}
          />
          <AppButton onClick={handleUpdateSession}>
            <FormattedMessage id="settings.update" defaultMessage="Update" />
          </AppButton>
        </div>


        {/* Promote to Admin Section */}
<div className="mt-10 border-t pt-6">
  <h2 className="text-lg font-semibold mb-4">
    <FormattedMessage id="settings.promote.title" defaultMessage="Promote User to Admin" />
  </h2>

  <div className="flex items-center gap-4">
    <select
      className="border px-2 py-1 rounded w-72"
      value={selectedAdminEmail}
      onChange={(e) => setSelectedAdminEmail(e.target.value)}
    >
      <option value="">
        {formatMessage({ id: "settings.promote.select", defaultMessage: "Select a user" })}
      </option>
      {potentialAdmins.map((user) => (
        <option key={user.email} value={user.email}>
          {user.firstName} {user.lastName} ({user.email})
        </option>
      ))}
    </select>

    <AppButton
      onClick={() => handlePromoteToAdmin(selectedAdminEmail)}
      disabled={!selectedAdminEmail}
    >
      <FormattedMessage id="settings.promote.button" defaultMessage="Promote" />
    </AppButton>
  </div>
</div>

      </div>
    </PageLayout>
  );
}

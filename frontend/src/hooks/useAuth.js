import { useNavigate } from "react-router-dom";
import { authAPI } from "../api/authAPI";
import { userStore } from "../stores/userStore";
import { useIntl } from "react-intl";
import { toast } from "react-toastify";
import { fieldLabelKeys } from "../utils/fieldLabels";
import { useNotificationStore } from "../stores/notificationStore";
import { profileAPI } from "../api/profileAPI";

/**
 * Custom hook to manage authentication logic (login, logout).
 * Integrates with Zustand userStore, sessionStorage, and react-toastify for notifications.
 *
 * @returns {Object} An object containing the login and logout functions.
 */
export function useAuth() {
  const navigate = useNavigate();
  const { formatMessage } = useIntl();

  // Access Zustand store methods if needed (extend as required)
  const updateName = userStore((state) => state.updateName);

  // Get methods to update global user state
  const { setUser, setProfileComplete, setMissingFields } = userStore.getState();

  // Fetch notification counters on login
  const fetchCounts = useNotificationStore.getState().fetchCounts;

  /**
   * Logs in a user using provided credentials.
   * After successful login, fetches the full profile (including photograph) and updates the userStore.
   * Shows a toast notification and redirects based on profile completion.
   *
   * @param {Object} credentials - Object containing 'email' and 'password'
   * @returns {Promise<boolean>} True if login was successful, false otherwise
   */
  const login = async (credentials) => {
    try {
      // 1. Call the login API and get response data
      const data = await authAPI.loginUser(credentials);
      console.log("Received login data:", data);

      // 2. Store session token in sessionStorage
      sessionStorage.setItem("authToken", data.sessionToken);

      // 3. Fetch full profile (contains photograph and other personal info)
      const profile = await profileAPI.getProfileById(data.id, data.sessionToken, { forceLogoutOn401: false });

      // 4. Save authenticated user in userStore, including photograph
      setUser({
        id: data.id,
        email: data.email,
        role: data.role,
        firstName: data.firstName,
        lastName: data.lastName,
        photograph: profile.photograph || null, // Can be null if user has no photo
      });

      // 5. Update profile completion status and any missing fields
      setProfileComplete(data.profileComplete);
      setMissingFields(data.missingFields || []);

      // 6. Refresh notification counts
      await fetchCounts();

      console.log("Profile complete?", data.profileComplete);
      console.log("Missing fields:", data.missingFields);

      // 7. Show success toast
      toast.success(
        formatMessage({
          id: "auth.login.success",
          defaultMessage: "Login successful!"
        })
      );

      // 8. Smart redirect based on profile completion
      if (data.profileComplete === false) {
        // Translate missing field labels for info banner (if needed)
        const missingLabels = (data.missingFields || [])
          .map((field) =>
            fieldLabelKeys[field]
              ? formatMessage({ id: fieldLabelKeys[field] })
              : field // fallback if translation missing
          )
          .join(", ");
          console.log("Redirecionar para: ", data.profileComplete === false ? `/profile/${data.id}` : "/dashboard");
        navigate(`/profile/${data.id}`);
        console.log("navigate chamado para /profile/" + data.id);
      } else {
        navigate("/dashboard");
      }

      return true;
    } catch (error) {
      // Special case: unconfirmed account (status 403 + specific backend message)
      if (error.status === 403 && error.message?.includes("not yet been confirmed")) {
        toast.info(formatMessage({
          id: "auth.login.unconfirmed",
          defaultMessage: "Your account has not yet been confirmed! Please check your email."
        }));
      } else {
        toast.error(formatMessage({
          id: "auth.login.failed",
          defaultMessage: "Login failed! Please check your credentials."
        }));
      }
      return false;
    }
  };

  /**
   * Logs out the current user:
   * - Invalidates the session on the backend
   * - Clears notification counts
   * - Clears all user/session data from Zustand and storage
   * - Shows a toast notification and redirects to login page
   *
   * @returns {Promise<void>}
   */
  const clearCounts = useNotificationStore.getState().markAllAsRead;
  const logout = async () => {
    try {
      // 1. Call backend logout endpoint
      await authAPI.logoutUser();
      // 2. Clear notification counts
      await clearCounts();

      // 3. Show success toast and redirect
      toast.success(
        formatMessage({
          id: "auth.logout.success",
          defaultMessage: "Logout successful!"
        }),
        {
          onClose: () => navigate("/login"),
          autoClose: 2000 // Adjust time as needed
        }
      );
    } catch (error) {
      // If session already invalid, continue normally
      if (!(error?.status && [401, 403].includes(error.status))) {
        toast.error(formatMessage({
          id: "auth.logout.failed",
          defaultMessage: "There was a problem logging out. Please try again."
        }));
      }
    } finally {
      // 4. Clear user-related state and all storage
    userStore.getState().clearUser();
    sessionStorage.removeItem("authToken");
    localStorage.removeItem("locale");
    sessionStorage.removeItem("user-store");
    }
  };

  return { login, logout };
}


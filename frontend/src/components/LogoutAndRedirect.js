import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { userStore } from "../stores/userStore";

/**
 * LogoutAndRedirect component.
 *
 * This utility component clears user authentication and session data from
 * sessionStorage and localStorage, then redirects the user to the login page.
 *
 * Typical use case: place this component as the element for a "catch-all" route,
 * or render it as part of an explicit logout flow.
 *
 * @component
 * @example
 * <Route path="*" element={<LogoutAndRedirect />} />
 *
 * @returns {null} This component renders nothing (returns null).
 */
export default function LogoutAndRedirect() {
  const navigate = useNavigate();

  useEffect(() => {
    // Remove authentication and session data
    userStore.getState().clearUser();
    sessionStorage.removeItem("authToken");
    localStorage.removeItem("locale");
    sessionStorage.removeItem("user-store");

    // Redirect to the login page, replacing the current history entry
    navigate("/login", { replace: true });
  }, [navigate]);

  return null;
}
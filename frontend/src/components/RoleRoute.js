/**
 * RoleRoute
 * Route guard component that restricts access to routes based on user roles.
 * Shows a professional denied-access card, then a spinner with the app logo,
 * and finally redirects to the dashboard after a set duration.
 *
 * Usage:
 * <RoleRoute allowedRoles={["admin"]}>
 *   <CoursesPage />
 * </RoleRoute>
 */

import { useEffect, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { userStore } from "../stores/userStore";
import Spinner from "../components/Spinner"; // O novo componente corporativo!
import logo from "../images/logo_red.png";
import { FormattedMessage } from "react-intl";

// Timing constants
const REDIRECT_DURATION = 6000;
const SPINNER_DURATION = 1300;
const FADE_DURATION = 600;

export default function RoleRoute({ allowedRoles = [], children }) {
  const user = userStore((state) => state.user);
  const location = useLocation();

  const [redirect, setRedirect] = useState(false);
  const [fadeOut, setFadeOut] = useState(false);
  const [progress, setProgress] = useState(false);

  useEffect(() => {
    if (user && user.id && !allowedRoles.includes(user.role)) {
      setProgress(true);

      const fadeTimer = setTimeout(() => setFadeOut(true), REDIRECT_DURATION - FADE_DURATION);
      const timer = setTimeout(() => setRedirect(true), REDIRECT_DURATION + SPINNER_DURATION);

      return () => {
        clearTimeout(timer);
        clearTimeout(fadeTimer);
      };
    }
  }, [user, allowedRoles]);

  // 1. If not authenticated, redirect to login page
  if (!user || !user.id) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // 2. If role is not allowed, show denied card > spinner > redirect
  if (!allowedRoles.includes(user.role)) {
    if (redirect) {
      return <Navigate to="/dashboard" replace />;
    }
    if (fadeOut) {
      return (
        <Spinner
          messageId="accessDenied.redirecting"
          messageDefault="Redirecting..."
          minHeight="100vh"
          spinDuration="2.2s"
          showLogo={true}
        />
      );
    }
    // Denied card with progress bar
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
        <div
          className={
            "bg-white border border-red-200 rounded-xl shadow-2xl px-10 py-10 flex flex-col items-center w-full max-w-md transition-opacity duration-700 " +
            (fadeOut ? "opacity-0" : "opacity-100")
          }
        >
          <img src={logo} alt="Logo" className="w-24 h-24 mb-3" />
          <span className="text-[64px] mb-10">🚫</span>
          <h2 className="text-2xl font-bold text-red-700 mb-2">
            <FormattedMessage id="accessDenied.title" defaultMessage="Access restricted" />
          </h2>
          <p className="text-gray-700 text-center mb-2">
            <FormattedMessage
              id="accessDenied.message"
              defaultMessage={
                "You do not have permission to access this page.\nYou will be redirected to the dashboard."
              }
            />
          </p>
          <div className="w-full h-2 bg-red-100 rounded-full mt-4 mb-2 overflow-hidden">
            <div
              className="h-2 bg-red-500 rounded-full"
              style={{
                width: progress ? "100%" : "0%",
                transition: `width ${REDIRECT_DURATION}ms linear`
              }}
            ></div>
          </div>
        </div>
      </div>
    );
  }

  // 3. If role is allowed, render the children (protected route)
  return children;
}





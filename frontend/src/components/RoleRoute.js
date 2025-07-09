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
import logo from "../images/logo_red.png";
import { FormattedMessage } from "react-intl";

// How long to show the denied card and progress bar (ms)
const REDIRECT_DURATION = 6000; // e.g., 6s for user to read the card
// How long to show the spinner/logo after card fades out (ms)
const SPINNER_DURATION = 1300;  // e.g., 1.3s for a smooth transition
// How long the fade-out of the card lasts (ms)
const FADE_DURATION = 600;      // e.g., 0.6s

export default function RoleRoute({ allowedRoles = [], children }) {
  // Get current user from the store
  const user = userStore((state) => state.user);
  const location = useLocation();

  // Local state for UI flow
  const [redirect, setRedirect] = useState(false); // when to actually redirect
  const [fadeOut, setFadeOut] = useState(false);   // when to fade out card and show spinner
  const [progress, setProgress] = useState(false); // when to animate progress bar

  useEffect(() => {
    // Only trigger denied card logic if user is authenticated and NOT in allowedRoles
    if (user && user.id && !allowedRoles.includes(user.role)) {
      setProgress(true); // start animating progress bar

      // Fade out card a bit before full duration (so spinner shows before redirect)
      const fadeTimer = setTimeout(() => setFadeOut(true), REDIRECT_DURATION - FADE_DURATION);

      // After full duration + spinner time, perform redirect to dashboard
      const timer = setTimeout(() => setRedirect(true), REDIRECT_DURATION + SPINNER_DURATION);

      // Clean up timers on unmount or if dependencies change
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
    // When time's up, redirect to dashboard
    if (redirect) {
      return <Navigate to="/dashboard" replace />;
    }
    // When fading out, show only spinner/logo (no card)
    if (fadeOut) {
      return (
        <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
          <div className="flex flex-col items-center">
            <img
              src={logo}
              alt="Logo"
              className="w-16 h-16 mb-4 animate-spin"
              style={{ animationDuration: "2.2s" }} // slower rotation
            />
            <span className="text-gray-700 text-lg font-semibold mt-2">
              <FormattedMessage id="accessDenied.redirecting" defaultMessage="Redirecting..." />
            </span>
          </div>
        </div>
      );
    }
    // Otherwise, show denied card with fade in/out and progress bar
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
          {/* Animated progress bar */}
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




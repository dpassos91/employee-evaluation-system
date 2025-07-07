/**
 * PrivateRoute ensures that:
 * - Only authenticated users can access protected routes.
 * - Users with incomplete profiles are forced to complete their profile before accessing other areas of the app.
 * - Users are never logged out or redirected to login solely due to an incomplete profile (unless not authenticated).
 */

import { Navigate, useLocation } from "react-router-dom";
import { userStore } from "../stores/userStore";

/**
 * A wrapper component that protects private routes by:
 * - Redirecting to the login page if the user is not authenticated
 * - Redirecting to the profile page if the profile is incomplete
 * - Allowing access to the requested route otherwise
 *
 * @param {Object} props
 * @param {React.ReactNode} props.children - The component(s) to render if access is allowed
 * @returns {React.ReactNode} Either the children or a <Navigate> component for redirection
 */
export function PrivateRoute({ children }) {
  // Get authentication and profile state from user store
  const { user, profileComplete } = userStore();
  const location = useLocation();

  // 1. If there is no authenticated user, redirect to the login page
  if (!user || !user.id) {
    return <Navigate to="/login" replace />;
  }

  // 2. If profile completion status is unknown (e.g., still loading), show a loader
  if (profileComplete === null) {
    // You can replace this with a spinner or more sophisticated loader if desired
    return <div>Loading...</div>;
  }

  // 3. If the profile is incomplete and the current route is not /profile,
  //    redirect to the profile page to force completion before accessing other routes
  const isProfileRoute = location.pathname === "/profile";
  if (profileComplete === false && !isProfileRoute) {
    return <Navigate to="/profile" replace />;
  }

  // 4. Otherwise, allow access to the requested route/component
  return children;
}


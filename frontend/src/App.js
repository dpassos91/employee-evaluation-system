/**
 * Main App component.
 * Sets up routing, internationalization, and provides a global WebSocket connection
 * for handling live notifications and chat events throughout the application.
 * 
 * The WebSocket connection is established once (when user is authenticated)
 * and dispatches all received events to the appropriate stores (e.g., notificationStore, chatStore).
 */

import { useEffect, useCallback } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { IntlProvider } from "react-intl";

import PrivateRoute from "./components/PrivateRoute";
import RoleRoute from "./components/RoleRoute";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

import useWebSocket from "./hooks/useWebSocket";
import { setWebSocketRef } from "./stores/chatStore"; // Import the setter for the global WebSocket reference
import { useNotificationStore } from "./stores/notificationStore";
import { userStore } from "./stores/userStore";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import RedefinePasswordPage from "./pages/RedefinePasswordPage";
import DashboardPage from "./pages/DashboardPage";
import ProfilePage from "./pages/ProfilePage";
import UsersListPage from "./pages/UsersListPage";
import ChatPage from "./pages/ChatPage";
import EvaluationListPage from "./pages/EvaluationListPage"; 
import EvaluationFormPage from "./pages/EvaluationFormPage";
import EvaluationHistoryPage from "./pages/EvaluationHistoryPage";
import NewEvaluationCyclePage from "./pages/NewEvaluationCyclePage";
import UsersWithoutManagerPage from "./pages/UsersWithoutManagerPage";
import EvaluationIntermediaryPage from "./pages/EvaluationIntermediaryPage";
import SettingsPage from "./pages/SettingsPage";
import CoursesPage from "./pages/CoursesPage";
import LogoutAndRedirect from "./components/LogoutAndRedirect";
import CoursesHistoryPage from "./pages/CoursesHistoryPage";
import TeamCoursesPage from "./pages/TeamCoursesPage";
import ConfirmAccountPage from "./pages/ConfirmAccountPage";

/**
 * Main App component.
 * Sets up routing, internationalization, and provides a global WebSocket connection
 * for handling live notifications and chat events throughout the application.
 */
export default function App() {
  // Get user and locale info from userStore
  const { user, locale, translations } = userStore();
  const incrementNotificationCount = useNotificationStore((s) => s.incrementCount);

  // WebSocket endpoint for your backend
  const WS_ENDPOINT = "wss://localhost:8443/grupo7/websocket/chat";

  /**
   * Global handler for all WebSocket messages.
   * Dispatches notification and chat events to their respective stores.
   * @param {Object} data - The message payload from the WebSocket server.
   */
const handleWebSocketMessage = useCallback((data) => {
  // System notifications (ALERT, SYSTEM, WARNING)
  if (data?.type && ["ALERT", "SYSTEM", "WARNING"].includes(data.type)) {
    incrementNotificationCount(data.type);
  }

  // Status update: update contact online/offline
  if (data.type === "status_update") {
    import("./stores/chatStore").then(({ useChatStore }) => {
      useChatStore.getState().updateContactStatus(data.userId, data.online);
    });
  }

  // Chat message
  if (data.type === "chat_message") {
    import("./stores/chatStore").then(({ useChatStore }) => {
      useChatStore.getState().addMessage(data);
    });

    // Increment badge only if not in the correct chat!
    import("./stores/chatStore").then(({ useChatStore }) => {
      const activeConversationId = useChatStore.getState().activeConversationId;

      if (
        !window.location.pathname.startsWith("/chat") ||
        activeConversationId !== data.senderId // or data.receiverId
      ) {
        import("./stores/notificationStore").then(({ useNotificationStore }) => {
          useNotificationStore.getState().incrementCount("MESSAGE");
        });
      }
    });
  }
}, [incrementNotificationCount]);



  /**
   * Establishes the global WebSocket connection once user is authenticated.
   * Makes the WebSocket instance available globally for sending chat messages via the chat store.
   */
  const { isConnected, sendMessage } = useWebSocket(
    WS_ENDPOINT,
    typeof window !== "undefined" ? sessionStorage.getItem("authToken") : null,
    user ? handleWebSocketMessage : null
  );

  // Save the sendMessage function in the chatStore for use by ChatPage and other components
  useEffect(() => {
    // setWebSocketRef will actually expect a WebSocket instance,
    // but since your hook only exposes sendMessage, you can store sendMessage instead.
    // If you want to send the actual instance, you can adapt the hook to expose it.
    setWebSocketRef({ sendMessage });
  }, [sendMessage]);

  return (
    <IntlProvider
      locale={locale}
      messages={translations}
      onError={(err) => {
        if (err.code === "MISSING_TRANSLATION") {
          // You can log or report missing translations here
          console.warn("Translation missing:", err.message);
        }
      }}
    >
      <Router>
        {/* Application routes */}
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgotpassword" element={<ForgotPasswordPage />} />
          <Route path="/redefinepassword" element={<RedefinePasswordPage />} />
          <Route path="/confirmAccount" element={<ConfirmAccountPage />} />
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <DashboardPage />
              </PrivateRoute>
            }
          />
          <Route
            path="/profile/:userId"
            element={
              <PrivateRoute>
                <ProfilePage />
              </PrivateRoute>
            }
          />
          <Route
            path="/userslist"
            element={
              <PrivateRoute>
                <UsersListPage />
              </PrivateRoute>
            }
          />
          <Route
            path="/chat"
            element={
              <PrivateRoute>
                <ChatPage />
              </PrivateRoute>
            }
          />
<Route
            path="/evaluationlist"
            element={
              <RoleRoute allowedRoles={["ADMIN", "MANAGER"]}>
                <EvaluationListPage />
              </RoleRoute>
            }
          />
<Route
  path="/evaluationform/:userId"
  element={
    
    <RoleRoute allowedRoles={["ADMIN", "MANAGER"]}>
      <EvaluationFormPage />
    </RoleRoute>
  }
/>
<Route
  path="/profile/:userId/evaluationhistory"
  element={
    <PrivateRoute>
      <EvaluationHistoryPage />
    </PrivateRoute>
  }
/>
<Route
  path="/newevaluationcycle"
  element={
    <RoleRoute allowedRoles={["ADMIN"]}>
      <NewEvaluationCyclePage />
    </RoleRoute>
  }
/>
<Route
  path="/newevaluationcycle/userswithoutmanager"
  element={
    <RoleRoute allowedRoles={["ADMIN"]}>
      <UsersWithoutManagerPage />
    </RoleRoute>
  }
/>
<Route
  path="/evaluations"
  element={
   <RoleRoute allowedRoles={["ADMIN"]}>
      <EvaluationIntermediaryPage />
    </RoleRoute>
  }
/>
<Route
            path="/courses"
            element={
              <RoleRoute allowedRoles={["ADMIN"]}>
                <CoursesPage />
              </RoleRoute>
            }
          />
<Route
  path="/profile/:userId/courseshistory"
  element={
    <PrivateRoute>
      <CoursesHistoryPage />
    </PrivateRoute>
  }
/>
<Route
  path="/settings"
  element={
    <RoleRoute allowedRoles={["ADMIN"]}>
      <SettingsPage />
    </RoleRoute>
  }
/>
<Route
  path="/teamcourses"
  element={
    <RoleRoute allowedRoles={["MANAGER", "ADMIN"]}>
      <TeamCoursesPage />
    </RoleRoute>
  }
/>
          {/* Redirect all unknown routes to login */}
          <Route path="*" element={<LogoutAndRedirect />} />
        </Routes>
        <ToastContainer position="top-center" />
      </Router>
    </IntlProvider>
  );
}

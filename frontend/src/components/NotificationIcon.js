import notificationIcon from "../images/notification_icon.png";
import { useNotificationStore } from "../stores/notificationStore";
import { useState } from "react";
import { notificationAPI } from "../api/notificationAPI";

/**
 * NotificationIcon component.
 * Renders the notification icon with a badge for unread ALERT, SYSTEM, and WARNING notifications.
 * On click, marks all notifications as read, fetches updated badge counts,
 * fetches updated notifications, e toggles o dropdown para mostrar detalhes.
 */
export default function NotificationIcon() {
  const counts = useNotificationStore((s) => s.counts);
  const fetchCounts = useNotificationStore((s) => s.fetchCounts);
  const notifications = useNotificationStore((s) => s.notifications);
  const fetchNonMessageNotifications = useNotificationStore((s) => s.fetchNonMessageNotifications);

  // Sum non-message notification types for the badge
  const notifCount = (counts.ALERT || 0) + (counts.SYSTEM || 0) + (counts.WARNING || 0);

  const [dropdownOpen, setDropdownOpen] = useState(false);

  /**
   * Handles click on the notification icon (dropdown).
   * - When opening the dropdown, marks all notifications as read in the backend,
   *   updates the badge counts, and fetches the latest unread non-MESSAGE notifications for the dropdown.
   * - Ensures that the badge and dropdown stay in sync with the backend state.
   *
   * @async
   * @function handleClick
   */
  const handleClick = async () => {
    if (!dropdownOpen) {
      await fetchNonMessageNotifications();
      await notificationAPI.markAllAsRead();
      await fetchCounts();
      
    }
    setDropdownOpen((open) => !open);
  };

  return (
    <div className="relative">
      <button aria-label="Notifications" className="relative focus:outline-none" onClick={handleClick}>
        <img
          src={notificationIcon}
          alt="Notifications"
          className="w-12 h-12 md:w-14 md:h-14"
        />
        {notifCount > 0 && (
          <span className="
            absolute top-1 right-1
            bg-red-600 text-white text-xs
            rounded-full w-5 h-5 flex items-center justify-center
            font-bold z-10
          ">
            {notifCount}
          </span>
        )}
      </button>
      {/* Dropdown */}
      {dropdownOpen && (
        <div className="absolute right-0 mt-2 w-72 bg-white border rounded-lg shadow-lg z-50 p-2">
          <div className="font-semibold mb-2">Notificações</div>
          {notifications.length === 0 && (
            <div className="text-gray-400 text-sm">Sem notificações por ler.</div>
          )}
          <ul>
            {notifications.map((notif) => (
              <li key={notif.id} className="py-2 border-b last:border-b-0">
                <div className="text-sm font-medium">{notif.message}</div>
                <div className="text-xs text-gray-400">{notif.createdAt}</div>
                <div className="text-xs italic text-blue-700">{notif.type}</div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}




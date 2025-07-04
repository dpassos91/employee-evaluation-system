import messageIcon from "../images/message_icon.png";
import { useNavigate } from "react-router-dom";
import { useNotificationStore } from "../stores/notificationStore";
import { messageAPI } from "../api/messageAPI";
import { notificationAPI } from "../api/notificationAPI";

/**
 * MessageIcon component.
 * Renders the message icon with a badge for unread MESSAGE notifications.
 * On click, marks all chat messages and message notifications as read,
 * fetches the updated badge counts from backend, and navigates to the chat page.
 */
export default function MessageIcon() {
  const navigate = useNavigate();
  const counts = useNotificationStore((s) => s.counts);
  const fetchCounts = useNotificationStore((s) => s.fetchCounts);

  /**
   * Handles click on the message icon:
   * - Marks all chat messages as read (messageAPI)
   * - Marks all MESSAGE notifications as read (notificationAPI)
   * - Fetches updated notification counts
   * - Navigates to /chat
   */
  const handleClick = async () => {
    try {
      await messageAPI.markMessagesAsRead();
      await notificationAPI.markAllMessageNotificationsAsRead();
      await fetchCounts();
    } catch (e) {
      // (Optional) Handle errors, e.g., show a toast
      console.error("Error marking messages as read:", e);
    } finally {
      navigate("/chat");
    }
  };

  const messageCount = counts.MESSAGE || 0;

  return (
    <button type="button" onClick={handleClick} className="relative focus:outline-none">
      <img
        src={messageIcon}
        alt="Mensagens"
        className="w-12 h-12 md:w-14 md:h-14"
      />
      {messageCount > 0 && (
        <span className="
          absolute top-1 right-1
          bg-red-600 text-white text-xs
          rounded-full w-5 h-5 flex items-center justify-center
          font-bold z-10
        ">
          {messageCount}
        </span>
      )}
    </button>
  );
}


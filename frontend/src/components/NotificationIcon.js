import notificationIcon from "../images/notification_icon.png"; // substitui pela tua imagem real

export default function NotificationIcon() {
  return (
    <button
      aria-label="Notifications"
      className="focus:outline-none"
    >
      <img
        src={notificationIcon}
        alt="Notifications"
        className="w-12 h-12 md:w-14 md:h-14"
      />
    </button>
  );
}

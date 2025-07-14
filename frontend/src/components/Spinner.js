import { FormattedMessage } from "react-intl";
import logo from "../images/logo_red.png";

/**
 * Corporate Spinner for loading or redirect states.
 * Props:
 * - messageId: string ID for FormattedMessage (optional)
 * - messageDefault: default loading message (optional)
 * - minHeight: CSS value for minHeight (default: '50vh')
 * - spinDuration: string for animation duration (default: '2.2s')
 */
export default function Spinner({
  messageId = "app.loading",
  messageDefault = "A carregar…",
  minHeight = "50vh",
  spinDuration = "2.2s",
  showLogo = true,
}) {
  return (
    <div
      className="bg-gray-50 flex flex-col items-center justify-center w-full"
      style={{ minHeight }}
      role="status"
      aria-live="polite"
    >
      <div className="flex flex-col items-center">
        {showLogo && (
          <img
            src={logo}
            alt="Logo"
            className="w-16 h-16 mb-4 animate-spin"
            style={{ animationDuration: spinDuration }}
          />
        )}
        <span className="text-gray-700 text-lg font-semibold mt-2">
          <FormattedMessage id={messageId} defaultMessage={messageDefault} />
        </span>
      </div>
    </div>
  );
}

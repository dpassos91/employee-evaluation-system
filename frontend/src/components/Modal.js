import React from "react";
import ReactDOM from "react-dom";
import AppButton from "./AppButton"; // Ajusta o path se necessário

/**
 * Reusable Modal component with consistent button styling using AppButton.
 *
 * Props:
 * - isOpen: boolean (controls visibility)
 * - onClose: function (called when overlay or close button is clicked)
 * - title: string (modal title/header)
 * - children: modal content (form, message, etc)
 * - actions: optional array of objects:
 *      { label: string | JSX, onClick?: function, type?: string, variant?: 'primary'|'secondary'|'sidebar', className?: string }
 *
 * Usage:
 * <Modal
 *   isOpen={isOpen}
 *   onClose={closeFn}
 *   title="My Modal"
 *   actions={[
 *     { label: "Cancel", variant: "secondary", onClick: closeFn },
 *     { label: "Save", variant: "primary", type: "submit", onClick: saveFn }
 *   ]}
 * >
 *   ...content...
 * </Modal>
 */
export default function Modal({ isOpen, onClose, title, children, actions }) {
  if (!isOpen) return null;

  return ReactDOM.createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      {/* Modal content */}
      <div className="bg-white rounded-xl shadow-lg w-full max-w-md mx-2 relative">
        {/* Close button (X) */}
        <button
          onClick={onClose}
          className="absolute top-2 right-2 text-gray-400 hover:text-gray-700 text-2xl font-bold px-2"
          aria-label="Close modal"
          tabIndex={0}
        >
          &times;
        </button>
        {/* Header */}
        {title && (
          <div className="px-6 pt-6 pb-2">
            <h2 className="text-xl font-bold text-gray-800">{title}</h2>
          </div>
        )}
        {/* Content */}
        <div className="px-6 py-4">{children}</div>
        {/* Actions (footer) */}
        {actions && Array.isArray(actions) && (
          <div className="px-6 pb-6 pt-2 flex gap-2 justify-end">
            {actions.map((action, idx) =>
              typeof action === "object" && action.label ? (
                <AppButton
                  key={idx}
                  variant={action.variant || "primary"}
                  type={action.type || "button"}
                  onClick={action.onClick}
                  className={action.className || ""}
                  {...(action.props || {})}
                >
                  {action.label}
                </AppButton>
              ) : (
                // Allow raw JSX for custom fallback
                <span key={idx}>{action}</span>
              )
            )}
          </div>
        )}
      </div>
    </div>,
    document.body
  );
}


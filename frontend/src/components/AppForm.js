/**
 * Reusable application form component.
 * Provides consistent layout, spacing, error/success display, and action area for buttons.
 *
 * Props:
 * - onSubmit: function (called when form is submitted)
 * - children: React nodes (form fields/inputs)
 * - actions: array of objects for action buttons
 *      { label: string | JSX, onClick?: function, type?: string, variant?: 'primary'|'secondary', loading?: boolean, className?: string }
 * - isLoading: boolean (disables submit button and prevents double submit)
 * - error: string | JSX (global error message)
 * - success: string | JSX (global success message)
 * - className: string (additional classes for the form)
 */
export default function AppForm({
  onSubmit,
  children,
  actions = [],
  isLoading = false,
  error,
  success,
  className = ""
}) {
  // Prevent double submit if isLoading is true
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!isLoading && typeof onSubmit === "function") {
      onSubmit(e);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className={`flex flex-col gap-2 ${className}`}
      autoComplete="off"
    >
      {/* Form fields (children) */}
      {children}

      {/* Global error message */}
      {error && (
        <div className="text-sm text-red-600 mt-1">{error}</div>
      )}

      {/* Global success message */}
      {success && (
        <div className="text-sm text-green-600 mt-1">{success}</div>
      )}

      {/* Actions area (footer), aligned right by default */}
      {actions && actions.length > 0 && (
        <div className="flex gap-2 justify-end mt-4">
          {actions.map((action, idx) => {
            // If an object, render as AppButton; if JSX, render directly
            if (typeof action === "object" && action.label) {
              // Use your AppButton for consistent styling
              // Adjust import path as needed
              const AppButton = require("./AppButton").default;
              return (
                <AppButton
                  key={idx}
                  variant={action.variant || "primary"}
                  type={action.type || "button"}
                  onClick={action.onClick}
                  className={action.className || ""}
                  disabled={action.loading || isLoading || action.disabled}
                  {...(action.props || {})}
                >
                  {action.loading ? (
                    // You can swap this for your spinner
                    <span className="animate-pulse">...</span>
                  ) : (
                    action.label
                  )}
                </AppButton>
              );
            }
            // Fallback for custom JSX
            return <span key={idx}>{action}</span>;
          })}
        </div>
      )}
    </form>
  );
}

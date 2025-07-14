/**
 * AppTableFilters Component
 *
 * Generic filter bar for tables, supporting dynamic input/select fields and custom JSX filters.
 *
 * @param {Object[]} filters - List of filter definitions. Each object may have:
 *   - type: "input" | "select" | "custom"
 *   - value: string (for input/select)
 *   - onChange: function (for input/select)
 *   - placeholder: string (for input)
 *   - options: array (for select) [{ value, label }]
 *   - render: function () => ReactNode (for custom)
 *   - inputProps / selectProps: any extra props to pass (optional)
 * @param {ReactNode} actions - Optional JSX elements for action buttons (e.g. export, reset).
 * @returns {ReactNode} Filter bar with configured filters and actions.
 */


export function AppTableFilters({ filters = [], actions = null }) {
  return (
    <div className="flex gap-4 mb-4 flex-wrap items-end">
      {filters.map((f, idx) => {
        // Input field
        if (f.type === "input") {
          return (
            <input
              key={idx}
              className="border px-2 py-1 rounded"
              value={f.value}
              onChange={f.onChange}
              placeholder={f.placeholder}
              {...(f.inputProps || {})}
            />
          );
        }
        // Select field
        if (f.type === "select") {
          return (
            <select
              key={idx}
              className="border px-2 py-1 rounded"
              value={f.value}
              onChange={f.onChange}
              {...(f.selectProps || {})}
            >
              {(f.options || []).map((opt) =>
                typeof opt === "string" ? (
                  <option key={opt} value={opt}>{opt}</option>
                ) : (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                )
              )}
            </select>
          );
        }
        // Custom filter element (JSX)
        if (f.type === "custom" && typeof f.render === "function") {
          return (
            <div key={idx}>
              {f.render()}
            </div>
          );
        }
        return null;
      })}
      {actions && <div className="flex gap-2">{actions}</div>}
    </div>
  );
}
/**
 * AppTable Component
 * 
 * Generic, reusable table component for displaying lists of data.
 * Supports custom columns, internationalized headers, loading state, and custom cell rendering.
 * 
 * @param {Object[]} columns - Array of column definitions. Each should have:
 *   - header: ReactNode (usually <FormattedMessage />)
 *   - accessor: string | function (object property name or function to extract the value)
 *   - className: string (optional, for custom width or style)
 *   - render: function (optional, receives (row, index) and returns ReactNode)
 * @param {Object[]} data - Array of data objects to render in the table.
 * @param {boolean} loading - Whether to show loading state.
 * @param {string|ReactNode} emptyMessage - Message to show if there's no data.
 */

export function AppTable({ columns, data, loading, emptyMessage }) {
  return (
    <div className="overflow-x-auto w-full">
      {/* Render loading state */}
      {loading ? (
        <div className="text-center py-6 text-gray-500">Loading...</div>
      ) : data.length === 0 ? (
        // Render empty message if no data
        <div className="text-center py-6 text-gray-500">{emptyMessage}</div>
      ) : (
        <table className="min-w-full text-left border-collapse table-auto">
          <thead>
            <tr className="bg-gray-200 text-sm">
              {columns.map((col, idx) => (
                <th
                  key={idx}
                  className={`p-2 ${col.className ?? ""}`}
                  style={col.width ? { width: col.width } : undefined}
                >
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((row, rowIdx) => (
              <tr key={row.id || rowIdx} className="border-b hover:bg-gray-50">
                {columns.map((col, colIdx) => (
                  <td
                    key={colIdx}
                    className={`p-2 ${col.tdClassName ?? ""}`}
                    colSpan={col.colSpan}
                  >
                    {/* Custom render function, fallback to accessor */}
                    {col.render
                      ? col.render(row, rowIdx)
                      : typeof col.accessor === "function"
                      ? col.accessor(row, rowIdx)
                      : row[col.accessor]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

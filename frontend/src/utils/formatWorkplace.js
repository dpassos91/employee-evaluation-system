/**
 * Formats a workplace string by capitalizing the first letter and setting the rest to lowercase.
 * Useful for displaying workplace enums (e.g., "LISBOA" → "Lisboa").
 *
 * @param {string} workplace - The workplace string to format (e.g., enum value).
 * @returns {string} The formatted workplace name, or an empty string if input is empty.
 */
export function formatWorkplace(workplace) {
  if (!workplace) return "";
  return workplace.charAt(0).toUpperCase() + workplace.slice(1).toLowerCase();
}

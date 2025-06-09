import { FormattedMessage } from "react-intl";

export default function InputField({ id, defaultLabel, type = "text", name, value, onChange }) {
  return (
    <div>
      <label htmlFor={name} className="block mb-1">
        <FormattedMessage id={id} defaultMessage={defaultLabel} />
      </label>
      <input
        id={name}
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
      />
    </div>
  );
}
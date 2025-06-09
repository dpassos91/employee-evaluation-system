import { FormattedMessage } from "react-intl";

export default function SubmitButton({ id, defaultLabel }) {
  return (
    <button
      type="submit"
      className="w-full bg-primary text-white font-bold py-2 rounded hover:bg-red-700 transition"
    >
      <FormattedMessage id={id} defaultMessage={defaultLabel} />
    </button>
  );
}
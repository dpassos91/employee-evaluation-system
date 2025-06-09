import { FormattedMessage } from "react-intl";
import logo from "../images/logo_white.png";

export default function AuthFormLayout({ titleId, defaultTitle, children, extraLinks }) {
  return (
    <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
      <div className="block md:hidden mb-6">
        <img src={logo} alt="SkillPath logo" className="w-28 mx-auto mb-1" />
        <p className="text-lg italic font-hand leading-none text-primary">
          <FormattedMessage id="slogan" defaultMessage="Learn. Evolve. Lead." />
        </p>
      </div>

      <h2 className="text-2xl font-bold mb-6">
        <FormattedMessage id={titleId} defaultMessage={defaultTitle} />
      </h2>

      {children}

      {extraLinks && (
        <div className="flex justify-between text-sm mt-4">
          {extraLinks}
        </div>
      )}
    </div>
  );
}
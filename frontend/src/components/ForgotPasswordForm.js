import { FormattedMessage } from "react-intl";
import logo from "../images/logo_white.png";
import { Link } from "react-router-dom";


export default function ForgotPasswordForm() {
  return (
    <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
      
      {/* Branding visível apenas em mobile */}
      <div className="block md:hidden mb-6">
        <img src={logo} alt="SkillPath logo" className="w-28 mx-auto mb-1" />
        <p className="text-lg italic font-hand leading-none text-primary">
          <FormattedMessage id="slogan" defaultMessage="Learn. Evolve. Lead." />
        </p>
      </div>

      <h2 className="text-2xl font-bold mb-6">
        <FormattedMessage id="forgotpassword.title" defaultMessage="Esqueci-me da password" />
      </h2>

      <form className="space-y-4 text-left">
        <div>
          <label className="block mb-1">
            <FormattedMessage id="email" defaultMessage="Email" />
          </label>
          <input
            type="email"
            className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-primary text-white font-bold py-2 rounded hover:bg-red-700 transition"
        >
          <FormattedMessage id="forgotpassword.form.submit" defaultMessage="Recuperar" />
        </button>
      </form>

      <div className="flex justify-between text-sm mt-4">
        <Link to="/login" className="text-primary hover:underline">
          <FormattedMessage id="goback" defaultMessage="Voltar" />
        </Link>
      </div>
    </div>
  );
}
import { FormattedMessage } from "react-intl";
import logo from "../images/logo_white.png";
import { Link } from "react-router-dom";


export default function RegisterForm() {
  return (
    <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
      
      {/* Branding visível apenas em mobile */}
      <div className="block md:hidden mb-6">
        <img src={logo} alt="SkillPath logo" className="w-28 mx-auto mb-1" />
        <p className="text-lg italic font-hand leading-none text-primary">
          <FormattedMessage id="register.slogan" defaultMessage="Learn. Evolve. Lead." />
        </p>
      </div>

      <h2 className="text-2xl font-bold mb-6">
        <FormattedMessage id="register.title" defaultMessage="Registar" />
      </h2>

      <form className="space-y-4 text-left">
        <div>
          <label className="block mb-1">
            <FormattedMessage id="register.form.email.label" defaultMessage="Email" />
          </label>
          <input
            type="email"
            className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div>
          <label className="block mb-1">
            <FormattedMessage id="register.form.password.label" defaultMessage="Password" />
          </label>
          <input
            type="password"
            className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div>
          <label className="block mb-1">
            <FormattedMessage id="register.form.passwordconfirmation.label" defaultMessage="Confirme a password" />
          </label>
          <input
            type="password"
            className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-primary text-white font-bold py-2 rounded hover:bg-red-700 transition"
        >
          <FormattedMessage id="register.form.submit" defaultMessage="Registar" />
        </button>
      </form>

      <div className="flex justify-between text-sm mt-4">
        <Link to="/login" className="text-primary hover:underline">
          <FormattedMessage id="register.form.goback" defaultMessage="Voltar" />
        </Link>
      </div>
    </div>
  );
}
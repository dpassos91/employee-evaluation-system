import logo from "../images/logo_white.png";

export default function LoginForm() {
  return (
    <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
      
      {/* Branding visível apenas em mobile */}
      <div className="block md:hidden mb-6">
        <img src={logo} alt="SkillPath logo" className="w-28 mx-auto mb-1" />
        <p className="text-lg italic font-hand leading-none text-primary">
          Learn. Evolve. Lead.
        </p>
      </div>

      <h2 className="text-2xl font-bold mb-6">Login</h2>

      <form className="space-y-4 text-left">
        <div>
          <label className="block mb-1">Email</label>
          <input
            type="email"
            className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div>
          <label className="block mb-1">Password</label>
          <input
            type="password"
            className="w-full border border-primary px-4 py-2 rounded focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-primary text-white font-bold py-2 rounded hover:bg-red-700 transition"
        >
          Login
        </button>
      </form>

      <div className="flex justify-between text-sm mt-4">
        <a href="#" className="text-primary hover:underline">Não tem conta?</a>
        <a href="#" className="text-primary hover:underline">Esqueceu-se da password?</a>
      </div>
    </div>
  );
}


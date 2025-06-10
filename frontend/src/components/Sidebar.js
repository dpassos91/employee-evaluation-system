import { useNavigate } from "react-router-dom";
import { FormattedMessage } from "react-intl";
import logo from "../images/logo_white_wordless.png"; 
import profileIcon from "../images/profile_icon.png"; 
import dashboardIcon from "../images/dashboard_icon.png";
import evaluationsIcon from "../images/evaluations_icon.png";
import miniprofileIcon from "../images/miniprofile_icon.png";
import usersIcon from "../images/users_icon.png";
import { FaSignOutAlt } from "react-icons/fa";

export default function Sidebar() {
  const navigate = useNavigate();

  const menuItems = [
    { id: "sidebar.dashboard", icon: dashboardIcon, path: "/dashboard" },
    { id: "sidebar.evaluations", icon: evaluationsIcon, path: "/evaluations" },
    { id: "sidebar.profile", icon: miniprofileIcon, path: "/profile" },
    { id: "sidebar.users", icon: usersIcon, path: "/users" },
  ];

  return (
    <aside className="fixed top-0 left-0 h-full w-64 bg-[#D41C1C] text-white flex flex-col justify-between shadow-lg z-50">
      {/* Top Section */}
      <div>
        <div className="flex flex-col items-center py-6">
          <img src={profileIcon} alt="Utilizador" className="w-20 h-20 rounded-full bg-white" />
          <span className="mt-2 text-sm font-light">
             defaultMessage="Nome Apelido"
          </span>
        </div>

        <div className="px-4">
          {menuItems.map((item) => (
            <button
              key={item.id}
              onClick={() => navigate(item.path)}
              className="w-full flex items-center gap-3 px-4 py-2 rounded text-sm hover:bg-white/10 transition-all mb-1"
            >
              <img src={item.icon} alt={item.id} className="w-8 h-8" />
              <FormattedMessage id={item.id} />
            </button>
          ))}
        </div>
      </div>

      {/* Bottom Section */}
      <div className="px-4 pb-6">
        <button
          onClick={() => console.log("logout")} // substitui por lógica real
          className="w-full flex items-center gap-4 px-6 py-2 rounded text-sm hover:bg-white/10 transition-all mb-1"
        >
          <FaSignOutAlt className="w-5 h-5" />
          <FormattedMessage id="sidebar.logout" defaultMessage="Sair" />
        </button>

        <div className="flex justify-center mt-4">
          <img src={logo} alt="Logo" className="w-24 h-24" />
        </div>
      </div>
    </aside>
  );
}

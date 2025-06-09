import { useState, useEffect, useRef } from "react";
import language_icon from "../images/language_icon.png";
import { userStore } from "../stores/userStore";

export default function LanguageIcon() {
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef(null);

  const setLanguage = userStore((state) => state.setLanguage);
  const currentLocale = userStore((state) => state.locale);

  const handleSelect = (lang) => {
    if (lang !== currentLocale) {
      setLanguage(lang);
    }
    setOpen(false);
  };

  // Fecha ao clicar fora
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  return (
    <div ref={dropdownRef} className="relative w-14 h-14 flex items-center justify-center">
      <div className="relative">
        <button
          onClick={() => setOpen(!open)}
          className="focus:outline-none"
          aria-label="Selecionar idioma"
        >
          <img
            src={language_icon}
            alt="Selecionar idioma"
            className="w-12 h-12 md:w-14 md:h-14"
          />
        </button>

        {/* Dropdown SEM ser removido do DOM */}
        <div
          className={`absolute left-full top-1/2 -translate-y-1/2 w-20 bg-white border border-gray-200 rounded shadow-lg z-50
            transition-all duration-300 ease-in-out
            ${open ? "opacity-100 scale-100 pointer-events-auto" : "opacity-0 scale-95 pointer-events-none"}`}
        >
          <ul className="flex flex-row items-center justify-center text-sm text-gray-800 text-center">
            <li
              onClick={() => handleSelect("pt")}
              className="px-4 py-2 hover:bg-gray-100 cursor-pointer rounded transition-all duration-200"
            >
              🇵🇹
            </li>
            <li
              onClick={() => handleSelect("en")}
              className="px-4 py-2 hover:bg-gray-100 cursor-pointer rounded transition-all duration-200"
            >
              🇬🇧
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}





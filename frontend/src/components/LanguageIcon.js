import { useState } from "react";
import language_icon from "../images/language_icon.png"; // substitui pela tua imagem real

export default function LanguageIcon() {
  const [open, setOpen] = useState(false);

  return (
    <div className="absolute top-6 right-6 z-50">
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

        {open && (
          <div className="absolute right-0 mt-2 w-40 bg-white border border-gray-200 rounded shadow-lg">
            <ul className="text-sm text-gray-800">
              <li className="px-4 py-2 hover:bg-gray-100 cursor-pointer">🇵🇹 Português</li>
              <li className="px-4 py-2 hover:bg-gray-100 cursor-pointer">🇬🇧 English</li>
              <li className="px-4 py-2 hover:bg-gray-100 cursor-pointer">🇫🇷 Français</li>
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}

import { useState } from "react";
import languageselector from "../images/languageselector.png"; // substitui com o teu ficheiro real

export default function LanguageSelector() {
  const [open, setOpen] = useState(false);

  return (
    <div className="fixed top-4 right-4 z-50">
      <div className="relative">
        <button
          onClick={() => setOpen(!open)}
          className="focus:outline-none"
          aria-label="Selecionar idioma"
        >
          <img src={languageselector} alt="Idioma" className="w-6 h-6 md:w-8 md:h-8" />
        </button>

        {open && (
          <div className="absolute right-0 mt-2 w-32 bg-white border border-gray-200 rounded shadow-md">
            <ul className="text-sm text-gray-800">
              <li className="px-4 py-2 hover:bg-gray-100 cursor-pointer">🇵🇹 Português</li>
              <li className="px-4 py-2 hover:bg-gray-100 cursor-pointer">🇬🇧 English</li>
          </div>
        )}
      </div>
    </div>
  );
}

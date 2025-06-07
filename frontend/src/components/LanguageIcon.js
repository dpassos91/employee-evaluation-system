import { useState } from "react";
import language_icon from "../images/language_icon.png";

export default function LanguageIcon() {
  const [open, setOpen] = useState(false);

  return (
    <div className="relative w-14 h-14 flex items-center justify-center">
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

<div
  className={`absolute left-full top-1/2 -translate-y-1/2 w-20 bg-white border border-gray-200 rounded shadow-lg
  transition-transform transition-opacity duration-500 ease-in-out
  ${open ? "opacity-100 scale-100" : "opacity-0 scale-95 pointer-events-none"}`}
>
  <ul className="flex flex-row items-center justify-center text-sm text-gray-800 text-center">
    <li
      className={`px-4 py-2 hover:bg-gray-100 cursor-pointer rounded 
      transition-all duration-500 ease-in-out delay-75
      ${open ? "opacity-100 scale-100" : "opacity-0 scale-95"}`}
    >
      🇵🇹
    </li>
    <li
      className={`px-4 py-2 hover:bg-gray-100 cursor-pointer rounded 
      transition-all duration-500 ease-in-out delay-150
      ${open ? "opacity-100 scale-100" : "opacity-0 scale-95"}`}
    >
      🇬🇧
    </li>
  </ul>
</div>
      </div>
    </div>
  );
}


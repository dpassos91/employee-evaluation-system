import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import pt from "../translations/pt.json";
import en from "../translations/en.json";

// Dicionário de traduções suportadas
const messages = {
  pt,
  en,
};

export const userStore = create(
  persist(
    (set, get) => ({
      username: "",
      mediatype: {},
      locale: "pt", // idioma por omissão
      translations: pt,

      updateName: (username) => set({ username }),
      updateMediatype: (mediatype) => set({ mediatype }),

      initializeLanguage: () => {
        const storedLocale = localStorage.getItem("locale");
        const browserLocale = navigator.language.split("-")[0];
        const lang = storedLocale || browserLocale;

        if (messages[lang]) {
          set({ locale: lang, translations: messages[lang] });
        } else {
          set({ locale: "pt", translations: pt });
        }
      },

      setLanguage: (lang) => {
        if (messages[lang]) {
          localStorage.setItem("locale", lang);
          set({ locale: lang, translations: messages[lang] });
        }
      },
    }),
    {
      name: "user-store",
      storage: createJSONStorage(() => sessionStorage),
      partialize: (state) => ({
        username: state.username,
        mediatype: state.mediatype,
      }),
    }
  )
);


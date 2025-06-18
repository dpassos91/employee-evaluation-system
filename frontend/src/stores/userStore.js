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
      user: null, // Objeto com info do user autenticado
      mediatype: {},
      locale: "pt",
      translations: pt,

      // Métodos para manipular o user autenticado
      setUser: (userObj) => set({ user: userObj }),
      clearUser: () => set({ user: null }),

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
        user: state.user,             // <--- GUARDA o objeto user!
        mediatype: state.mediatype,
        // podes guardar outros campos aqui se precisares
      }),
    }
  )
);



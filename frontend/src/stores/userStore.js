import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import pt from "../translations/pt.json";
import en from "../translations/en.json";

// Supported translation dictionaries
const messages = {
  pt,
  en,
};

/**
 * @typedef {Object} User
 * @property {number|string} id - User's unique identifier
 * @property {string} email - User's email
 * @property {string} role - User's role (e.g., "admin", "user")
 * @property {string} [name] - User's name (optional)
 * @property {string} [photograph] - Profile photo path or base64 (optional)
 */

/**
 * Zustand store for user authentication and profile state
 */
export const userStore = create(
  persist(
    (set, get) => ({
      /** @type {User|null} Authenticated user object (now includes photograph field) */
      user: null,

      /** Media type state for responsiveness */
      mediatype: {},

      /** List of missing required profile fields */
      missingFields: [],

      /** Locale/language in use */
      locale: "pt",

      /** Profile completion status */
      profileComplete: null,

      /** Translations object for selected language */
      translations: pt,

      /**
       * Clears authenticated user and related profile state.
       * @returns {void}
       */
      clearUser: () =>
        set({ user: null, profileComplete: null, missingFields: [] }),

      /**
       * Initializes language from localStorage or browser settings.
       * @returns {void}
       */
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

      /**
       * Sets language and updates translations.
       * @param {string} lang - Language code (e.g., "pt", "en")
       * @returns {void}
       */
      setLanguage: (lang) => {
        if (messages[lang]) {
          localStorage.setItem("locale", lang);
          set({ locale: lang, translations: messages[lang] });
        }
      },

      /**
       * Sets list of missing required fields for profile completion.
       * @param {Array} missingFields
       * @returns {void}
       */
      setMissingFields: (missingFields) => set({ missingFields }),

      /**
       * Sets profile completion state.
       * @param {boolean|null} profileComplete
       * @returns {void}
       */
      setProfileComplete: (profileComplete) => set({ profileComplete }),

      /**
       * Sets authenticated user (now can include photograph field).
       * @param {User|null} userObj
       * @returns {void}
       */
      setUser: (userObj) => set({ user: userObj }),

      /**
       * Sets media type state (responsive design).
       * @param {Object} mediatype
       * @returns {void}
       */
      updateMediatype: (mediatype) => set({ mediatype }),
    }),
    {
      name: "user-store",
      storage: createJSONStorage(() => sessionStorage),
      partialize: (state) => ({
        user: state.user,
        mediatype: state.mediatype,
        profileComplete: state.profileComplete,
        missingFields: state.missingFields,
        // add more fields if you need to persist more state
      }),
    }
  )
);





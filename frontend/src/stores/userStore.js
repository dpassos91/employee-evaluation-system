import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

// Define the Zustand store
export const userStore = create(
  persist(
    (set) => ({
      username: "",
      mediatype: {},
      updateName: (username) => set({ username }),
      updateMediatype: (mediatype) => set({ mediatype }),
    }),
    {
      name: "user-store",
      storage: createJSONStorage(() => sessionStorage),
    }
  )
);

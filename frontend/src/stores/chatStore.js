import { create } from "zustand";

/**
 * Store to manage chat messages.
 * This store holds an array of messages and provides methods to add and clear messages.
 */
export const useChatStore = create((set) => ({
  messages: [],
  addMessage: (msg) =>
    set((state) => ({ messages: [...state.messages, msg] })),
  clearMessages: () => set({ messages: [] }),
}));

import { create } from "zustand";

/**
 * Global sendMessage function reference for sending chat messages.
 * This will be set once in the App after the WebSocket is created.
 * @type {function|null}
 */
let sendMessageRef = null;

/**
 * Sets the global sendMessage function used by the chat store to send messages.
 * Should be called from the App after establishing the WebSocket connection.
 * @param {Object} obj - An object containing the sendMessage function ({ sendMessage }).
 */
export const setWebSocketRef = (obj) => { 
  sendMessageRef = obj.sendMessage; 
};

/**
 * Zustand store for chat messages.
 * Provides methods for managing and sending chat messages in real time.
 *
 * @returns {object} Chat store state and actions
 * @property {Array} messages - Array of chat message objects.
 * @property {function} addMessage - Adds a single message to the chat.
 * @property {function} clearMessages - Clears all messages in the chat.
 * @property {function} sendMessage - Sends a chat message via the global sendMessage function.
 */
export const useChatStore = create((set) => ({
  /**
   * Array of messages in the current chat.
   * @type {Array}
   */
  messages: [],

  /**
   * Adds a single message to the messages array.
   * @param {object} msg - The message object to add.
   */
  addMessage: (msg) =>
    set((state) => ({ messages: [...state.messages, msg] })),

  /**
   * Clears all messages from the chat.
   */
  clearMessages: () => set({ messages: [] }),

  /**
   * Sends a chat message via the global sendMessage function.
   * Does nothing if the function is not available.
   * @param {object} msg - The message object to send (must be serializable).
   */
  sendMessage: (msg) => {
    if (typeof sendMessageRef === "function") {
      sendMessageRef(msg);
    }
  }
}));



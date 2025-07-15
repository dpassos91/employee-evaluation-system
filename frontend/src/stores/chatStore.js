import { create } from "zustand";
import { userStore } from "./userStore";

/**
 * Global reference to the sendMessage function,
 * set in App after WebSocket is created.
 */
let sendMessageRef = null;

/**
 * Setter to store the global WebSocket sendMessage function.
 * Call this after initializing WebSocket in App.
 * @param {Object} obj - An object containing the sendMessage function.
 */
export const setWebSocketRef = (obj) => {
  sendMessageRef = obj.sendMessage;
};

/**
 * Zustand store for managing chat state, conversations, and online presence.
 * Provides methods for adding messages, updating contacts, handling active chat, and sending messages.
 */
export const useChatStore = create((set, get) => ({
  /**
   * Maps userId to an array of MessageDto (conversation history with each user).
   * @type {Object.<number, Array>}
   */
  messagesByConversation: {},

  /**
   * The userId of the currently active conversation (opened chat window).
   * @type {?number}
   */
  activeConversationId: null,

  /**
   * List of contacts/conversations (ConversationDto), as provided by the backend.
   * Each item should include at least: otherUserId, otherUserName, online, etc.
   * @type {Array}
   */
  contacts: [],

  /**
   * Sets the currently active conversation (selected userId).
   * @param {number} userId
   */
  setActiveConversation: (userId) => set({ activeConversationId: userId }),

  /**
   * Returns all messages for the current active conversation.
   * @returns {Array}
   */
  getMessagesForActiveConversation: () => {
    const { messagesByConversation, activeConversationId } = get();
    return messagesByConversation[activeConversationId] || [];
  },

  /**
   * Sets the entire array of messages for a given conversation (overwrites).
   * Useful when fetching the full history from REST.
   * @param {number} userId
   * @param {Array} messages
   */
  setMessagesForConversation: (userId, messages) =>
    set((state) => ({
      messagesByConversation: {
        ...state.messagesByConversation,
        [userId]: messages,
      },
    })),

  /**
   * Adds a new message to the correct conversation.
   * Prevents duplicates (checks sender, receiver, content, and timestamp).
   * @param {Object} msg - MessageDto object
   */
  addMessage: (msg) => {
    const myId = userStore.getState().user.id;
    const otherId = msg.senderId === myId ? msg.receiverId : msg.senderId;
    set((state) => {
      const msgs = state.messagesByConversation[otherId] || [];
      // Checks for duplicates by unique message fields
      const exists = msgs.some(
        (m) =>
          m.content === msg.content &&
          m.senderId === msg.senderId &&
          m.receiverId === msg.receiverId &&
          (m.timestamp === msg.timestamp || m.createdAt === msg.createdAt)
      );
      if (exists) return {};
      return {
        messagesByConversation: {
          ...state.messagesByConversation,
          [otherId]: [...msgs, msg],
        },
      };
    });
  },

  /**
   * Sets the full array of contacts/conversations as fetched from the API.
   * @param {Array} contacts - Array of ConversationDto objects
   */
  setContacts: (contacts) => set({ contacts }),

  /**
   * Updates the online status for a specific contact (real-time presence).
   * Used by the WebSocket handler for status_update events.
   * @param {number} userId
   * @param {boolean} online
   */
  updateContactStatus: (userId, online) =>
    set((state) => ({
      contacts: state.contacts.map((c) =>
        c.otherUserId === userId ? { ...c, online } : c
      ),
    })),

  /**
   * Sends a message via WebSocket (if available).
   * Uses the global sendMessageRef set from App.
   * @param {Object} msg - MessageDto (or minimal message data)
   */
  sendMessage: (msg) => {
    if (typeof sendMessageRef === "function") {
      sendMessageRef(msg);
    }
  },
}));


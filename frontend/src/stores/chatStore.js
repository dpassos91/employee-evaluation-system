import { create } from "zustand";
import { userStore } from "./userStore";

/**
 * Global sendMessage function reference for sending chat messages via WebSocket.
 * Será definida na App depois do WebSocket ser criado.
 */
let sendMessageRef = null;

/**
 * Setter global para guardar a função que envia mensagens pelo WebSocket.
 * Chama isto na App depois de criares o WebSocket!
 */
export const setWebSocketRef = (obj) => {
  sendMessageRef = obj.sendMessage;
};

export const useChatStore = create((set, get) => ({
  messagesByConversation: {},  // { [userId]: [msg, msg, ...] }
  activeConversationId: null,

  setActiveConversation: (userId) => set({ activeConversationId: userId }),

  getMessagesForActiveConversation: () => {
    const { messagesByConversation, activeConversationId } = get();
    return messagesByConversation[activeConversationId] || [];
  },

  setMessagesForConversation: (userId, messages) =>
    set((state) => ({
      messagesByConversation: {
        ...state.messagesByConversation,
        [userId]: messages
      }
    })),

  addMessage: (msg) => {
    const myId = userStore.getState().user.id;
    // Determina qual o outro user na conversa (sender ou receiver)
    const otherId = msg.senderId === myId ? msg.receiverId : msg.senderId;
    set((state) => {
      const msgs = state.messagesByConversation[otherId] || [];
      return {
        messagesByConversation: {
          ...state.messagesByConversation,
          [otherId]: [...msgs, msg]
        }
      };
    });
  },

  /**
   * Envia uma mensagem via WebSocket global (se estiver disponível).
   * Usa o sendMessageRef que é definido pelo setWebSocketRef na App.
   */
  sendMessage: (msg) => {
    if (typeof sendMessageRef === "function") {
      sendMessageRef(msg);
    }
  },
}));



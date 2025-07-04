import PageLayout from "../components/PageLayout";
import { useState, useEffect, useRef } from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { messageAPI } from "../api/messageAPI";
import { userStore } from "../stores/userStore";
import { useChatStore } from "../stores/chatStore"; // Import global chat store

/**
 * ChatPage component.
 * Handles the sidebar conversations, main chat thread, and message input UI.
 * Messages are always read from the global chatStore, which is kept in sync with backend and WebSocket events.
 */
export default function ChatPage() {
  /** Sidebar state: list of conversations (not messages) */
  const [sidebarConversations, setSidebarConversations] = useState([]);
  /** Currently selected conversation ID */
  const [selectedConvId, setSelectedConvId] = useState(null);
  /** Loading flags for sidebar and messages */
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [loadingSidebar, setLoadingSidebar] = useState(true);
  /** Input and error state */
  const [input, setInput] = useState("");
  const [error, setError] = useState(null);
  /** i18n */
  const intl = useIntl();
  /** Current authenticated user */
  const { user } = userStore();

  /** --------- Chat store integration --------- */
  /**
   * Read messages from the global chatStore.
   * All chat updates (live or via API) should flow through this store.
   */
  const messages = useChatStore((s) => s.messages);
  const clearMessages = useChatStore((s) => s.clearMessages);
  const addMessage = useChatStore((s) => s.addMessage);
  const sendMessage = useChatStore((s) => s.sendMessage);

  /** Refs to keep selectedConvId and user.id updated inside async callbacks */
  const selectedConvIdRef = useRef(selectedConvId);
  const userIdRef = useRef(user?.id);

  useEffect(() => { selectedConvIdRef.current = selectedConvId; }, [selectedConvId]);
  useEffect(() => { userIdRef.current = user?.id; }, [user]);

  /**
   * Fetch sidebar conversations on mount.
   * Sets the initial selected conversation.
   */
  useEffect(() => {
    setLoadingSidebar(true);
    messageAPI.chatSidebarConversations()
      .then(convs => {
        setSidebarConversations(convs || []);
        if (convs && convs.length > 0) {
          setSelectedConvId(convs[0].otherUserId);
        }
      })
      .catch(() => setError("Failed to load conversations"))
      .finally(() => setLoadingSidebar(false));
  }, []);

  /**
   * Fetch messages for the selected conversation.
   * Populates the global chatStore (not local state!).
   * Also marks messages as read and refreshes sidebar.
   */
  useEffect(() => {
    if (!selectedConvId) return;
    setLoadingMessages(true);
    clearMessages(); // Reset messages in the store
    setError(null);

    // Mark messages as read and refresh sidebar
    messageAPI.markMessagesAsRead(selectedConvId)
      .then(() => messageAPI.chatSidebarConversations())
      .then(convs => setSidebarConversations(convs || []))
      .catch(() => setError("Failed to update read state"));

    // Fetch messages and add to store
    messageAPI.getConversation(selectedConvId)
      .then((msgs) => {
        (msgs || []).forEach(m => addMessage(m));
      })
      .catch(() => setError("Failed to load messages"))
      .finally(() => setLoadingMessages(false));
  }, [selectedConvId, clearMessages, addMessage]);

  /** 
   * Ref to scroll to the bottom of the messages list.
   * Keeps scroll always at the latest message.
   */
  const messagesEndRef = useRef(null);
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollTop = messagesEndRef.current.scrollHeight;
    }
  }, [messages]);

  /**
   * Finds the selected contact details (avatar, name) for the chat header.
   */
  const selectedContact = sidebarConversations.find(c => c.otherUserId === selectedConvId);

  /**
 * Handles sending a chat message.
 * Validates input, then sends the message to the backend via the global WebSocket,
 * using the sendMessage function from the chat store.
 * After sending, clears the input field.
 */
const handleSend = () => {
  if (!input.trim()) return;
  sendMessage({
    senderId: user.id,
    receiverId: selectedConvId,
    content: input
  });
  setInput("");
};

  return (
    <PageLayout
      title={<FormattedMessage id="chat.title" defaultMessage="Mensagens" />}
      subtitle={<FormattedMessage id="chat.subtitle" defaultMessage="O teu histórico de conversas" />}
    >
      <div className="flex w-full max-w-6xl h-[500px] mx-auto bg-white rounded-2xl shadow overflow-hidden">
        {/* Sidebar */}
        <aside className="w-64 bg-gray-100 border-r flex flex-col">
          <div className="p-3 border-b font-semibold text-lg">
            <FormattedMessage id="chat.conversations" defaultMessage="Conversas" />
          </div>
          <div className="flex-1 overflow-y-auto">
            {loadingSidebar && (
              <div className="text-center text-gray-400 py-4">A carregar conversas...</div>
            )}
            {sidebarConversations.map(conv => (
              <button
                key={conv.otherUserId}
                className={`
                  flex items-center w-full px-4 py-3 gap-3 hover:bg-gray-200
                  ${selectedConvId === conv.otherUserId ? "bg-gray-300" : ""}
                `}
                onClick={() => setSelectedConvId(conv.otherUserId)}
              >
                <img src={conv.otherUserAvatar} alt="" className="w-10 h-10 rounded-full object-cover border" />
                <div className="flex-1 text-left">
                  <div className="font-medium">{conv.otherUserName}</div>
                  <div className="text-xs text-gray-500 truncate">{conv.lastMessage}</div>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <div className="text-[10px] text-gray-400">{conv.lastMessageTime && conv.lastMessageTime.substring(11, 16)}</div>
                  {conv.unreadCount > 0 && (
                    <span className="bg-[#D41C1C] text-white rounded-full text-xs px-2">{conv.unreadCount}</span>
                  )}
                  {conv.online && (
                    <span className="inline-block w-2 h-2 rounded-full bg-green-500" title="Online"></span>
                  )}
                </div>
              </button>
            ))}
          </div>
        </aside>

        {/* Main chat */}
        <section className="flex-1 flex flex-col">
          {/* Header */}
          <div className="flex items-center gap-3 px-6 py-4 border-b">
            {selectedContact && (
              <>
                <img src={selectedContact.otherUserAvatar} alt="" className="w-10 h-10 rounded-full border" />
                <div>
                  <div className="font-semibold">{selectedContact.otherUserName}</div>
                  <div className="text-xs text-gray-400">
                    {selectedContact.online
                      ? <FormattedMessage id="chat.online" defaultMessage="Online" />
                      : selectedContact.role
                        ? selectedContact.role.charAt(0).toUpperCase() + selectedContact.role.slice(1).toLowerCase()
                        : ""}
                  </div>
                </div>
              </>
            )}
          </div>
          {/* Mensagens */}
          <div 
          ref={messagesEndRef}
          className="flex-1 px-6 py-4 overflow-y-auto bg-gray-50 space-y-2">
            {loadingMessages && <div className="text-center text-gray-400">A carregar...</div>}
            {error && <div className="text-center text-red-500">{error}</div>}
            {messages.map((msg, idx) => {
              const isMine = msg.senderId === user?.id || msg.sentByMe;
              return (
                <div
                  key={msg.id || idx}
                  className={`flex ${isMine ? "justify-end" : "justify-start"}`}
                >
                  <div className={`
                    px-4 py-2 rounded-2xl shadow
                    ${isMine
                      ? "bg-[#D41C1C] text-white rounded-br-sm"
                      : "bg-white text-gray-800 rounded-bl-sm border"}
                    max-w-[70%]
                  `}>
                    <div className="text-sm">{msg.content}</div>
                    <div className="text-[10px] text-right opacity-70 mt-1">{msg.timestamp || msg.createdAt}</div>
                  </div>
                </div>
              );
            })}
          </div>
          {/* Input */}
          <div className="flex items-center gap-2 p-4 border-t bg-white">
            <input
              className="flex-1 rounded-2xl border px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D41C1C] transition"
              placeholder={intl.formatMessage({ id: "chat.placeholder", defaultMessage: "Escreve uma mensagem..." })}
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === "Enter" && handleSend()}
            />
            <button
              className="bg-[#D41C1C] text-white rounded-full w-10 h-10 flex items-center justify-center hover:bg-red-700 transition"
              onClick={handleSend}
              disabled={!input.trim()}
            >
              <svg fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor"
                className="w-6 h-6"><path strokeLinecap="round" strokeLinejoin="round" d="M3 10l9-6 9 6M4 10v10a1 1 0 001 1h2a1 1 0 001-1V14a1 1 0 011-1h2a1 1 0 011 1v6a1 1 0 001 1h2a1 1 0 001-1V10" /></svg>
            </button>
          </div>
        </section>
      </div>
    </PageLayout>
  );
}







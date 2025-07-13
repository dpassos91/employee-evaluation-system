import PageLayout from "../components/PageLayout";
import { useState, useEffect, useRef } from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { messageAPI } from "../api/messageAPI";
import { userStore } from "../stores/userStore";
import { useChatStore } from "../stores/chatStore";
import { notificationAPI } from "../api/notificationAPI";
import { useLocation, useNavigate } from "react-router-dom";
import { profileAPI } from "../api/profileAPI";
import profileIcon from "../images/profile_icon.png";

export default function ChatPage() {
  // Sidebar: lista de conversas
  const [sidebarConversations, setSidebarConversations] = useState([]);
  const [loadingSidebar, setLoadingSidebar] = useState(true);
  const [tempContact, setTempContact] = useState(null);

  const intl = useIntl();
  const { user } = userStore();
  const navigate = useNavigate();

  // ChatStore (Zustand) - só hooks!
  
  const addMessage = useChatStore((s) => s.addMessage);
  const clearMessages = useChatStore((s) => s.clearMessages);
  const setActiveConversation = useChatStore((s) => s.setActiveConversation);
  const setMessagesForConversation = useChatStore((s) => s.setMessagesForConversation);
  const activeConversationId = useChatStore((s) => s.activeConversationId);
  const messagesByConversation = useChatStore((s) => s.messagesByConversation);
const messages = messagesByConversation[activeConversationId] || [];
  // UI
  const [input, setInput] = useState("");
  const [error, setError] = useState(null);
  const [loadingMessages, setLoadingMessages] = useState(false);

  // URL para nova conversa
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const userIdFromQuery = searchParams.get("user");

  // 1. Carrega conversas da sidebar (histórico) ao montar ou mudar a query
  useEffect(() => {
    setLoadingSidebar(true);
    messageAPI.chatSidebarConversations()
      .then(convs => {
        setSidebarConversations(convs || []);
        // Define conversa ativa só aqui!
        const current = useChatStore.getState().activeConversationId;
        if (userIdFromQuery && current !== userIdFromQuery) {
          useChatStore.getState().setActiveConversation(userIdFromQuery);
        } else if (!userIdFromQuery && convs && convs.length > 0 && current !== convs[0].otherUserId) {
          useChatStore.getState().setActiveConversation(convs[0].otherUserId);
        } else if (!userIdFromQuery && (!convs || convs.length === 0) && current !== null) {
          useChatStore.getState().setActiveConversation(null);
        }
      })
      .catch(() => setError("Failed to load conversations"))
      .finally(() => setLoadingSidebar(false));
  }, [userIdFromQuery]);

  // 2. Ao mudar de conversa ativa, carrega o histórico (só se ainda não existir)
  const messagesExist = (messagesByConversation[activeConversationId] || []).length > 0;

  useEffect(() => {
    if (!activeConversationId) return;
    setLoadingMessages(true);
    setError(null);

    // Marca mensagens como lidas
    messageAPI.markMessagesAsRead(activeConversationId)
      .then(() => notificationAPI.markAllMessageNotificationsAsRead())
      .catch(() => setError("Failed to update read state"));

    if (!messagesExist) {
      messageAPI.getConversation(activeConversationId)
        .then((msgs) => {
          setMessagesForConversation(activeConversationId, msgs || []);
        })
        .catch(() => setError("Failed to load messages"))
        .finally(() => setLoadingMessages(false));
    } else {
      setLoadingMessages(false);
    }
  }, [activeConversationId, setMessagesForConversation, messagesExist]);

  // 3. Contacto selecionado
  const selectedContact = sidebarConversations.find(c => c.otherUserId === activeConversationId);
  const contactInfo = selectedContact || tempContact;

  // 4. Fetch tempContact se nova conversa
  useEffect(() => {
    if (
      activeConversationId &&
      !sidebarConversations.some(c => c.otherUserId === activeConversationId)
    ) {
      profileAPI.getProfileById(activeConversationId, sessionStorage.getItem("authToken"))
        .then(data => {
          setTempContact({
            otherUserId: data.id,
            otherUserName: data.name || `${data.firstName || ""} ${data.lastName || ""}`.trim(),
            otherUserAvatar: data.photograph,
          });
        })
        .catch(() => setTempContact(null));
    } else {
      setTempContact(null);
    }
  }, [activeConversationId, sidebarConversations]);

  // 5. Handler para enviar mensagem
function fetchSidebarConversations() {
  setLoadingSidebar(true);
  messageAPI.chatSidebarConversations()
    .then(convs => setSidebarConversations(convs || []))
    .catch(() => setError("Failed to load conversations"))
    .finally(() => setLoadingSidebar(false));
}

function updateSidebarWithLastMessageOptimistically(userId, content) {
  setSidebarConversations(prev =>
    prev.map(conv =>
      conv.otherUserId === userId
        ? {
            ...conv,
            lastMessage: content,
            lastMessageTime: new Date().toISOString(), // ou Date.now(), conforme mostras na UI
          }
        : conv
    )
  );
}

  const sendMessage = useChatStore((s) => s.sendMessage);
  
const handleSend = () => {
  if (!input.trim()) return;
  const msg = {
    senderId: user.id,
    receiverId: activeConversationId,
    content: input,
  };
  sendMessage(msg);
  addMessage(msg);
  setInput("");
  updateSidebarWithLastMessageOptimistically(activeConversationId, input);
};

  // 6. Scroll always to last message
  const messagesEndRef = useRef(null);
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollTop = messagesEndRef.current.scrollHeight;
    }
  }, [messages]);

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
                  ${activeConversationId === conv.otherUserId ? "bg-gray-300" : ""}
                `}
                onClick={() => {
                  setActiveConversation(conv.otherUserId);
                  if (userIdFromQuery) navigate("/chat", { replace: true });
                }}
              >
                <img
                  src={
                    conv.otherUserAvatar && conv.otherUserAvatar.trim() !== ""
                      ? profileAPI.getPhoto(conv.otherUserAvatar)
                      : profileIcon
                  }
                  alt={conv.otherUserName}
                  className="w-10 h-10 rounded-full border object-cover"
                  style={{ display: "block" }}
                  onError={e => {
                    e.target.onerror = null;
                    e.target.src = profileIcon;
                  }}
                />
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
            {contactInfo && (
              <>
                <img
                  src={
                    contactInfo.otherUserAvatar && contactInfo.otherUserAvatar.trim() !== ""
                      ? profileAPI.getPhoto(contactInfo.otherUserAvatar)
                      : profileIcon
                  }
                  alt={contactInfo.otherUserName || ""}
                  className="w-10 h-10 rounded-full border object-cover"
                  style={{ display: "block" }}
                  onError={e => {
                    e.target.onerror = null;
                    e.target.src = profileIcon;
                  }}
                />
                <div>
                  <div className="font-semibold">{contactInfo.otherUserName}</div>
                  {/* Só mostra status/role se vierem da sidebar */}
                  {selectedContact ? (
                    <div className="text-xs text-gray-400">
                      {selectedContact.online
                        ? <FormattedMessage id="chat.online" defaultMessage="Online" />
                        : selectedContact.role
                          ? selectedContact.role.charAt(0).toUpperCase() + selectedContact.role.slice(1).toLowerCase()
                          : ""}
                    </div>
                  ) : null}
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








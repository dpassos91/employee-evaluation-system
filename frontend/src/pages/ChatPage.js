import PageLayout from "../components/PageLayout";
import { useState, useEffect, useRef } from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { messageAPI } from "../api/messageAPI";
import { userStore } from "../stores/userStore";
import useWebSocket from "../hooks/useWebSocket";

export default function ChatPage() {
  const [sidebarConversations, setSidebarConversations] = useState([]);
  const [selectedConvId, setSelectedConvId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [loadingSidebar, setLoadingSidebar] = useState(true);
  const [input, setInput] = useState("");
  const [error, setError] = useState(null);
  const intl = useIntl();
  const { user } = userStore();
  const token = sessionStorage.getItem("authToken");

  // Refs para manter selectedConvId e user.id atualizados dentro do callback do WebSocket
  const selectedConvIdRef = useRef(selectedConvId);
  const userIdRef = useRef(user?.id);

  useEffect(() => {
    selectedConvIdRef.current = selectedConvId;
  }, [selectedConvId]);

  useEffect(() => {
    userIdRef.current = user?.id;
  }, [user]);

  // Buscar conversas da sidebar ao carregar a página
  useEffect(() => {
    setLoadingSidebar(true);
    messageAPI.chatSidebarConversations()
      .then(convs => {
        setSidebarConversations(convs || []);
        if (convs && convs.length > 0) {
          setSelectedConvId(convs[0].otherUserId);
        }
      })
      .catch(() => setError("Erro ao carregar conversas"))
      .finally(() => setLoadingSidebar(false));
  }, []);

  // Buscar mensagens da conversa selecionada
  useEffect(() => {
    if (!selectedConvId) return;
    setLoadingMessages(true);
    setMessages([]);
    setError(null);

    // Mark messages as read
    // This will update the unread count in the sidebar
    messageAPI.markMessagesAsRead(selectedConvId)
    .then(() => {
      // Depois de marcar como lidas, refresca sidebar
      return messageAPI.chatSidebarConversations();
    })
    .then((convs) => {
      setSidebarConversations(convs || []);
    })
    .catch(() => setError("Erro ao atualizar estado das mensagens"));  

    // Always seeks the conversation messages regardless of read status
    messageAPI.getConversation(selectedConvId)
      .then((msgs) => setMessages(msgs || []))
      .catch(() => setError("Erro ao carregar mensagens"))
      .finally(() => setLoadingMessages(false));
  }, [selectedConvId]);

  // WebSocket para mensagens em tempo real
  const { sendMessage, isConnected } = useWebSocket(
    "wss://localhost:8443/grupo7/websocket/chat",
    token,
    (data) => {
      if (data.type === "chat_message") {
        // Só mostrar se for para a conversa aberta!
        if (
          (data.senderId === selectedConvIdRef.current && data.receiverId === userIdRef.current) ||
          (data.senderId === userIdRef.current && data.receiverId === selectedConvIdRef.current)
        ) {
          setMessages((prev) => [
            ...prev,
            {
              ...data,
              sentByMe: data.senderId === userIdRef.current
            }
          ]);
        }
          //    messageAPI.chatSidebarConversations()
        //.then((convs) => setSidebarConversations(convs || []));
      }
    }
  );

  // Send message handler
  const handleSend = () => {
    if (!input.trim()) return;
    sendMessage({
      senderId: user.id,
      receiverId: selectedConvId,
      content: input
    });
    setInput("");
  };
  
  // Scroll down to the latest message
  useEffect(() => {
  // Sempre que as mensagens mudam, faz scroll para baixo
  if (messagesEndRef.current) {
    messagesEndRef.current.scrollTop = messagesEndRef.current.scrollHeight;
  }
}, [messages]);

  //Selected contact on the sidebar
  const selectedContact = sidebarConversations.find(c => c.otherUserId === selectedConvId);

  // Ref to scroll to bottom of messages
  const messagesEndRef = useRef(null);

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






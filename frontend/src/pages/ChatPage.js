import PageLayout from "../components/PageLayout";
import { useState, useEffect } from "react";
import { FormattedMessage, useIntl } from "react-intl";
import { messageAPI } from "../api/messageAPI";

// Simulação de contactos disponíveis (até teres endpoint real)
const contacts = [
  { id: 2, name: "João Silva", avatar: "https://randomuser.me/api/portraits/men/32.jpg" },
  { id: 3, name: "Maria Santos", avatar: "https://randomuser.me/api/portraits/women/44.jpg" }
];

export default function ChatPage() {
  const [selectedConvId, setSelectedConvId] = useState(contacts[0].id);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [input, setInput] = useState("");
  const [error, setError] = useState(null);
  const intl = useIntl();

  // Sempre que muda a conversa selecionada, faz fetch ao backend
  useEffect(() => {
    setLoading(true);
    setMessages([]);
    setError(null);
    messageAPI.getConversation(selectedConvId)
      .then((msgs) => setMessages(msgs || []))
      .catch(() => setError("Erro ao carregar mensagens"))
      .finally(() => setLoading(false));
  }, [selectedConvId]);

  // Enviar mensagem usando messageAPI
  const handleSend = async () => {
    if (!input.trim()) return;
    try {
      await messageAPI.sendMessage({
        receiverId: selectedConvId,
        content: input
      });
      // Podes adicionar imediatamente ao state (optimistic UI)
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now(), // id temporário
          senderId: null, // current user (o backend sabe)
          receiverId: selectedConvId,
          content: input,
          timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
          sentByMe: true
        }
      ]);
      setInput("");
    } catch {
      setError("Erro ao enviar mensagem");
    }
  };

  // Encontras o contacto atualmente selecionado
  const selectedContact = contacts.find((c) => c.id === selectedConvId);

  return (
    <PageLayout
      title={<FormattedMessage id="chat.title" defaultMessage="Mensagens" />}
      subtitle={<FormattedMessage id="chat.subtitle" defaultMessage="O teu histórico de conversas" />}
    >
      <div className="flex w-full max-w-6xl h-[500px] mx-auto bg-white rounded-2xl shadow overflow-hidden">
        {/* Sidebar */}
        <aside className="w-64 bg-gray-100 border-r flex flex-col">
          <div className="flex-1 overflow-y-auto">
            {contacts.map(conv => (
              <button
                key={conv.id}
                className={`
                  flex items-center w-full px-4 py-3 gap-3 hover:bg-gray-200
                  ${selectedConvId === conv.id ? "bg-gray-300" : ""}
                `}
                onClick={() => setSelectedConvId(conv.id)}
              >
                <img src={conv.avatar} alt="" className="w-10 h-10 rounded-full object-cover border" />
                <div className="flex-1 text-left">
                  <div className="font-medium">{conv.name}</div>
                </div>
              </button>
            ))}
          </div>
        </aside>

        {/* Main chat */}
        <section className="flex-1 flex flex-col">
          {/* Header */}
          <div className="flex items-center gap-3 px-6 py-4 border-b">
            <img src={selectedContact.avatar} alt="" className="w-10 h-10 rounded-full border" />
            <div>
              <div className="font-semibold">{selectedContact.name}</div>
              <div className="text-xs text-gray-400">Online</div>
            </div>
          </div>
          {/* Mensagens */}
          <div className="flex-1 px-6 py-4 overflow-y-auto bg-gray-50 space-y-2">
            {loading && <div className="text-center text-gray-400">A carregar...</div>}
            {error && <div className="text-center text-red-500">{error}</div>}
            {messages.map((msg, idx) => (
              <div
                key={msg.id || idx}
                className={`flex ${msg.sentByMe || (msg.senderId === undefined) ? "justify-end" : "justify-start"}`}
              >
                <div className={`
                  px-4 py-2 rounded-2xl shadow
                  ${msg.sentByMe || (msg.senderId === undefined) || (msg.senderId === null)
                    ? "bg-[#D41C1C] text-white rounded-br-sm"
                    : "bg-white text-gray-800 rounded-bl-sm border"}
                  max-w-[70%]
                  `}
                >
                  <div className="text-sm">{msg.content}</div>
                  <div className="text-[10px] text-right opacity-70 mt-1">{msg.timestamp}</div>
                </div>
              </div>
            ))}
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




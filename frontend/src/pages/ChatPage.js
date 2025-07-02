import PageLayout from "../components/PageLayout";
import { useState } from "react";
import { FormattedMessage } from "react-intl";
import { useIntl } from "react-intl";

const mockConversations = [
  {
    id: 1,
    name: "João Silva",
    avatar: "https://randomuser.me/api/portraits/men/32.jpg",
    lastMessage: "Até já!",
    lastMessageTime: "10:31",
    unread: 2,
    messages: [
      { id: 1, text: "Olá!", sentByMe: false, timestamp: "10:30" },
      { id: 2, text: "Olá João!", sentByMe: true, timestamp: "10:30" },
      { id: 3, text: "Até já!", sentByMe: false, timestamp: "10:31" },
    ],
  },
  {
    id: 2,
    name: "Maria Santos",
    avatar: "https://randomuser.me/api/portraits/women/44.jpg",
    lastMessage: "Falamos logo!",
    lastMessageTime: "09:17",
    unread: 0,
    messages: [
      { id: 1, text: "Bom dia, Maria!", sentByMe: true, timestamp: "09:16" },
      { id: 2, text: "Falamos logo!", sentByMe: false, timestamp: "09:17" },
    ],
  },
];

export default function ChatPage() {
  const [selectedConvId, setSelectedConvId] = useState(mockConversations[0].id);
  const conversation = mockConversations.find(c => c.id === selectedConvId);
  const intl = useIntl();

  return (
    <PageLayout       
    title={<FormattedMessage id="chat.title" defaultMessage="Mensagens" />}
      subtitle={<FormattedMessage id="chat.subtitle" defaultMessage="O teu histórico de conversas" />}>
      <div className="flex w-full max-w-6xl h-[500px] mx-auto bg-white rounded-2xl shadow overflow-hidden">
        {/* Sidebar */}
        <aside className="w-64 bg-gray-100 border-r flex flex-col">
          <div className="flex-1 overflow-y-auto">
            {mockConversations.map(conv => (
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
                  <div className="text-xs text-gray-500 truncate">{conv.lastMessage}</div>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <div className="text-[10px] text-gray-400">{conv.lastMessageTime}</div>
                  {conv.unread > 0 && (
                    <span className="bg-[#D41C1C] text-white rounded-full text-xs px-2">{conv.unread}</span>
                  )}
                </div>
              </button>
            ))}
          </div>
        </aside>

        {/* Chat principal */}
        <section className="flex-1 flex flex-col">
          {/* Header chat */}
          <div className="flex items-center gap-3 px-6 py-4 border-b">
            <img src={conversation.avatar} alt="" className="w-10 h-10 rounded-full border" />
            <div>
              <div className="font-semibold">{conversation.name}</div>
              <div className="text-xs text-gray-400">Online</div>
            </div>
          </div>
          {/* Mensagens */}
          <div className="flex-1 px-6 py-4 overflow-y-auto bg-gray-50 space-y-2">
            {conversation.messages.map(msg => (
              <div
                key={msg.id}
                className={`flex ${msg.sentByMe ? "justify-end" : "justify-start"}`}
              >
                <div className={`
                  px-4 py-2 rounded-2xl shadow
                  ${msg.sentByMe
                    ? "bg-[#D41C1C] text-white rounded-br-sm"
                    : "bg-white text-gray-800 rounded-bl-sm border"}
                  max-w-[70%]
                  `}
                >
                  <div className="text-sm">{msg.text}</div>
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
            />
            <button
              className="bg-[#D41C1C] text-white rounded-full w-10 h-10 flex items-center justify-center hover:bg-red-700 transition"
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


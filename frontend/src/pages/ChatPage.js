/**
 * ChatPage
 * Main chat interface: renders the conversation sidebar, search, and the chat window.
 * Integrates with Zustand chatStore for real-time messages and presence.
 * Sidebar always reflects the real-time online status of contacts.
 */

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
import { useNotificationStore } from "../stores/notificationStore";

export default function ChatPage() {
  // UI states
  const [input, setInput] = useState("");
  const [error, setError] = useState(null);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [search, setSearch] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [tempContact, setTempContact] = useState(null);

  const intl = useIntl();
  const { user } = userStore();
  const navigate = useNavigate();

  // Zustand chat store
  const addMessage = useChatStore((s) => s.addMessage);
  const setActiveConversation = useChatStore((s) => s.setActiveConversation);
  const setMessagesForConversation = useChatStore((s) => s.setMessagesForConversation);
  const activeConversationId = useChatStore((s) => s.activeConversationId);
  const messagesByConversation = useChatStore((s) => s.messagesByConversation);
  const contacts = useChatStore((s) => s.contacts); // Always up-to-date with real-time status!
  const setContacts = useChatStore((s) => s.setContacts);

  const messages = messagesByConversation[activeConversationId] || [];
  const sessionToken = sessionStorage.getItem("authToken");

  const resetMessageCount = useNotificationStore((s) => s.resetCount);

  // Router state for deep linking to a conversation
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const userIdFromQuery = searchParams.get("user");

  // 1. Load sidebar conversations (contacts) at mount or when query changes
  useEffect(() => {
    messageAPI.chatSidebarConversations()
      .then(convs => {
        setContacts(convs || []);
        // Set active conversation from URL or default to first contact
        const current = useChatStore.getState().activeConversationId;
        if (userIdFromQuery && current !== userIdFromQuery) {
          useChatStore.getState().setActiveConversation(userIdFromQuery);
        } else if (!userIdFromQuery && convs && convs.length > 0 && current !== convs[0].otherUserId) {
          useChatStore.getState().setActiveConversation(convs[0].otherUserId);
        } else if (!userIdFromQuery && (!convs || convs.length === 0) && current !== null) {
          useChatStore.getState().setActiveConversation(null);
        }
      })
      .catch(() => setError("Failed to load conversations"));
  }, [userIdFromQuery, setContacts]);

  // 2. When active conversation changes, load messages if not in store
  const messagesExist = (messagesByConversation[activeConversationId] || []).length > 0;

  useEffect(() => {
    if (!activeConversationId) return;
    setLoadingMessages(true);
    setError(null);

    // Mark messages as read
    messageAPI.markMessagesAsRead(activeConversationId)
      .then(() => {
        notificationAPI.markAllMessageNotificationsAsRead();
        resetMessageCount("MESSAGE");
      })
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

  /**
   * The currently selected contact (from contacts state), or a tempContact for new conversations.
   * This object is always real-time thanks to contacts coming from the store.
   */
  const selectedContact = contacts.find(c => c.otherUserId === activeConversationId);
  const contactInfo = selectedContact || tempContact;

  // 3. Fetch tempContact if this is a new conversation (user not in contacts yet)
  useEffect(() => {
    if (
      activeConversationId &&
      !contacts.some(c => c.otherUserId === activeConversationId)
    ) {
      profileAPI.getProfileById(activeConversationId, sessionToken)
        .then(data => {
          setTempContact({
            otherUserId: data.id,
            otherUserName: data.name || `${data.firstName || ""} ${data.lastName || ""}`.trim(),
            otherUserAvatar: data.photograph,
            role: data.role,
            online: data.online
          });
        })
        .catch(() => setTempContact(null));
    } else {
      setTempContact(null);
    }
  }, [activeConversationId, contacts, sessionToken]);

  /**
   * Handler to send a message using WebSocket and update the store optimistically.
   */
  const sendMessage = useChatStore((s) => s.sendMessage);

  const handleSend = () => {
    if (!input.trim() || !activeConversationId) return;
    const msg = {
      senderId: user.id,
      receiverId: activeConversationId,
      content: input,
    };
    sendMessage(msg); // WebSocket send (real-time)
    addMessage(msg);  // Optimistic local update
    setInput("");
    // Optionally: update last message in contacts store here as well (or rely on real-time push)
  };

  // Auto-scroll to the last message
  const messagesEndRef = useRef(null);
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollTop = messagesEndRef.current.scrollHeight;
    }
  }, [messages]);

  /**
   * Handler for user search (starts a new conversation with a user not in contacts).
   */
  const handleSearch = async (e) => {
    const value = e.target.value;
    setSearch(value);
    if (!value.trim()) {
      setSearchResults([]);
      return;
    }
    setSearchLoading(true);
    try {
      const results = await profileAPI.searchProfiles(value, sessionToken);
      const filtered = results.filter(
        (u) =>
          u.userId !== user.id &&
          !contacts.some((c) => c.otherUserId === u.userId)
      );
      setSearchResults(filtered);
    } catch (err) {
      setSearchResults([]);
    } finally {
      setSearchLoading(false);
    }
  };

  return (
    <PageLayout
      title={<FormattedMessage id="chat.title" defaultMessage="Messages" />}
      subtitle={<FormattedMessage id="chat.subtitle" defaultMessage="Your conversation history" />}
    >
      <div className="flex w-full max-w-6xl h-[500px] mx-auto bg-white rounded-2xl shadow overflow-hidden">
        {/* Sidebar: conversation list */}
        <aside className="w-64 bg-gray-100 border-r flex flex-col">
          <div className="p-3 border-b font-semibold text-lg">
            <FormattedMessage id="chat.conversations" defaultMessage="Conversations" />
          </div>

          {/* User search */}
          <div className="p-2 relative">
            <input
              className="w-full rounded px-2 py-1 border text-sm"
              placeholder={intl.formatMessage({ id: "chat.search", defaultMessage: "Search user..." })}
              value={search}
              onChange={handleSearch}
              autoComplete="off"
            />
            {search && (
              <div className="absolute left-0 right-0 bg-white shadow rounded max-h-40 overflow-y-auto mt-1 z-10">
                {searchLoading && <div className="px-3 py-2 text-gray-400">Searching...</div>}
                {!searchLoading && searchResults.length === 0 && (
                  <div className="px-3 py-2 text-gray-400">No results</div>
                )}
                {!searchLoading && searchResults.map((u) => (
                  <button
                    key={u.id}
                    className="flex items-center gap-2 w-full px-3 py-2 hover:bg-gray-100"
                    onClick={() => {
                      setActiveConversation(u.userId);
                      setSearch("");
                      setSearchResults([]);
                      setTempContact({
                        otherUserId: u.userId,
                        otherUserName: `${u.firstName || ""} ${u.lastName || ""}`.trim(),
                        otherUserAvatar: u.photograph,
                      });
                    }}
                  >
                    <img
                      src={u.photograph ? profileAPI.getPhoto(u.photograph) : profileIcon}
                      alt={`${u.firstName} ${u.lastName}`}
                      className="w-7 h-7 rounded-full object-cover border"
                    />
                    <span className="truncate">{`${u.firstName || ""} ${u.lastName || ""}`}</span>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Conversation list (always in sync with chatStore for real-time status) */}
          <div className="flex-1 overflow-y-auto">
            {contacts.length === 0 && (
              <div className="text-center text-gray-400 py-4">No conversations yet...</div>
            )}
            {contacts.map(conv => (
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

        {/* Main chat window */}
        <section className="flex-1 flex flex-col">
          {/* Header */}
          <div className="flex items-center gap-3 px-6 py-4 border-b">
            {(contactInfo || tempContact) && (
              <>
                <img
                  src={
                    (contactInfo?.otherUserAvatar || tempContact?.otherUserAvatar)
                      ? profileAPI.getPhoto(contactInfo?.otherUserAvatar || tempContact?.otherUserAvatar)
                      : profileIcon
                  }
                  alt={contactInfo?.otherUserName || tempContact?.otherUserName || ""}
                  className="w-10 h-10 rounded-full border object-cover"
                  style={{ display: "block" }}
                  onError={e => {
                    e.target.onerror = null;
                    e.target.src = profileIcon;
                  }}
                />
                <div>
                  <div className="font-semibold">{contactInfo?.otherUserName || tempContact?.otherUserName}</div>
                  <div className="text-xs text-gray-400">
                    {(contactInfo?.online || tempContact?.online)
                      ? <FormattedMessage id="chat.online" defaultMessage="Online" />
                      : (contactInfo?.role || tempContact?.role)
                        ? ((contactInfo?.role || tempContact?.role).charAt(0).toUpperCase() +
                            (contactInfo?.role || tempContact?.role).slice(1).toLowerCase())
                        : ""}
                  </div>
                </div>
              </>
            )}
          </div>

          {/* Messages */}
          <div
            ref={messagesEndRef}
            className="flex-1 px-6 py-4 overflow-y-auto bg-gray-50 space-y-2"
          >
            {loadingMessages && <div className="text-center text-gray-400">Loading...</div>}
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
              placeholder={intl.formatMessage({ id: "chat.placeholder", defaultMessage: "Type a message..." })}
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === "Enter" && handleSend()}
            />
            <button
              className="bg-[#D41C1C] text-white rounded-full w-10 h-10 flex items-center justify-center hover:bg-red-700 transition"
              onClick={handleSend}
              disabled={!input.trim()}
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" strokeLinejoin="round" className="w-6 h-6">
                <path d="M22 2L11 13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
            </button>
          </div>
        </section>
      </div>
    </PageLayout>
  );
}


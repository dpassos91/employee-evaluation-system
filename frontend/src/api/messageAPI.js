import { apiConfig } from './apiConfig.js';

const { apiCall, API_ENDPOINTS } = apiConfig;

/**
 * Fetches the list of sidebar conversations (summaries) for the authenticated user.
 * @returns {Promise<Array>} List of ConversationDto
 */
const chatSidebarConversations = async () => {
  return apiCall(API_ENDPOINTS.messages.chatSidebarConversations, { 
    method: "GET" 
  });
};

/**
 * Fetches the conversation (message history) between the authenticated user and another user.
 * @async
 * @function getConversation
 * @param {number} otherUserId - The ID of the other user in the conversation.
 * @returns {Promise<Array>} The list of message DTOs exchanged with the user.
 *
 * @example
 * const messages = await messageAPI.getConversation(5);
 */
const getConversation = async (otherUserId) => {
  return apiCall(API_ENDPOINTS.messages.getConversation(otherUserId), {
    method: "GET"
  });
};

/**
 * Sends a new message from the authenticated user to another user.
 * The senderId is automatically set in the backend.
 * 
 * @async
 * @function sendMessage
 * @param {Object} messageDto - The message data (should include at least receiverId and content).
 * @param {number} messageDto.receiverId - The ID of the recipient user.
 * @param {string} messageDto.content - The message text.
 * @returns {Promise<Object>} The API response from the backend.
 *
 * @example
 * await messageAPI.sendMessage({ receiverId: 5, content: "Hello!" });
 */
const sendMessage = async (messageDto) => {
  return apiCall(API_ENDPOINTS.messages.send, {
    method: "POST",
    body: JSON.stringify(messageDto)
  });
};

/**
 * Marks all messages as read from a specific user (otherUserId) to the authenticated user.
 * 
 * @async
 * @function markMessagesAsRead
 * @param {number} otherUserId - The ID of the user whose messages are to be marked as read.
 * @returns {Promise<Object>} The API response, typically with the number of messages updated.
 *
 * @example
 * await messageAPI.markMessagesAsRead(5);
 */
const markMessagesAsRead = async (otherUserId) => {
  return apiCall(API_ENDPOINTS.messages.markAsRead(otherUserId), {
    method: "PUT"
  });
};

/**
 * API for message-related operations (conversations, sending, marking as read).
 */
export const messageAPI = {
  getConversation,
  sendMessage,
  markMessagesAsRead,
  chatSidebarConversations
};

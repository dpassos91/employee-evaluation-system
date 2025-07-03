package aor.projetofinal.websocket;

import aor.projetofinal.bean.MessageBean;
import aor.projetofinal.bean.NotificationBean;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

import aor.projetofinal.util.OnlineUserTracker;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for real-time chat functionality.
 * Handles authentication via session token (passed as query param),
 * message persistence, real-time delivery, and basic notification creation.
 * All logs use the RequestContext for author and IP consistency.
 */
@Singleton
@ServerEndpoint(
    value = "/websocket/chat",
    configurator = aor.projetofinal.websocket.ChatEndpointConfigurator.class
)
public class ChatEndpoint {

    private static final Logger logger = LogManager.getLogger(ChatEndpoint.class);

    @Inject
    MessageBean messageBean;

    @Inject
    NotificationBean notificationBean;

    private static final Map<Integer, Session> sessions = new ConcurrentHashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper();

        /**
     * Called when a WebSocket connection is closed.
     * Removes the session from the active sessions map.
     * Sets RequestContext for logging.
     *
     * @param session the WebSocket session that was closed
     */
    @OnClose
    public void onClose(Session session) {
        logger.info("[ONCLOSE] Antes de remover, sessions.keySet: {}", sessions.keySet());
        try {
            sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
            Integer userId = (Integer) session.getUserProperties().get("userId");
            if (userId != null) {
                OnlineUserTracker.markOffline(userId);
            }
            String author = (String) session.getUserProperties().getOrDefault("author", "Anonymous");
            String ip = (String) session.getUserProperties().getOrDefault("ip", "Unknown");
            RequestContext.setAuthor(author);
            RequestContext.setIp(ip);

            logger.info("User: {} | IP: {} - WebSocket connection closed.", RequestContext.getAuthor(), RequestContext.getIp());
        } finally {
            RequestContext.clear();
        }
    }

    /**
 * Called when a WebSocket error occurs.
 * Removes the user from the online tracker and logs the error.
 * Sets the RequestContext for logging consistency.
 *
 * @param session   the WebSocket session where the error occurred
 * @param throwable the exception that was thrown
 */
@OnError
public void onError(Session session, Throwable throwable) {
    try {
        Integer userId = (Integer) session.getUserProperties().get("userId");
        if (userId != null) {
            OnlineUserTracker.markOffline(userId);
        }
        String author = (String) session.getUserProperties().getOrDefault("author", "Anonymous");
        String ip = (String) session.getUserProperties().getOrDefault("ip", "Unknown");
        RequestContext.setAuthor(author);
        RequestContext.setIp(ip);

        logger.error(
            "User: {} | IP: {} - WebSocket error occurred: {}",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            throwable
        );
    } finally {
        RequestContext.clear();
    }
}

    /**
     * Handles incoming messages from the client.
     * Expects a JSON-formatted MessageDto, validates and persists it,
     * delivers it in real time to the receiver if online, and notifies both users.
     * Sets RequestContext for logging.
     *
     * @param session the session from which the message was received
     * @param messageText the raw JSON string representing the message
     */
    @OnMessage
public void onMessage(Session session, String messageText) {

        if (messageText.contains("\"type\":\"ping\"") || "ping".equalsIgnoreCase(messageText.trim())) {
        return;
        }

    // Set context for this thread/message
    String author = (String) session.getUserProperties().getOrDefault("author", "Anonymous");
    String ip = (String) session.getUserProperties().getOrDefault("ip", "Unknown");
    RequestContext.setAuthor(author);
    RequestContext.setIp(ip);

    Integer senderId = null;
    Integer receiverId = null;
    try {
        MessageDto dto = mapper.readValue(messageText, MessageDto.class);

        senderId = dto.getSenderId();
        receiverId = dto.getReceiverId();
        String content = dto.getContent();

        if (senderId == null || receiverId == null || content == null || content.isBlank()) {
            logger.warn("User: {} | IP: {} - Invalid message format: required fields missing. Raw message: {}", RequestContext.getAuthor(), RequestContext.getIp(), messageText);
            session.getBasicRemote().sendText("❌ Invalid message format: required fields missing.");
            return;
        }

        // Persist the message using business logic
        boolean saved = messageBean.saveMessage(dto);

        if (!saved) {
            logger.error("User: {} | IP: {} - Message could not be saved. Validation or DB error. Raw message: {}", RequestContext.getAuthor(), RequestContext.getIp(), messageText);
            session.getBasicRemote().sendText("❌ Message could not be saved.");
            return;
        }

        // Seeks a message just created (the last one exchanged between sender and receiver)
        MessageDto enrichedDto = null;
        try {
            // Assures that getConversation returns messages in chronological order
            // Last message is the most recent one exchanged
            List<MessageDto> conversation = messageBean.getConversation(senderId, receiverId);
            if (conversation != null && !conversation.isEmpty()) {
                enrichedDto = conversation.get(conversation.size() - 1);
            }
        } catch (Exception ex) {
            logger.error("User: {} | IP: {} - Failed to fetch enriched message after save. {}", RequestContext.getAuthor(), RequestContext.getIp(), ex.getMessage());
        }

        if (enrichedDto == null) {
            session.getBasicRemote().sendText("❌ Could not fetch saved message from DB.");
            return;
        }

        // Create a notification for the receiver
        String notificationText = "New message from " + enrichedDto.getSenderName();
        notificationBean.createNotification(receiverId, "MESSAGE", notificationText);

        // If receiver is online, send them the message instantly
        Session receiverSession = sessions.get(receiverId);
        logger.info("[ONMESSAGE] receiverSession == null? {}", receiverSession == null);
        logger.info("[ONMESSAGE] receiverSession != null && isOpen? {}", receiverSession != null && receiverSession.isOpen());
        if (receiverSession != null && receiverSession.isOpen()) {
            String jsonMsg = mapper.writeValueAsString(Map.of(
                "type", "chat_message",
                "senderId", enrichedDto.getSenderId(),
                "senderName", enrichedDto.getSenderName(),
                "content", enrichedDto.getContent(),
                "createdAt", enrichedDto.getCreatedAt(),
                "receiverId", enrichedDto.getReceiverId(),
                "id", enrichedDto.getId()
            ));

        // Always send the message back to the sender
        session.getBasicRemote().sendText(jsonMsg);
        
            logger.info("Tentar enviar mensagem para receiverId = " + receiverId + ", sessions: " + sessions.keySet());
            receiverSession.getBasicRemote().sendText(jsonMsg);
            logger.info("User: {} | IP: {} - Message delivered in real time to userId: {}.", RequestContext.getAuthor(), RequestContext.getIp(), receiverId);
        } else {
            logger.info("User: {} | IP: {} - Receiver userId {} is offline. Message will be shown when they come online.", RequestContext.getAuthor(), RequestContext.getIp(), receiverId);
        }

        // Confirm delivery to the sender 
        logger.info("[DEBUG] Vou enviar para receiverId {}, sessions atuais: {}", receiverId, sessions.keySet());
        logger.info("[DEBUG] Session receiverId aberta? {}", receiverSession.isOpen());
        session.getBasicRemote().sendText("✔️ Message sent to userId: " + receiverId);

    } catch (Exception e) {
        logger.error("User: {} | IP: {} - Error processing message: {}. Exception: {}", RequestContext.getAuthor(), RequestContext.getIp(), messageText, e.getMessage());
        try {
            session.getBasicRemote().sendText("❌ Error processing your message.");
        } catch (IOException ignored) {}
    } finally {
        RequestContext.clear();
    }

}


    /**
     * Called when a new WebSocket connection is established.
     * Authenticates the user and associates their userId with the session.
     * Sets RequestContext for logging.
     *
     * @param session the WebSocket session
     * @param config the endpoint configuration, used to obtain userId, author and IP
     */
    @OnOpen
public void onOpen(Session session, EndpointConfig config) {
    logger.info("DEBUG: session.getUserProperties() = {}", session.getUserProperties());
    logger.info("DEBUG: config.getUserProperties() = {}", config.getUserProperties());

    try {
        Integer userId = (Integer) session.getUserProperties().get("userId");
        String author = (String) session.getUserProperties().getOrDefault("author", "Anonymous");
        String ip = (String) session.getUserProperties().getOrDefault("ip", "Unknown");

        OnlineUserTracker.markOnline(userId);

        RequestContext.setAuthor(author);
        RequestContext.setIp(ip);

    logger.info("[ONOPEN] userId: {}", userId);
    logger.info("[ONOPEN] sessions.keySet antes: {}", sessions.keySet());

        if (userId == null) {
            logger.warn("User: {} | IP: {} - WebSocket connection rejected: Invalid or missing session token.", RequestContext.getAuthor(), RequestContext.getIp());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication failed"));
            } catch (IOException ignored) {}
            return;
        }
        sessions.put(userId, session);
        logger.info("Sessões WebSocket ativas: " + sessions.keySet());
        logger.info("[ONOPEN] sessions.keySet depois: {}", sessions.keySet());
        logger.info("User: {} | IP: {} - WebSocket connection established.", RequestContext.getAuthor(), RequestContext.getIp());
    } finally {
        RequestContext.clear();
    }
}

}

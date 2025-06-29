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
    configurator = ChatEndpointConfigurator.class
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
     * Called when a new WebSocket connection is established.
     * Authenticates the user and associates their userId with the session.
     * Sets RequestContext for logging.
     *
     * @param session the WebSocket session
     * @param config the endpoint configuration, used to obtain userId, author and IP
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        try {
            Integer userId = (Integer) config.getUserProperties().get("userId");
            String author = (String) config.getUserProperties().getOrDefault("author", "Anonymous");
            String ip = (String) config.getUserProperties().getOrDefault("ip", "Unknown");
            // Store context on the session for future messages
            session.getUserProperties().put("userId", userId);
            session.getUserProperties().put("author", author);
            session.getUserProperties().put("ip", ip);

            RequestContext.setAuthor(author);
            RequestContext.setIp(ip);

            if (userId == null) {
                logger.warn("User: {} | IP: {} - WebSocket connection rejected: Invalid or missing session token.", RequestContext.getAuthor(), RequestContext.getIp());
                try {
                    session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication failed"));
                } catch (IOException ignored) {}
                return;
            }
            sessions.put(userId, session);
            logger.info("User: {} | IP: {} - WebSocket connection established.", RequestContext.getAuthor(), RequestContext.getIp());
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * Removes the session from the active sessions map.
     * Sets RequestContext for logging.
     *
     * @param session the WebSocket session that was closed
     */
    @OnClose
    public void onClose(Session session) {
        try {
            sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
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
        // Set context for this thread/message
        String author = (String) session.getUserProperties().getOrDefault("author", "Anonymous");
        String ip = (String) session.getUserProperties().getOrDefault("ip", "Unknown");
        RequestContext.setAuthor(author);
        RequestContext.setIp(ip);

        Integer senderId = null;
        try {
            MessageDto dto = mapper.readValue(messageText, MessageDto.class);

            senderId = dto.getSenderId();
            Integer receiverId = dto.getReceiverId();
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

            // Create a notification for the receiver
            String notificationText = "New message from " + dto.getSenderName();
            notificationBean.createNotification(receiverId, "mensagem", notificationText);

            // If receiver is online, send them the message instantly
            Session receiverSession = sessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                String jsonMsg = mapper.writeValueAsString(Map.of(
                        "type", "chat_message",
                        "senderId", senderId,
                        "senderName", dto.getSenderName(),
                        "content", content,
                        "createdAt", dto.getCreatedAt()
                ));
                receiverSession.getBasicRemote().sendText(jsonMsg);
                logger.info("User: {} | IP: {} - Message delivered in real time to userId: {}.", RequestContext.getAuthor(), RequestContext.getIp(), receiverId);
            } else {
                logger.info("User: {} | IP: {} - Receiver userId {} is offline. Message will be shown when they come online.", RequestContext.getAuthor(), RequestContext.getIp(), receiverId);
            }

            // Confirm delivery to the sender
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
}

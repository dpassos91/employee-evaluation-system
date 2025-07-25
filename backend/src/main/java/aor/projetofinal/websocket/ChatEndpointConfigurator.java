package aor.projetofinal.websocket;

import aor.projetofinal.bean.UserBean;
import aor.projetofinal.entity.UserEntity;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * WebSocket configurator to extract and validate the session token sent as a query parameter
 * during the WebSocket handshake. Also extracts the client's IP address and user name for logging.
 * These are saved into the UserProperties map for use by the ChatEndpoint and RequestContext.
 */
public class ChatEndpointConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger logger = LogManager.getLogger(ChatEndpointConfigurator.class);

    @Override
    public void modifyHandshake(
        ServerEndpointConfig sec,
        HandshakeRequest request,
        HandshakeResponse response) {
    logger.info("HANDSHAKE CONFIGURATOR CALLED!");
    String token = null;
    List<String> tokenParams = request.getParameterMap().get("token");
    if (tokenParams != null && !tokenParams.isEmpty()) {
        token = tokenParams.get(0);
    }
    logger.info("WebSocket handshake - Token from query param: {}", token);

    Integer userId = null;
    String author = "Anonymous";
    if (token != null && !token.isBlank()) {
        try {
            UserBean userBean = CDI.current().select(UserBean.class).get();
            userId = userBean.findUserIdBySessionToken(token);

            if (userId != null) {
                UserEntity userEntity = userBean.findUserBySessionToken(token);
                if (userEntity != null && userEntity.getEmail() != null) {
                    author = userEntity.getEmail();
                }
            }
            logger.info("WebSocket handshake - Session token received: {} → userId: {}", token, userId);
        } catch (Exception e) {
            logger.error("WebSocket handshake - Error validating session token: {}", e.getMessage());
        }
    } else {
        logger.warn("WebSocket handshake - No session token found in WebSocket request.");
    }

    // Loga se userId não foi encontrado (expira/é inválido)
    if (userId == null) {
        logger.warn("WebSocket handshake - Authentication failed: Invalid or expired token: {}", token);
    }

    // Extract client IP address
    String clientIp = "Unknown";
    if (request != null && request.getHeaders() != null) {
        List<String> xff = request.getHeaders().get("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            clientIp = xff.get(0);
        } else if (request.getHeaders().get("X-Real-IP") != null) {
            clientIp = request.getHeaders().get("X-Real-IP").get(0);
        }
    }
    logger.info("WebSocket handshake - Client IP: {}", clientIp);

    // Store userId, author, and IP for later use in the session
if (userId != null) {
    sec.getUserProperties().put("userId", userId);
    sec.getUserProperties().put("author", author);
    sec.getUserProperties().put("ip", clientIp);
    logger.info("Handshake - userId, author, and IP saved to session: {} {} {}", userId, author, clientIp);
} else {
    logger.warn("Handshake - userId is null: not saving to UserProperties (token invalid/expired?)");
}
}
}


package aor.projetofinal.service;

import aor.projetofinal.bean.MessageBean;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.MessageDto;
import aor.projetofinal.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import aor.projetofinal.context.RequestContext;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageService {

    private static final Logger logger = LogManager.getLogger(MessageService.class);

    @Inject
    private MessageBean messageBean;

    @Inject
    private UserDao userDao;

    /**
     * Gets all messages in a conversation between the authenticated user and another user.
     * @param otherUserId The ID of the other user
     * @param token       The authentication token (from header)
     * @return List of MessageDto or error
     */
    @GET
    @Path("/with/{otherUserId}")
    public Response getConversation(
            @PathParam("otherUserId") int otherUserId,
            @HeaderParam("token") String token,
            @Context Request request
    ) {
        // Example: get author/IP from RequestContext
        String author = RequestContext.getAuthor();
        String ip = RequestContext.getIp();

        UserEntity currentUser = authenticateUserByToken(token);
        if (currentUser == null) {
            logger.warn("User: {} | IP: {} - Unauthorized access to conversation.", author, ip);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired token.").build();
        }

        List<MessageDto> conversation = messageBean.getConversation(currentUser.getId(), otherUserId);

        logger.info("User: {} | IP: {} - Fetched conversation with userId {}", author, ip, otherUserId);
        return Response.ok(conversation).build();
    }

    /**
     * Sends a new message.
     * @param messageDto The message to be sent
     * @param token      The authentication token (from header)
     * @return Status code
     */
    @POST
    public Response sendMessage(
            MessageDto messageDto,
            @HeaderParam("token") String token,
            @Context Request request
    ) {
        String author = RequestContext.getAuthor();
        String ip = RequestContext.getIp();

        UserEntity currentUser = authenticateUserByToken(token);
        if (currentUser == null) {
            logger.warn("User: {} | IP: {} - Unauthorized message send attempt.", author, ip);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired token.").build();
        }

        // For security, override senderId with the current user
        messageDto.setSenderId(currentUser.getId());

        boolean saved = messageBean.saveMessage(messageDto);
        if (!saved) {
            logger.warn("User: {} | IP: {} - Failed to save message.", author, ip);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Message could not be saved.").build();
        }
        logger.info("User: {} | IP: {} - Sent message to userId {}", author, ip, messageDto.getReceiverId());
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Marks all messages as read from a specific user to the authenticated user.
     * @param otherUserId The sender's user ID (the other party)
     * @param token       The authentication token (from header)
     * @return Number of messages updated
     */
    @PUT
    @Path("/read-from/{otherUserId}")
    public Response markMessagesAsRead(
            @PathParam("otherUserId") int otherUserId,
            @HeaderParam("token") String token,
            @Context Request request
    ) {
        String author = RequestContext.getAuthor();
        String ip = RequestContext.getIp();

        UserEntity currentUser = authenticateUserByToken(token);
        if (currentUser == null) {
            logger.warn("User: {} | IP: {} - Unauthorized attempt to mark messages as read.", author, ip);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid or expired token.").build();
        }

        int updated = messageBean.markMessagesAsRead(otherUserId, currentUser.getId());
        logger.info("User: {} | IP: {} - Marked {} messages as read from userId {}", author, ip, updated, otherUserId);
        return Response.ok("Messages marked as read: " + updated).build();
    }

    /**
     * Helper method to get the user from the token.
     * @param token Auth token
     * @return UserEntity if valid, null otherwise
     */
private UserEntity authenticateUserByToken(String token) {
    if (token == null || token.isBlank()) {
        return null;
    }
    return userDao.findBySessionToken(token);
}
}


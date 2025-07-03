package aor.projetofinal.service;

import aor.projetofinal.bean.MessageBean;
import aor.projetofinal.dto.MessageDto;
import aor.projetofinal.entity.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dto.ConversationDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MessageService {

    private static final Logger logger = LogManager.getLogger(MessageService.class);

    @Inject
    private MessageBean messageBean;

    /**
 * Gets the list of conversations for the authenticated user.
 * Each conversation contains information about the other user,
 * the last message exchanged, its timestamp, and the number of unread messages.
 *
 * Example response:
 * [
 *   {
 *     "otherUserId": 2,
 *     "otherUserName": "João Silva",
 *     "otherUserAvatar": "https://randomuser.me/api/portraits/men/32.jpg",
 *     "lastMessage": "Até já!",
 *     "lastMessageTime": "2025-07-03T10:31:00Z",
 *     "unreadCount": 1
 *   },
 *   ...
 * ]
 *
 * @return 200 OK with a list of ConversationDto objects (one for each conversation)
 */
@GET
@Path("/conversations")
public Response getConversations() {
    UserEntity currentUser = RequestContext.getCurrentUser();
    List<ConversationDto> conversations = messageBean.getUserConversations(currentUser.getId());
    return Response.ok(conversations).build();
}

    /**
     * Gets all messages in a conversation between the authenticated user and another user.
     * @param otherUserId The ID of the other user
     * @return List of MessageDto
     */
    @GET
    @Path("/with/{otherUserId}")
    public Response getConversation(@PathParam("otherUserId") int otherUserId) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        List<MessageDto> conversation = messageBean.getConversation(currentUser.getId(), otherUserId);

        logger.info("User: {} | IP: {} - Fetched conversation with userId {}.",
                currentUser.getEmail(),
                RequestContext.getIp(),
                otherUserId);

        return Response.ok(conversation).build();
    }

    /**
     * Sends a new message.
     * @param messageDto The message to be sent
     * @return Status code
     */
    @POST
    public Response sendMessage(MessageDto messageDto) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        // For security, override senderId with the current user
        messageDto.setSenderId(currentUser.getId());

        boolean saved = messageBean.saveMessage(messageDto);
        if (!saved) {
            logger.warn("User: {} | IP: {} - Failed to save message.",
                    currentUser.getEmail(),
                    RequestContext.getIp());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Message could not be saved.").build();
        }
        logger.info("User: {} | IP: {} - Sent message to userId {}.",
                currentUser.getEmail(),
                RequestContext.getIp(),
                messageDto.getReceiverId());
        return Response.status(Response.Status.CREATED).build();
    }

    /**
     * Marks all messages as read from a specific user to the authenticated user.
     * @param otherUserId The sender's user ID (the other party)
     * @return Number of messages updated
     */
    @PUT
    @Path("/read-from/{otherUserId}")
    public Response markMessagesAsRead(@PathParam("otherUserId") int otherUserId) {
        UserEntity currentUser = RequestContext.getCurrentUser();

        int updated = messageBean.markMessagesAsRead(otherUserId, currentUser.getId());
        logger.info("User: {} | IP: {} - Marked {} messages as read from userId {}.",
                currentUser.getEmail(),
                RequestContext.getIp(),
                updated,
                otherUserId);

        return Response.ok("Messages marked as read: " + updated).build();
    }
}



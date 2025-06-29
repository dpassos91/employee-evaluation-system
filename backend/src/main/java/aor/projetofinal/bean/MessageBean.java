package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.MessageDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.MessageDto;
import aor.projetofinal.entity.MessageEntity;
import aor.projetofinal.entity.UserEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class MessageBean {

    private static final Logger logger = LogManager.getLogger(ProfileBean.class);

    @Inject
    private MessageDao messageDao;

    @Inject
    private UserDao userDao; // Assumes you have UserDao to fetch UserEntity by id

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Saves a new message after validating sender and receiver.
     * @param dto MessageDto with all fields
     * @return true if saved, false otherwise
     */
    public boolean saveMessage(MessageDto dto) {
        if (dto == null) {
            logger.warn("User: {} | IP: {} - Attempted to save null MessageDto. Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return false;
        }

        UserEntity sender = userDao.findById(dto.getSenderId());
        UserEntity receiver = userDao.findById(dto.getReceiverId());

        if (sender == null || receiver == null) {
            logger.warn("User: {} | IP: {} - Invalid sender ({}) or receiver ({}). Message not saved.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                dto.getSenderId(),
                dto.getReceiverId()
            );
            return false;
        }

        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(dto.getContent());
        message.setRead(false);

        messageDao.save(message);
        logger.info("User: {} | IP: {} - Message persisted. SenderId: {}, ReceiverId: {}",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            sender.getId(),
            receiver.getId()
        );
        return true;
    }

    /**
     * Gets the conversation (all messages) between two users, ordered by date.
     * @param user1Id First user's ID
     * @param user2Id Second user's ID
     * @return List of MessageDto
     */
    public List<MessageDto> getConversation(int user1Id, int user2Id) {
        UserEntity user1 = userDao.findById(user1Id);
        UserEntity user2 = userDao.findById(user2Id);

        if (user1 == null || user2 == null) {
            logger.warn("User: {} | IP: {} - Invalid users in getConversation(). Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return List.of();
        }

        List<MessageEntity> entities = messageDao.findConversation(user1, user2);
        List<MessageDto> dtos = new ArrayList<>();
        for (MessageEntity entity : entities) {
            dtos.add(toDto(entity));
        }
        return dtos;
    }

    /**
     * Marks all messages as read from one user to another.
     * @param senderId Sender's ID
     * @param receiverId Receiver's ID
     * @return Number of messages updated
     */
    public int markMessagesAsRead(int senderId, int receiverId) {
        UserEntity sender = userDao.findById(senderId);
        UserEntity receiver = userDao.findById(receiverId);

        if (sender == null || receiver == null) {
            logger.warn("User: {} | IP: {} - Invalid sender ({}) or receiver ({}). Cannot mark as read.",
                RequestContext.getAuthor(),
                RequestContext.getIp(),
                senderId,
                receiverId
            );
            return 0;
        }

        int updated = messageDao.markMessagesAsRead(sender, receiver);
        logger.info("User: {} | IP: {} - {} messages marked as read (SenderId: {}, ReceiverId: {}).",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            updated,
            senderId,
            receiverId
        );
        return updated;
    }

    /**
     * Converts a MessageEntity to MessageDto, optionally enriching with user names.
     * @param entity The MessageEntity
     * @return MessageDto
     */
    public MessageDto toDto(MessageEntity entity) {
        if (entity == null) {
            logger.warn("User: {} | IP: {} - Attempted to convert null MessageEntity to DTO.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return null;
        }
        MessageDto dto = new MessageDto();
        dto.setId(entity.getId());
        dto.setSenderId(entity.getSender().getId());
        dto.setReceiverId(entity.getReceiver().getId());
        dto.setContent(entity.getContent());
        dto.setRead(entity.isRead());
        if (entity.getCreatedAt() != null) {
            dto.setCreatedAt(entity.getCreatedAt().format(FORMATTER));
        }
        // Optionally, enrich with names from ProfileEntity
        if (entity.getSender().getProfile() != null) {
            dto.setSenderName(entity.getSender().getProfile().getFirstName()
                    + " " + entity.getSender().getProfile().getLastName());
        }
        if (entity.getReceiver().getProfile() != null) {
            dto.setReceiverName(entity.getReceiver().getProfile().getFirstName()
                    + " " + entity.getReceiver().getProfile().getLastName());
        }
        return dto;
    }
}


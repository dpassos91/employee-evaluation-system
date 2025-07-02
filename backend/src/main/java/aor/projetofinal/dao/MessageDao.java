package aor.projetofinal.dao;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.entity.MessageEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import aor.projetofinal.bean.MessageBean;
import aor.projetofinal.context.RequestContext;

import java.util.List;

@Stateless
public class MessageDao {

    private static final Logger logger = LogManager.getLogger(MessageBean.class);

    @PersistenceContext(unitName = "grupo7")
    private EntityManager em;

    /**
 * Counts the number of unread messages sent from the contact (sender) to the authenticated user (receiver).
 *
 * @param contactId The sender's user ID.
 * @param userId The authenticated user's ID (receiver).
 * @return Number of unread messages from contactId to userId.
 */
public int countUnreadMessagesFrom(int contactId, int userId) {
    logger.info(
        "User: {} | IP: {} - Counting unread messages from userId {} to userId {}.",
        RequestContext.getAuthor(),
        RequestContext.getIp(),
        contactId,
        userId
    );

    Long count = em.createQuery(
        "SELECT COUNT(m) FROM MessageEntity m " +
        "WHERE m.sender.id = :contactId AND m.receiver.id = :userId AND m.read = false",
        Long.class
    )
    .setParameter("contactId", contactId)
    .setParameter("userId", userId)
    .getSingleResult();

    logger.info(
        "User: {} | IP: {} - Unread messages from userId {} to userId {}: {}.",
        RequestContext.getAuthor(),
        RequestContext.getIp(),
        contactId,
        userId,
        count
    );
    return count.intValue();
}

    /**
 * Finds all unique contacts (users) with whom the specified user has exchanged messages,
 * either as sender or receiver.
 *
 * @param userId The ID of the user whose contacts to find.
 * @return List of UserEntity representing the contacts.
 */
public List<UserEntity> findContactsForUser(int userId) {
    logger.info(
        "User: {} | IP: {} - Finding contacts for userId {}.",
        RequestContext.getAuthor(),
        RequestContext.getIp(),
        userId
    );

    List<UserEntity> contacts = em.createQuery(
        "SELECT DISTINCT u FROM UserEntity u " +
        "WHERE u.id <> :userId AND (" +
        "   u.id IN (SELECT m.sender.id FROM MessageEntity m WHERE m.receiver.id = :userId) " +
        "   OR u.id IN (SELECT m.receiver.id FROM MessageEntity m WHERE m.sender.id = :userId)" +
        ")",
        UserEntity.class
    )
    .setParameter("userId", userId)
    .getResultList();

    logger.info(
        "User: {} | IP: {} - Found {} contacts for userId {}.",
        RequestContext.getAuthor(),
        RequestContext.getIp(),
        contacts.size(),
        userId
    );
    return contacts;
}

/**
 * Finds the latest message exchanged between two users, regardless of direction.
 *
 * @param userId1 The first user's ID.
 * @param userId2 The second user's ID.
 * @return The most recent MessageEntity exchanged between the users, or null if none exists.
 */
public MessageEntity findLastMessageBetween(int userId1, int userId2) {
    logger.info(
        "User: {} | IP: {} - Finding last message between userId {} and userId {}.",
        RequestContext.getAuthor(),
        RequestContext.getIp(),
        userId1,
        userId2
    );

    List<MessageEntity> result = em.createQuery(
        "SELECT m FROM MessageEntity m " +
        "WHERE ((m.sender.id = :userId1 AND m.receiver.id = :userId2) " +
        "    OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)) " +
        "ORDER BY m.createdAt DESC",
        MessageEntity.class
    )
    .setParameter("userId1", userId1)
    .setParameter("userId2", userId2)
    .setMaxResults(1)
    .getResultList();

    if (!result.isEmpty()) {
        logger.info(
            "User: {} | IP: {} - Last message found: id {}.",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            result.get(0).getId()
        );
        return result.get(0);
    } else {
        logger.info(
            "User: {} | IP: {} - No messages found between userId {} and userId {}.",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            userId1,
            userId2
        );
        return null;
    }
}

    /**
     * Retrieves all messages exchanged between two users, ordered by creation date (ascending).
     * @param user1 First user
     * @param user2 Second user
     * @return List of MessageEntity
     */
    public List<MessageEntity> findConversation(UserEntity user1, UserEntity user2) {
        if (user1 == null || user2 == null) {
            logger.warn(
                "User: {} | IP: {} - One or both users are null in findConversation(). Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return List.of();
        }
        TypedQuery<MessageEntity> query = em.createQuery(
            "SELECT m FROM MessageEntity m " +
            "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
            "   OR (m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.createdAt ASC", MessageEntity.class);
        query.setParameter("user1", user1);
        query.setParameter("user2", user2);
        return query.getResultList();
    }

    /**
     * Retrieves all unread messages from sender to receiver, ordered by creation date (ascending).
     * @param sender   The sender user
     * @param receiver The receiver user
     * @return List of unread MessageEntity
     */
    public List<MessageEntity> findUnreadMessages(UserEntity sender, UserEntity receiver) {
        if (sender == null || receiver == null) {
            logger.warn(
                "User: {} | IP: {} - Sender or receiver is null in findUnreadMessages(). Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return List.of();
        }
        TypedQuery<MessageEntity> query = em.createQuery(
            "SELECT m FROM MessageEntity m " +
            "WHERE m.sender = :sender AND m.receiver = :receiver AND m.read = false " +
            "ORDER BY m.createdAt ASC", MessageEntity.class);
        query.setParameter("sender", sender);
        query.setParameter("receiver", receiver);
        return query.getResultList();
    }

    /**
     * Marks all unread messages from sender to receiver as read.
     * @param sender   The sender user
     * @param receiver The receiver user
     * @return Number of updated messages
     */
    public int markMessagesAsRead(UserEntity sender, UserEntity receiver) {
        if (sender == null || receiver == null) {
            logger.warn(
                "User: {} | IP: {} - Sender or receiver is null in markMessagesAsRead(). Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return 0;
        }
        int updated = em.createQuery(
            "UPDATE MessageEntity m SET m.read = true " +
            "WHERE m.sender = :sender AND m.receiver = :receiver AND m.read = false")
            .setParameter("sender", sender)
            .setParameter("receiver", receiver)
            .executeUpdate();
        logger.info(
            "User: {} | IP: {} - {} messages marked as read (SenderId: {}, ReceiverId: {}).",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            updated,
            sender.getId(),
            receiver.getId()
        );
        return updated;
    }

        /**
     * Persists a new message entity to the database.
     * @param message the MessageEntity to persist
     */
    public void save(MessageEntity message) {
        if (message == null) {
            logger.warn(
                "User: {} | IP: {} - Attempted to persist a null MessageEntity. Operation aborted.",
                RequestContext.getAuthor(),
                RequestContext.getIp()
            );
            return;
        }
        em.persist(message);
        logger.info(
            "User: {} | IP: {} - Message saved. SenderId: {}, ReceiverId: {}, Content: '{}'",
            RequestContext.getAuthor(),
            RequestContext.getIp(),
            message.getSender() != null ? message.getSender().getId() : null,
            message.getReceiver() != null ? message.getReceiver().getId() : null,
            message.getContent()
        );
    }

}


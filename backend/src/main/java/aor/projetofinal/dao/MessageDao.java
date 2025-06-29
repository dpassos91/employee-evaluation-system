package aor.projetofinal.dao;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import aor.projetofinal.entity.MessageEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import aor.projetofinal.bean.ProfileBean;
import aor.projetofinal.context.RequestContext;

import java.util.List;

@Stateless
public class MessageDao {

    private static final Logger logger = LogManager.getLogger(MessageBean.class);

    @PersistenceContext(unitName = "grupo7")
    private EntityManager em;

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
}


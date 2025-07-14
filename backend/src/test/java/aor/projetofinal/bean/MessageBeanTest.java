package aor.projetofinal.bean;

import aor.projetofinal.context.RequestContext;
import aor.projetofinal.dao.MessageDao;
import aor.projetofinal.dao.UserDao;
import aor.projetofinal.dto.ConversationDto;
import aor.projetofinal.dto.MessageDto;
import aor.projetofinal.entity.MessageEntity;
import aor.projetofinal.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageBeanTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private MessageBean messageBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getConversation_returnsEmptyListIfUsersNotFound() {
        when(userDao.findById(1)).thenReturn(null);
        when(userDao.findById(2)).thenReturn(null);

        List<MessageDto> result = messageBean.getConversation(1, 2);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findById(1);
        verify(userDao, times(1)).findById(2);
    }

    @Test
    void getConversation_returnsDtos() {
        UserEntity user1 = new UserEntity();
        user1.setId(1);
        UserEntity user2 = new UserEntity();
        user2.setId(2);

        when(userDao.findById(1)).thenReturn(user1);
        when(userDao.findById(2)).thenReturn(user2);

        MessageEntity msg = new MessageEntity();
        msg.setId(10);
        msg.setSender(user1);
        msg.setReceiver(user2);
        msg.setContent("Hello");
        msg.setRead(false);
        msg.setCreatedAt(LocalDateTime.now());

        when(messageDao.findConversation(user1, user2)).thenReturn(List.of(msg));

        List<MessageDto> result = messageBean.getConversation(1, 2);

        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getContent());
    }

    @Test
    void getUserConversations_returnsSortedList() {
        int userId = 1;

        UserEntity contact1 = new UserEntity();
        contact1.setId(2);
        contact1.setEmail("contact1@example.com");

        UserEntity contact2 = new UserEntity();
        contact2.setId(3);
        contact2.setEmail("contact2@example.com");

        when(messageDao.findContactsForUser(userId)).thenReturn(List.of(contact1, contact2));
        when(messageDao.findLastMessageBetween(userId, 2)).thenReturn(null);
        when(messageDao.findLastMessageBetween(userId, 3)).thenReturn(null);
        when(messageDao.countUnreadMessagesFrom(2, userId)).thenReturn(0);
        when(messageDao.countUnreadMessagesFrom(3, userId)).thenReturn(0);

        List<ConversationDto> result = messageBean.getUserConversations(userId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getOtherUserId() == 2));
        assertTrue(result.stream().anyMatch(c -> c.getOtherUserId() == 3));
    }

    @Test
    void markMessagesAsRead_returnsZeroIfUsersInvalid() {
        when(userDao.findById(1)).thenReturn(null);
        when(userDao.findById(2)).thenReturn(new UserEntity());

        int updated = messageBean.markMessagesAsRead(1, 2);
        assertEquals(0, updated);
    }

    @Test
    void markMessagesAsRead_returnsUpdateCount() {
        UserEntity sender = new UserEntity();
        UserEntity receiver = new UserEntity();
        when(userDao.findById(1)).thenReturn(sender);
        when(userDao.findById(2)).thenReturn(receiver);

        when(messageDao.markMessagesAsRead(sender, receiver)).thenReturn(5);

        int updated = messageBean.markMessagesAsRead(1, 2);
        assertEquals(5, updated);
    }

    @Test
    void saveMessage_returnsFalseIfDtoNull() {
        assertFalse(messageBean.saveMessage(null));
    }

    @Test
    void saveMessage_returnsFalseIfUsersInvalid() {
        MessageDto dto = new MessageDto();
        dto.setSenderId(1);
        dto.setReceiverId(2);

        when(userDao.findById(1)).thenReturn(null);
        when(userDao.findById(2)).thenReturn(new UserEntity());

        assertFalse(messageBean.saveMessage(dto));
    }

    @Test
    void saveMessage_savesMessage() {
        MessageDto dto = new MessageDto();
        dto.setSenderId(1);
        dto.setReceiverId(2);
        dto.setContent("Test content");

        UserEntity sender = new UserEntity();
        sender.setId(1);
        UserEntity receiver = new UserEntity();
        receiver.setId(2);

        when(userDao.findById(1)).thenReturn(sender);
        when(userDao.findById(2)).thenReturn(receiver);

        assertTrue(messageBean.saveMessage(dto));
        verify(messageDao, times(1)).save(any());
    }

    @Test
    void toDto_returnsNullIfEntityNull() {
        assertNull(messageBean.toDto(null));
    }

    @Test
    void toDto_returnsCorrectDto() {
        UserEntity sender = new UserEntity();
        sender.setId(1);
        sender.setProfile(null);

        UserEntity receiver = new UserEntity();
        receiver.setId(2);
        receiver.setProfile(null);

        MessageEntity entity = new MessageEntity();
        entity.setId(10);
        entity.setSender(sender);
        entity.setReceiver(receiver);
        entity.setContent("content");
        entity.setRead(true);
        entity.setCreatedAt(LocalDateTime.now());

        MessageDto dto = messageBean.toDto(entity);

        assertEquals(10, dto.getId());
        assertEquals(1, dto.getSenderId());
        assertEquals(2, dto.getReceiverId());
        assertEquals("content", dto.getContent());
        assertTrue(dto.getRead());
        assertNotNull(dto.getCreatedAt());
        assertEquals("", dto.getSenderName());
        assertEquals("", dto.getReceiverName());
    }
}

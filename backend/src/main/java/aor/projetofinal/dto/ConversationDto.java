package aor.projetofinal.dto;

/**
 * DTO representing a conversation/contact in the chat sidebar.
 * Contains information about the other user, preview of the last message,
 * its timestamp, number of unread messages, online status, and role.
 */
public class ConversationDto {

    /** The ID of the other user in the conversation */
    private int otherUserId;

    /** The display name of the other user */
    private String otherUserName;

    /** The avatar URL or path of the other user (may be null) */
    private String otherUserAvatar;

    /** The content of the last message exchanged with this user */
    private String lastMessage;

    /** The timestamp of the last message (ISO or formatted string) */
    private String lastMessageTime;

    /** The number of unread messages from this user */
    private int unreadCount;

    /** The online status of the other user */
    private boolean online;

    /** The role of the other user (e.g., "ADMIN", "USER", etc.) */
    private String role;

    // --- Getters and setters ---

    public int getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(int otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

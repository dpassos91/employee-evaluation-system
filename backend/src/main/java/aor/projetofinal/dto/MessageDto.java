package aor.projetofinal.dto;

/**
 * DTO representing a personal message exchanged between users.
 * Includes sender and receiver information, content, timestamp, and read status.
 */
public class MessageDto {
    private Integer id;
    private Integer senderId;
    private String senderName;     
    private Integer receiverId;
    private String receiverName;   
    private String content;
    private String createdAt;      
    private Boolean read;          

    public MessageDto() {}

    // Getters e Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
}


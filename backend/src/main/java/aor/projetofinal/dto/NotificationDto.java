package aor.projetofinal.dto;

import aor.projetofinal.entity.enums.NotificationEnum;

/**
 * Data Transfer Object for NotificationEntity.
 */
public class NotificationDto {
    private Integer id;
    private Integer userId;
    private String message;
    private NotificationEnum type;
    private String createdAt;
    private Boolean read;

    public NotificationDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationEnum getType() { return type; }
    public void setType(NotificationEnum type) { this.type = type; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
}


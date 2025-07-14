package aor.projetofinal.dto;

import java.time.LocalDateTime;

/**
 * DTO representing the status of a user session.
 * Contains the session token and its expiry date.
 */
public class SessionStatusDto {

    private String sessionToken;
    private LocalDateTime expiryDate;

    public SessionStatusDto(String sessionToken, LocalDateTime expiryDate) {
        this.sessionToken = sessionToken;
        this.expiryDate = expiryDate;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}

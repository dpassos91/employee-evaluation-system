package aor.projetofinal.dto;

import java.time.LocalDateTime;


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

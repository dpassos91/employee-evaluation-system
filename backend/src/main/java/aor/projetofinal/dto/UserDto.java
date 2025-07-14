package aor.projetofinal.dto;

import java.time.LocalDateTime;


/**
 * DTO representing user information for data transfer between layers.
 * Contains identification, authentication status, role, and token metadata.
 */
public class UserDto {
    private int id;
    private String email;
    private boolean confirmed;
    private String confirmationToken;
    private LocalDateTime confirmationTokenExpiry;
    private String recoveryToken;
    private LocalDateTime recoveryTokenExpiry;
    private boolean active;
    private String role;
    private LocalDateTime createdAt;

    // Construtor vazio obrigatório para Jackson
    public UserDto() {}

    // Construtor a partir de entidade (para resposta)
    public UserDto(aor.projetofinal.entity.UserEntity user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.confirmed = user.isConfirmed();   
        this.active = user.isActive();
        if (user.getRole() != null) {
            this.role = user.getRole().getName();
        }
    }

    // Getters e setters para todos os campos

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public String getConfirmationToken() { return confirmationToken; }
    public void setConfirmationToken(String confirmationToken) { this.confirmationToken = confirmationToken; }

    public LocalDateTime getConfirmationTokenExpiry() { return confirmationTokenExpiry; }
    public void setConfirmationTokenExpiry(LocalDateTime confirmationTokenExpiry) { this.confirmationTokenExpiry = confirmationTokenExpiry; }

    public String getRecoveryToken() { return recoveryToken; }
    public void setRecoveryToken(String recoveryToken) { this.recoveryToken = recoveryToken; }

    public LocalDateTime getRecoveryTokenExpiry() { return recoveryTokenExpiry; }
    public void setRecoveryTokenExpiry(LocalDateTime recoveryTokenExpiry) { this.recoveryTokenExpiry = recoveryTokenExpiry; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


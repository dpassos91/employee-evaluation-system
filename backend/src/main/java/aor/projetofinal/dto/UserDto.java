package aor.projetofinal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDto {
    private int id;
    private String email;
    private String password;
    private boolean confirmed;
    private String confirmationToken;
    private LocalDate confirmationTokenExpiry;
    private String recoveryToken;
    private LocalDate recoveryTokenExpiry;
    private boolean active;
    private LocalDateTime createdAt;

    // Construtor vazio obrigatório para Jackson
    public UserDto() {}

    // Construtor a partir de entidade (para resposta)
    public UserDto(aor.projetofinal.entity.UserEntity user) {
        this.email = user.getEmail();
        // Não copiamos a password!
    }

    // Getters e setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


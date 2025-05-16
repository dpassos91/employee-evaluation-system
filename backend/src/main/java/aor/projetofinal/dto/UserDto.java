package aor.projetofinal.dto;

public class UserDto {
    private String email;
    private String password; // só usado na entrada

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


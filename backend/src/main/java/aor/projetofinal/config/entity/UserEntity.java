package aor.projetofinal.config.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "User")

public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "email", nullable = false, unique = true, updatable = false)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "account_is_confirmed", nullable = false)
    private boolean confirmed;

    @Column(name = "accountConfirmToken", nullable = true)
    private String accountConfirmToken;

    @Column(name = "accountConfirm_Token_ExpiryDate", nullable = true)
    private LocalDateTime accountConfirmTokenExpiryDate;

    @Column(name = "forgottenPass_Token", nullable = true)
    private String forgottenPassToken;

    @Column(name = "forgottenPass_Token_ExpiryDate", nullable = true)
    private LocalDateTime forgottenPassTokenExpiryDate;

    @Column(name = "Active", nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(name = "register_date", nullable = false, updatable = false)
    private LocalDateTime registerDate;

    @ManyToMany(mappedBy = "role_function")
    private List<RoleEntity> role_functions;


    public UserEntity() {
    }

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

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getAccountConfirmToken() {
        return accountConfirmToken;
    }

    public void setAccountConfirmToken(String accountConfirmToken) {
        this.accountConfirmToken = accountConfirmToken;
    }

    public LocalDateTime getAccountConfirmTokenExpiryDate() {
        return accountConfirmTokenExpiryDate;
    }

    public void setAccountConfirmTokenExpiryDate(LocalDateTime accountConfirmTokenExpiryDate) {
        this.accountConfirmTokenExpiryDate = accountConfirmTokenExpiryDate;
    }

    public String getForgottenPassToken() {
        return forgottenPassToken;
    }

    public void setForgottenPassToken(String forgottenPassToken) {
        this.forgottenPassToken = forgottenPassToken;
    }

    public LocalDateTime getForgottenPassTokenExpiryDate() {
        return forgottenPassTokenExpiryDate;
    }

    public void setForgottenPassTokenExpiryDate(LocalDateTime forgottenPassTokenExpiryDate) {
        this.forgottenPassTokenExpiryDate = forgottenPassTokenExpiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(LocalDateTime registerDate) {
        this.registerDate = registerDate;
    }

    public List<RoleEntity> getRole_functions() {
        return role_functions;
    }

    public void setRole_function(List<RoleEntity> role_function) {
        this.role_functions = role_functions;
    }
}

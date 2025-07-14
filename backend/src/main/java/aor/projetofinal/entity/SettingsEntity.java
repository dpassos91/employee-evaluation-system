package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entity representing system-wide configuration settings for token expiration.
 * This includes confirmation, recovery, and session token timeouts (in minutes).
 */
@Entity
@Table(name = "settings")
public class SettingsEntity implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "confirmation", nullable = false)
    private int confirmationTokenTimeout;

    @Column(name = "recovery", nullable = false)
    private int recoveryTokenTimeout;

    @Column(name = "session", nullable = false)
    private int sessionTokenTimeout;

    // Construtor vazio
    public SettingsEntity() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getConfirmationTokenTimeout() {
        return confirmationTokenTimeout;
    }
    public void setConfirmationTokenTimeout(int confirmationTokenTimeout) {
        this.confirmationTokenTimeout = confirmationTokenTimeout;
    }

    public int getRecoveryTokenTimeout() {
        return recoveryTokenTimeout;
    }
    public void setRecoveryTokenTimeout(int recoveryTokenTimeout) {
        this.recoveryTokenTimeout = recoveryTokenTimeout;
    }

    public int getSessionTokenTimeout() {
        return sessionTokenTimeout;
    }
    public void setSessionTokenTimeout(int sessionTokenTimeout) {
        this.sessionTokenTimeout = sessionTokenTimeout;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettingsEntity that = (SettingsEntity) o;
        return id == that.id;
    }

    // hash
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // toString
    @Override
    public String toString() {
        return "SettingsEntity{" +
                "id=" + id +
                ", confirmationTokenTimeout=" + confirmationTokenTimeout +
                ", recoveryTokenTimeout=" + recoveryTokenTimeout +
                ", sessionTokenTimeout=" + sessionTokenTimeout +
                '}';
    }
}



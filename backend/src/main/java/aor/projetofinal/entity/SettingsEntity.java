package aor.projetofinal.entity;

import jakarta.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "Settings")
public class SettingsEntity implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "confirmation", nullable = false)
    private int confirmationTokenTimeout;

    @Column(name = "reset", nullable = false)
    private int recoveryTokenTimeout;

    @Column(name = "session", nullable = false)
    private int sessionTokenTimeout;


    public SettingsEntity() {
    }

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
}



package aor.projetofinal.dto;

public class SettingsDTO {
    private int confirmationTokenTimeout;
    private int recoveryTokenTimeout;
    private int sessionTokenTimeout;

    // Getters and Setters
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
